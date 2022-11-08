package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.configuration.BPECSComputeInfrastructure;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerContainer;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.datamodel.utils.BPExceptionLogger;
import eu.essi_lab.vlab.core.engine.utils.BPUtils;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ContainerOverride;
import software.amazon.awssdk.services.ecs.model.InvalidParameterException;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.StopTaskRequest;
import software.amazon.awssdk.services.ecs.model.TaskOverride;

/**
 * @author Mattia Santoro
 */
public class ECSClient implements IContainerOrchestratorCommandExecutor {

	private static final String VLAB_PREPARE_TASK_CONTAINER_NAME = "AlpineSFTP";
	private static final String VLAB_AWSCLI_TASK_CONTAINER_NAME = "AWSCLI";
	private static final String VLAB_MODEL_TASK_FAMILY = "VLalModelTask";
	private static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
	private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
	private Logger logger = LogManager.getLogger(ECSClient.class);

	private BPECSComputeInfrastructure computeInfrastructure;
	private EcsClient cClient;
	private EcsClient eClient;
	private static final String PREPARE_FAMILY = "VLabPrepare";
	private static final String AWS_CLI_TASK_FAMILY = "VLabAwsCli";
	private String rootFolder;

	@Override
	public void setRootExecutionFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}

	@Override
	public void setBPInfrastructure(BPComputeInfrastructure infrastructure) throws BPException {

		if (infrastructure instanceof BPECSComputeInfrastructure i) {
			this.computeInfrastructure = i;
		} else {
			logger.error("Can't use this IContainerOrchestratorCommandExecutor with infrastructure of type {}",
					infrastructure.getClass().getSimpleName());
			throw new BPException("Can't use this ECSClient with infrastructure of type " + infrastructure.getClass().getSimpleName(),
					BPException.ERROR_CODES.BAD_CONFIGURATION);
		}

	}

	ECSTaskManager createTaskManager(RunTaskRequest runTaskRequest, EcsClient client, Integer retriesmax) {

		var m = new ECSTaskManager(client, runTaskRequest);

		m.handler.setMaxRetries(retriesmax);

		return m;
	}

	protected EcsClient getControllerClient() {
		if (this.cClient == null)
			this.cClient = EcsClient.builder().region(Region.of(computeInfrastructure.getDeployECSRegion())).credentialsProvider(
					StaticCredentialsProvider.create(AwsBasicCredentials.create(computeInfrastructure.getDeployECSAccessKey(),
							computeInfrastructure.getDeployECSSecretKey()))).build();

		return this.cClient;
	}

	protected EcsClient getExecutorClient() {
		if (this.eClient == null)
			this.eClient = EcsClient.builder().region(Region.of(computeInfrastructure.getExecuteECSRegion())).credentialsProvider(
					StaticCredentialsProvider.create(AwsBasicCredentials.create(computeInfrastructure.getExecuteECSAccessKey(),
							computeInfrastructure.getExecuteECSSecretKey()))).build();

		return this.eClient;
	}

	@Override
	public ContainerOrchestratorCommandResult downloadFileTo(String fileURL, String targetFile, Long maxwait) {

		logger.info("Downloading {} to {}", fileURL, targetFile);

		ContainerOrchestratorCommandResult res = createDirectoryStructure(targetFile, 1000L * 60 * 60);

		if (!res.isSuccess()) {
			logger.warn("Failed to create directory structure for downloading {} to {}", fileURL, targetFile);
			return res;
		}

		try {
			List<String> commands = BPUtils.createCommand(fileURL, targetFile, computeInfrastructure.getS3AccessKey(),
					computeInfrastructure.getS3SecretKey(), computeInfrastructure.getS3ServiceUrl());

			logger.debug("Created {} download commands", commands.size());

			for (String cmd : commands) {

				RunTaskRequest runTaskRequest = createRunRequest(computeInfrastructure.getPrepareImage(), PREPARE_FAMILY,
						VLAB_PREPARE_TASK_CONTAINER_NAME, Arrays.asList(cmd), Arrays.asList("/bin" + "/sh", "-c"),
						computeInfrastructure.getCoreCluster());

				RunTaskResponse task = getControllerClient().runTask(runTaskRequest);

				logger.debug("Requested runTask for downloading {} to {} with command {}", fileURL, targetFile, cmd);

				ECSTaskManager taskManager = createTaskManager(runTaskRequest, getControllerClient(), 5);

				ContainerOrchestratorCommandResult result = taskManager.getTaskResult(task, maxwait,
						computeInfrastructure.getCoreCluster());

				if (!result.isSuccess())
					return result;

				logger.info("Completed runTask for downloading {} to {} with result: {}", fileURL, targetFile, result.isSuccess());

			}

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);
			result.setMessage(e.getMessage());

			return result;

		}
		var result = new ContainerOrchestratorCommandResult();

		result.setSuccess(true);
		return result;

	}

	private RunTaskResponse requestRunAwsCliTask(ContainerOverride containerOverride, RunTaskRequest runTaskRequest, String accessKey,
			String secretKey, EcsClient client) {

		KeyValuePair ak = KeyValuePair.builder().name(AWS_ACCESS_KEY_ID).value(accessKey).build();

		KeyValuePair sk = KeyValuePair.builder().name(AWS_SECRET_ACCESS_KEY).value(secretKey).build();

		containerOverride = containerOverride.toBuilder().environment(ak, sk).build();

		runTaskRequest = runTaskRequest.toBuilder().overrides(TaskOverride.builder().containerOverrides(containerOverride).build()).build();

		return client.runTask(runTaskRequest);
	}

	public RunTaskRequest createRunRequest(String imageName, String family, String taskContainerName, List<String> command,
			List<String> entryPoint, String cluster) throws BPException {

		logger.trace("Creating runTask request for {}", imageName);

		var emt = createECSPreparatoryTask(taskContainerName);

		emt.setTaskFamily(family);

		emt.setRunFolder("/");

		VLabDockerImage image = new VLabDockerImage();

		image.setImage(imageName);
		VLabDockerContainer container = new VLabDockerContainer();
		container.setCommand(command);
		container.setEntryPoint(entryPoint);

		VLabDockerResources resources = new VLabDockerResources();
		resources.setMemory_mb("128");
		image.setResources(resources);

		image.setContainer(container);

		var runTaskRequest = emt.createRunTaskRequest(image, "");

		return runTaskRequest.toBuilder().cluster(cluster).build();

	}

	@Override
	public ContainerOrchestratorCommandResult copyFileTo(File file, String destinationFile, Long maxwait) {

		logger.info("Copying file {} to {}", file.getName(), destinationFile);

		logger.debug("Utilizing local scripts update strategy");

		new File(destinationFile).mkdirs();

		var destPath = new File(destinationFile).toPath();

		return BPUtils.copyLocalToLocal(file.toPath(), destPath);

	}

	private String createUniqueKey() {
		return UUID.randomUUID().toString();
	}

	public ECSModelTask createECSModelTask(String containerName) {
		return new ECSModelTask(getExecutorClient(), computeInfrastructure.getExecuteECSRegion(), computeInfrastructure.getEfsFolder(),
				containerName, computeInfrastructure.getModelExecutionLogGroup(), computeInfrastructure.getModelExecutionLogPrefix(),
				rootFolder);
	}

	public ECSModelTask createECSPreparatoryTask(String containerName) {
		return new ECSModelTask(getControllerClient(), computeInfrastructure.getDeployECSRegion(), computeInfrastructure.getEfsFolder(),
				containerName, computeInfrastructure.getDeployLogGroup(), computeInfrastructure.getDeployLogPrefix(), rootFolder);

	}

	public SubmitImageTaskResult submitImageTask(VLabDockerImage image, Optional<ContainerOrchestratorReservationResult> optReservation,
			String runFolder, String runid, BPRunStatus status, String family, String cluster, EcsClient client, Integer maxRetries)
			throws BPException {

		logger.info("Start run image of {}", image.getImage());

		var emt = createECSModelTask(computeInfrastructure.getModelTaskContainerName());

		emt.setTaskFamily(family);

		emt.setRunFolder(runFolder);

		logger.debug("Creating runTask request for {}", image.getImage());

		var runTaskRequest = emt.createRunTaskRequest(image, runid);

		runTaskRequest = runTaskRequest.toBuilder().cluster(cluster).build();

		if (optReservation.isPresent()) {

			ContainerOrchestratorReservationResult reservation = optReservation.get();

			logger.info("Releasing reserved resources {}", reservation.getTaskArn());

			release(reservation);
		} else {
			logger.info("No reservation to release for {}", runid);
		}

		RunTaskResponse task = client.runTask(runTaskRequest);

		logger.info("Requested runTask for image {}", image.getImage());

		status.setModelTaskId(getTaskId(task));

		ECSTaskManager taskManager = createTaskManager(runTaskRequest, client, maxRetries);

		return new SubmitImageTaskResult(taskManager, task);

	}

	@Override
	public ContainerOrchestratorCommandResult runImage(VLabDockerImage image, ContainerOrchestratorReservationResult reservation,
			String runFolder, Long maxwait, String runid, BPRunStatus status) throws BPException {

		var submitImageTaskResult = submitImageTask(image, Optional.of(reservation), runFolder, runid, status, VLAB_MODEL_TASK_FAMILY,
				computeInfrastructure.getModelCluster(), getExecutorClient(), 0);

		ECSTaskManager taskManager = submitImageTaskResult.getEscTaskManager();

		RunTaskResponse task = submitImageTaskResult.getRunTaskResult();

		ContainerOrchestratorCommandResult result = taskManager.getTaskResult(task, maxwait, computeInfrastructure.getModelCluster());

		logger.info("Completed run image {} with result: {}", image.getImage(), result.isSuccess());

		return result;

	}

	private String getTaskId(RunTaskResponse task) {
		try {

			String id = task.tasks().get(0).taskArn().split("task/")[1];

			logger.debug("Found Task id {}", id);

			return id;

		} catch (Exception throwable) {
			logger.warn("Unable to find task id", throwable);
		}

		return null;
	}

	@Override
	public ContainerOrchestratorReservationResult reserveResources(VLabDockerResources resources) {

		logger.info("Reserving resources on cluster {}: memory --> {}, cpu --> {}", computeInfrastructure.getModelCluster(),
				resources.getMemory_mb(), resources.getCpu_units());

		String command = BPUtils.createReserveCommand();
		RunTaskResponse task = null;
		RunTaskRequest runTaskRequest = null;
		try {
			runTaskRequest = createRunRequest(computeInfrastructure.getPrepareImage(), PREPARE_FAMILY, VLAB_PREPARE_TASK_CONTAINER_NAME,
					Arrays.asList(command), Arrays.asList("/bin" + "/sh", "-c"), computeInfrastructure.getModelCluster());

			var runTaskRequestBuilder = runTaskRequest.toBuilder();

			var containerOverride = ContainerOverride.builder().name(VLAB_PREPARE_TASK_CONTAINER_NAME).command(command).build();

			String requiredMem = resources.getMemory_mb();

			if (requiredMem != null && !requiredMem.equalsIgnoreCase(""))
				containerOverride = containerOverride.toBuilder().memoryReservation(Integer.valueOf(requiredMem)).build();

			String requiredCpu = resources.getCpu_units();

			if (requiredCpu != null && !requiredCpu.equalsIgnoreCase(""))
				containerOverride = containerOverride.toBuilder().cpu(Integer.valueOf(requiredCpu)).build();

			runTaskRequestBuilder.overrides(TaskOverride.builder().containerOverrides(containerOverride).build());

			runTaskRequest = runTaskRequestBuilder.build();

			task = getExecutorClient().runTask(runTaskRequest);

		} catch (InvalidParameterException ipe) {

			if (ipe.getMessage() != null && !"".equalsIgnoreCase(ipe.getMessage())) {

				var nocontainerMsg = "No Container Instances were found";

				if (ipe.getMessage().toLowerCase().contains(nocontainerMsg.toLowerCase())) {

					logger.info("Found empty model cluster {}", computeInfrastructure.getModelCluster());

					var result = new ContainerOrchestratorReservationResult();

					result.setAcquired(false);

					result.setMessage("No running instance is available at the moment");

					return result;
				}

			}

			throw ipe;

		} catch (BPException e) {

			BPExceptionLogger.logBPException(e, logger, Level.ERROR);

			var result = new ContainerOrchestratorReservationResult();

			result.setAcquired(false);

			result.setMessage("Exception running reservation task (BP code: " + e.getErroCode() + ")");

			return result;
		}

		logger.debug("Requested runTask to reserve rssources [{} MB, {} CPU]", resources.getMemory_mb(), resources.getCpu_units());

		ECSTaskManager taskManager = createTaskManager(runTaskRequest, getExecutorClient(), 0);

		ContainerOrchestratorReservationResult result = taskManager.getReservationResult(task, computeInfrastructure.getModelCluster());

		logger.info("Reserving resources [{} MB, {} CPU] completed with result: {}", resources.getMemory_mb(), resources.getCpu_units(),
				result.isAcquired());

		return result;

	}

	public void release(ContainerOrchestratorReservationResult reservation) throws BPException {

		if (Boolean.FALSE.equals(reservation.isAcquired())) {

			if (reservation.getTaskArn() != null) {
				logger.warn("Reservation acquired is false, task arn is expceted to be null but is {}", reservation.getTaskArn());
			}

			logger.info("Reservation acquired is false, returning");
			return;
		}

		try {
			logger.info("Starting to release resources from {}", reservation.getTaskArn());

			StopTaskRequest stopRequest = StopTaskRequest.builder().task(reservation.getTaskArn()).cluster(
					computeInfrastructure.getModelCluster()).build();

			getExecutorClient().stopTask(stopRequest);

			logger.info("Completed release of resources from {}", reservation.getTaskArn());

		} catch (Exception throwable) {

			logger.error("Unable to stop task {}", reservation.getTaskArn(), throwable);

			throw new BPException("Unable to stop task " + reservation.getTaskArn(), BPException.ERROR_CODES.RESOURCES_RELEASE_ERROR);

		}

	}

	@Override
	public ContainerOrchestratorCommandResult appendParamTo(String key, Number value, String targetFile, Long maxwait) {

		logger.info("Append parameter {}={} to {}", key, value, targetFile);

		ContainerOrchestratorCommandResult res = createDirectory(targetFile, 1000L * 60 * 60);

		if (!res.isSuccess()) {
			logger.warn("Failed to create directory structure for VLab param file {}", targetFile);
			return res;
		}

		try {
			String cmd = BPUtils.createCommand(key, value, targetFile);

			logger.debug("Local command for appending {}={} to {} with command {}", key, value, targetFile, cmd);

			String err = "Can't append vlab param " + key;

			doExecuteLocalCmd(cmd, err);

			logger.debug("Local command for appending {}={} to {} success", key, value, targetFile);

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);
			result.setMessage(e.getMessage());

			return result;

		}
		var result = new ContainerOrchestratorCommandResult();

		result.setSuccess(true);
		return result;

	}

	void doExecuteLocalCmd(String command, String err) throws BPException {
		BPUtils.executeLocalCommand(command, err);
	}

	ContainerOrchestratorCommandResult doSimpleCommand(String cmd, String err, Long maxwait) {

		try {

			logger.debug("Executing command");

			doExecuteLocalCmd(cmd, err);

			logger.debug("Local command success {}", cmd);

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);
			result.setMessage(e.getMessage());

			return result;

		}
		var result = new ContainerOrchestratorCommandResult();

		result.setSuccess(true);
		return result;
	}

	@Override
	public ContainerOrchestratorCommandResult move(String source, String target, Long maxwait) {

		logger.trace("Requested move command {} to {}", source, target);

		String cmd = BPUtils.createMoveCmd(source, target);

		logger.trace("Move command {} to {} is: {}", source, target, cmd);

		return doSimpleCommand(cmd, "Can't move from " + source + " to " + target, maxwait);

	}

	@Override
	public BPLogChunk readLogChunk(String taskId, Optional<String> containerName, Optional<Integer> sinceSeconds) throws BPException {
		throw new BPException("Not implemented in ECS", BPException.ERROR_CODES.OPERATION_NOT_SUPPORTED);
	}

	@Override
	public void testConnection() throws BPException {

		try {
			getControllerClient().describeClusters();
			getExecutorClient().describeClusters();
		} catch (AwsServiceException | SdkClientException ex) {

			logger.error("Error testing connection to ECS", ex);

			throw new BPException("Can't connect to ECS", BPException.ERROR_CODES.BAD_CONFIGURATION);

		}

	}

	@Override
	public ContainerOrchestratorCommandResult appendParamTo(String key, String value, String targetFile, Long maxwait) throws BPException {

		logger.info("Append parameter {}={} to {}", key, value, targetFile);

		ContainerOrchestratorCommandResult res = createDirectory(targetFile, 1000L * 60 * 60);

		if (!res.isSuccess()) {
			logger.warn("Failed to create directory structure for VLab param file {}", targetFile);
			return res;
		}

		String cmd = BPUtils.createCommand(key, value, targetFile, "dirScript.sh");

		logger.debug("Local command for appending {}={} to {} with command {}", key, value, targetFile, cmd);

		String err = "Can't append vlab param " + key;

		doExecuteLocalCmd(cmd, err);

		logger.debug("Local command for appending {}={} to {} success", key, value, targetFile);

		var dccr = new ContainerOrchestratorCommandResult();

		dccr.setSuccess(true);
		return dccr;

	}

	@Override
	public ContainerOrchestratorCommandResult removeDirectory(String folder, Long maxWait) {

		return execPrepareTaskCommand("rm -rf " + folder, maxWait, "Can't remove directory " + folder);
	}

	private ContainerOrchestratorCommandResult save2S3(String command, Long maxwait) {

		RunTaskRequest runTaskRequest = null;
		try {
			runTaskRequest = createRunRequest(computeInfrastructure.getAwsCliImage(), AWS_CLI_TASK_FAMILY, VLAB_AWSCLI_TASK_CONTAINER_NAME,
					Arrays.asList(command), Arrays.asList("/bin/sh", "-c"), computeInfrastructure.getCoreCluster());
		} catch (BPException e) {
			BPExceptionLogger.logBPException(e, logger, Level.ERROR);
			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Exception creating run task request (BP code: " + e.getErroCode() + ")");

			return result;

		}

		var containerOverride = ContainerOverride.builder().name(VLAB_AWSCLI_TASK_CONTAINER_NAME).command(command).build();

		RunTaskResponse task = requestRunAwsCliTask(containerOverride, runTaskRequest, computeInfrastructure.getS3AccessKey(),
				computeInfrastructure.getS3SecretKey(), getControllerClient());

		ECSTaskManager taskManager = createTaskManager(runTaskRequest, getControllerClient(), 5);

		return taskManager.getTaskResult(task, maxwait, computeInfrastructure.getCoreCluster());

	}

	@Override
	public ContainerOrchestratorCommandResult saveFileToWebStorage(String filePath, String bucketName, String s3ObjectKey,
			Boolean publicread, Long maxwait) {

		String command = BPUtils.createS3UploadCommand(filePath, bucketName, s3ObjectKey, publicread,
				computeInfrastructure.getS3ServiceUrl());

		logger.info("Saving output file with command {}", command);

		return save2S3(command, maxwait);

	}

	@Override
	public ContainerOrchestratorCommandResult saveFolderToWebStorage(String folderPath, String bucketName, String s3BaseObjectKey,
			Boolean publicread, Long maxwait) {

		String command = BPUtils.createS3UploadFolderCommand(folderPath, bucketName, s3BaseObjectKey, publicread,
				computeInfrastructure.getS3ServiceUrl());

		logger.info("Saving output folder with command {}", command);

		return save2S3(command, maxwait);
	}

	private ContainerOrchestratorCommandResult execPrepareTaskCommand(String command, Long maxWait, String err) {

		logger.trace("Local command");

		try {
			doExecuteLocalCmd(command, err);

			logger.debug("Local command success");

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(true);

			return result;

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Failed command with error " + e.getMessage());

			return result;

		}

	}

	@Override
	public ContainerOrchestratorCommandResult createDirectory(String target, Long maxWait) {

		return createDirectoryStructure(target, maxWait);

	}

	ContainerOrchestratorCommandResult mkDir(List<String> dirsToCreate, Long maxWait) {

		String command = BPUtils.createDirectoriesCommand(dirsToCreate);

		logger.trace("Create directories command: {}", command);

		logger.trace("Local command for directories creation");

		var err = "Can't create requested directories";

		try {
			BPUtils.executeLocalCommand(command, err);

			logger.debug("Local command for directories creation success");

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(true);

			return result;

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Failed to create directories with error" + e.getMessage());

			return result;

		}
	}

	private ContainerOrchestratorCommandResult createDirectoryStructure(String targetFile, Long maxWait) {

		List<String> dirsToCreate = BPUtils.extractDirs(targetFile);

		return mkDir(dirsToCreate, maxWait);

	}

	public void setCoreClient(EcsClient ecs_core) {
		cClient = ecs_core;
	}

	public void setExecutionClient(EcsClient ecs_ex) {
		eClient = ecs_ex;
	}

	public BPECSComputeInfrastructure getBPComupteInfrastructure() {
		return this.computeInfrastructure;
	}

	public String getRootFolder() {
		return this.rootFolder;
	}
}
