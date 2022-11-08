package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ModelLogCollector;
import eu.essi_lab.vlab.ce.controller.services.kubernetes.jobs.VLabAwsCliJob;
import eu.essi_lab.vlab.ce.controller.services.kubernetes.jobs.VLabModelJob;
import eu.essi_lab.vlab.ce.controller.services.kubernetes.jobs.VLabPrepeareJob;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPKubernetesComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import eu.essi_lab.vlab.core.datamodel.utils.BPExceptionLogger;
import eu.essi_lab.vlab.core.engine.factory.StorageFactory;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.engine.utils.BPUtils;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.util.Config;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubernetesClient implements IContainerOrchestratorCommandExecutor {

	public static final String JOBS_NS = "gi-suite";
	public static final Integer DEFAULT_JOB_TTL_SECONDS = 60 * 60 * 24 * 7;
	private Logger logger = LogManager.getLogger(KubernetesClient.class);
	private CoreV1Api cApi;
	private BatchV1Api bApi;

	private static final String CANT_SUBMIT_JOB = "Can't submit job {}";

	private BPKubernetesComputeInfrastructure computeInfrastructure;

	private static final String REQUESTED_RUN_IMAGE = "Requested runTask for image {}";

	private AppsV1Api aApi;

	private static final String BACKGROUND_PROPAGATION_POLICY = "Background";
	private String vlabRootFolder;

	@Override
	public void setRootExecutionFolder(String rootFolder) {
		this.vlabRootFolder = rootFolder;
	}

	public BPKubernetesComputeInfrastructure getBPInfrastructure() {
		return this.computeInfrastructure;
	}

	@Override
	public void setBPInfrastructure(BPComputeInfrastructure infrastructure) throws BPException {

		if (infrastructure instanceof BPKubernetesComputeInfrastructure i) {
			this.computeInfrastructure = i;

		} else {
			logger.error("Can't use this IContainerOrchestratorCommandExecutor with infrastructure of type {}",
					infrastructure.getClass().getSimpleName());
			throw new BPException(
					"Can't use this KubernetesClient with infrastructure of type " + infrastructure.getClass().getSimpleName(),
					BPException.ERROR_CODES.BAD_CONFIGURATION);
		}

		logger.trace("KubernetesClient server url {}", computeInfrastructure.getServerUrl());

		logger.trace("Connecting with token");
		ApiClient client = Config.fromToken(computeInfrastructure.getServerUrl(), computeInfrastructure.getToken(), false);

		logger.trace("Setting default api client");
		Configuration.setDefaultApiClient(client);

		logger.trace("Setting core api");
		cApi = new CoreV1Api(client);

		logger.trace("Setting batch api");
		bApi = new BatchV1Api(client);

		logger.trace("Setting apps api");
		aApi = new AppsV1Api(client);

		logger.trace("Completed Init KubernetesClient");
	}

	public void testConnection() throws BPException {

		try {
			var response = getCoreApi().listNamespaceCall(null, null, null, null, null, 5, null, null, 10, Boolean.FALSE, null).execute();

			if (response.isSuccessful()) {
				logger.debug("Successful kubernetes connection test");
				return;
			}
			if (logger.isErrorEnabled())
				logger.error("Retrieved failed response from Kubernetes API -> {}", response.body().string());

		} catch (IOException | ApiException e) {

			logger.error("Exception connecting to kubernetes at {}", computeInfrastructure.getServerUrl(), e);

		}
		throw new BPException("Can't connect to kubernetes cluster", BPException.ERROR_CODES.BAD_CONFIGURATION);
	}

	@Override
	public ContainerOrchestratorCommandResult createDirectory(String target, Long maxWait) {
		/**
		 * Remote ok
		 */

		List<String> dirsToCreate = BPUtils.extractDirs(target);

		return mkDir(dirsToCreate);
	}

	private ContainerOrchestratorCommandResult submitToControllerAndGetResult(V1Job kubeJob, Long maxWait, String errTxt) {
		V1Job responseJob = null;

		try {

			responseJob = getBatchApi().createNamespacedJob(JOBS_NS, kubeJob, null, null, null);

		} catch (ApiException e) {

			logger.error(CANT_SUBMIT_JOB, kubeJob.getMetadata().getName(), e);

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage(errTxt + e.getMessage());

			return result;

		}

		KubernetesJobManager jobManager = createJobManager(5);

		return jobManager.getJobResult(kubeJob, responseJob, maxWait, 0);
	}

	KubernetesJobManager createJobManager(Integer maxRetries) {
		var m = new KubernetesJobManager(getBatchApi(), getCoreApi());

		m.handler.setMaxRetries(maxRetries);

		return m;
	}

	ContainerOrchestratorCommandResult submitLocalCmdAndGetResult(String command, String err) {

		try {
			logger.trace("Submitting local command {}", command);

			BPUtils.executeLocalCommand(command, err);

			logger.trace("Local command succeded with command {}", command);

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(true);

			return result;

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Failed local command with error" + e.getMessage());

			return result;

		}

	}

	public ContainerOrchestratorCommandResult mkDir(List<String> dirsToCreate) {

		/**
		 * Remote ok
		 */

		String command = BPUtils.createDirectoriesCommand(dirsToCreate);

		logger.trace("Create directories command: {}", command);

		return submitLocalCmdAndGetResult(command, "Can't create requested directories");

	}

	@Override
	public ContainerOrchestratorCommandResult downloadFileTo(String fileURL, String targetFile, Long maxWait) {

		/**
		 * Remote ok
		 */

		logger.info("Downloading {} to {}", fileURL, targetFile);

		ContainerOrchestratorCommandResult res = createDirectory(targetFile, 1000L * 60 * 60);

		if (!res.isSuccess()) {
			logger.warn("Failed to create directory structure for downloading {} to {}", fileURL, targetFile);
			return res;
		}

		var kubejobid = "None";

		try {

			List<String> commands = BPUtils.createCommand(fileURL, targetFile, computeInfrastructure.getS3AccessKey(),
					computeInfrastructure.getS3SecretKey(), computeInfrastructure.getS3ServiceUrl());

			logger.debug("Created {} download commands", commands.size());

			for (String cmd : commands) {

				var job = new VLabPrepeareJob(computeInfrastructure.getPrepareImage());

				job.setCommand(cmd);

				job.setNodeSelector(Optional.ofNullable(computeInfrastructure.getControllerNodeSelector()));
				job.setNodeTolerations(computeInfrastructure.getControllerNodeTolerations());
				job.setVlabPV(computeInfrastructure.getVlabPv());
				job.setVlabPvClaim(computeInfrastructure.getVlabPvClaim());
				job.setRunFolder(vlabRootFolder);
				V1Job kubeJob = job.getJob();

				kubejobid = kubeJob.getMetadata().getName();

				V1Job responseJob = getBatchApi().createNamespacedJob(JOBS_NS, kubeJob, null, null, null);

				KubernetesJobManager jobManager = createJobManager(5);

				ContainerOrchestratorCommandResult result = jobManager.getJobResult(kubeJob, responseJob, maxWait, 0);

				if (!result.isSuccess())
					return result;

				logger.info("Completed runTask for downloading {} to {} with result: {}", fileURL, targetFile, result.isSuccess());

			}

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);
			result.setMessage(e.getMessage());

			return result;

		} catch (ApiException e) {

			logger.error(CANT_SUBMIT_JOB, kubejobid, e);

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Failed to download input file " + e.getMessage());

			return result;

		}

		var result = new ContainerOrchestratorCommandResult();

		result.setSuccess(true);
		return result;

	}

	@Override
	public ContainerOrchestratorCommandResult copyFileTo(File file, String destinationFile, Long maxwait) {

		logger.debug("Utilizing local scripts update strategy");
		new File(destinationFile).mkdirs();

		var destPath = new File(destinationFile).toPath();

		return BPUtils.copyLocalToLocal(file.toPath(), destPath);

	}

	@Override
	public ContainerOrchestratorCommandResult runImage(VLabDockerImage image, ContainerOrchestratorReservationResult reservation,
			String runFolder, Long maxwait, String runid, BPRunStatus status) throws BPException {

		logger.info(REQUESTED_RUN_IMAGE, image.getImage());

		SubmitJobResult submitResponse = null;
		try {
			submitResponse = submitJob(image, Optional.of(reservation), runFolder, runid,
					Optional.ofNullable(computeInfrastructure.getExecutorNodeSelector()),
					computeInfrastructure.getExecutorNodeTolerations(), 0);
		} catch (ApiException e) {
			logger.error(CANT_SUBMIT_JOB, runid, e);

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Failed creating model job for run id " + runid + " " + e.getMessage());

			return result;
		}

		var jobManager = submitResponse.getKubernetesJobManager();

		//TODO  I probably need to provide pod id?  status.setModelTaskId(getTaskId(task))

		jobManager.getPodId(submitResponse.getResponseJob()).ifPresent(status::setModelTaskId);

		Optional<ModelLogCollector> logRegistry = initModelLog(status.getModelTaskId(), status.getRunid());

		ContainerOrchestratorCommandResult result = jobManager.getJobResult(submitResponse.getOriginalJob(),
				submitResponse.getResponseJob(), maxwait, 0);

		finalizeModelLog(logRegistry, status.getModelTaskId(), status.getRunid());

		logger.info("Completed run image {} with result: {}", image.getImage(), result.isSuccess());

		return result;

	}

	private void finalizeModelLog(Optional<ModelLogCollector> oLogRegistry, String modelTaskId, String runid) {

		oLogRegistry.ifPresent(modelLogCollector -> modelLogCollector.finalizeModelLogCollection(modelTaskId, runid));

	}

	Optional<ModelLogCollector> initModelLog(String modelTaskId, String runid) {
		ModelLogCollector logRegistry = null;
		try {
			logRegistry = new ModelLogCollector(this, new StorageFactory().getBPRunLogStorage());

			logRegistry.addModelLog(modelTaskId, runid);

			logRegistry.watch(modelTaskId, runid);

			return Optional.of(logRegistry);

		} catch (BPException e) {

			logger.error("Can't initialize Model Log Collector, model execution logs will not be available for run {}", runid);
			BPExceptionLogger.logBPException(e, logger, Level.ERROR);

		}

		return Optional.empty();

	}

	public void prepareVLabModelJob(VLabModelJob modelJob, VLabDockerImage image, String runFolder, Optional<String> nodeSelector,
			String nodeTolerations, Map<String, String> envs) {

		modelJob.setRunFolder(runFolder);

		modelJob.setNodeSelector(nodeSelector);
		modelJob.setNodeTolerations(nodeTolerations);

		modelJob.setEnvVars(envs);

		modelJob.setRootFolder(vlabRootFolder);

		Optional<VLabDockerResources> opResources = Optional.ofNullable(image.getResources());

		opResources.ifPresent(resources -> {

			String requiredMem = resources.getMemory_mb();

			if (requiredMem != null && !requiredMem.equalsIgnoreCase(""))
				modelJob.setMemoryReservation(Integer.valueOf(requiredMem));

			String requiredCpu = resources.getCpu_units();

			if (requiredCpu != null && !requiredCpu.equalsIgnoreCase(""))
				modelJob.setCpu(Integer.valueOf(requiredCpu));

		});

		modelJob.setVlabPV(computeInfrastructure.getVlabPv());
		modelJob.setVlabPvClaim(computeInfrastructure.getVlabPvClaim());

	}

	public VLabModelJob instantiateModelJob(VLabDockerImage image, String runid) {
		return new VLabModelJob(image, runid);
	}

	SubmitJobResult submitJob(VLabDockerImage image, Optional<ContainerOrchestratorReservationResult> optReservation, String runFolder,
			String runid, Optional<String> nodeSelector, String nodeTolerations, Integer maxRetries, Map<String, String> envs)
			throws ApiException, BPException {

		logger.info("Start submit job image of {}", image.getImage());

		logger.debug("Creating model job for {}", image.getImage());
		var modelJob = instantiateModelJob(image, runid);

		prepareVLabModelJob(modelJob, image, runFolder, nodeSelector, nodeTolerations, envs);

		if (optReservation.isPresent()) {

			ContainerOrchestratorReservationResult reservation = optReservation.get();

			logger.info("Releasing reserved resources {}", reservation.getTaskArn());

			release(reservation);
		} else {
			logger.info("No reservation to release for {}", runid);
		}

		V1Job kubeJob = modelJob.getJob();

		V1Job responseJob = getBatchApi().createNamespacedJob(JOBS_NS, kubeJob, null, null, null);

		KubernetesJobManager jobManager = createJobManager(maxRetries);

		logger.info(REQUESTED_RUN_IMAGE, image.getImage());

		//TODO  I probably need to provide pod id?  status.setModelTaskId(getTaskId(task))

		return new SubmitJobResult(jobManager, kubeJob, responseJob);

	}

	SubmitJobResult submitJob(VLabDockerImage image, Optional<ContainerOrchestratorReservationResult> optReservation, String runFolder,
			String runid, Optional<String> nodeSelector, String nodeTolerations, Integer maxRetries) throws ApiException, BPException {

		return submitJob(image, optReservation, runFolder, runid, nodeSelector, nodeTolerations, maxRetries, new HashMap<>());
	}

	@Override
	public ContainerOrchestratorCommandResult removeDirectory(String folder, Long maxWait) {

		String command = "rm -rf " + folder;

		logger.trace("Remove directory command {}", command);

		return submitLocalCmdAndGetResult(command, "Can't remove requested directories");

	}

	public IWebStorage createIWebStorageClient(String bucketName) throws BPException {
		return new StorageFactory().getWebStorage(bucketName);
	}

	@Override
	public ContainerOrchestratorCommandResult saveFileToWebStorage(String filePath, String bucketName, String s3ObjectKey,
			Boolean publicread, Long maxwait) {

		logger.debug("Trying to save locally, file path: {} and object key {} in bucket {}", filePath, s3ObjectKey, bucketName);
		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		try {
			IWebStorage webStorage = createIWebStorageClient(bucketName);

			WebStorageObject o = webStorage.upload(new File(filePath), s3ObjectKey);

			logger.debug("Saved file {} to s3://{}/{}", filePath, bucketName, o.getKey());

			result.setSuccess(true);

		} catch (BPException e) {

			BPExceptionLogger.logBPException(e, logger, Level.WARN);

			result.setMessage(
					"BPException trying to upload file " + filePath + " to bucket " + bucketName + " with key " + s3ObjectKey + " with "
							+ "error " + e.getMessage());
			result.setSuccess(false);
		}

		return result;
	}

	private ContainerOrchestratorCommandResult save2S3(String command, Long maxwait) {

		var job = new VLabAwsCliJob(computeInfrastructure.getS3AccessKey(), computeInfrastructure.getS3SecretKey(),
				computeInfrastructure.getAwsCliImage());

		job.setCommand(command);

		job.setNodeSelector(Optional.ofNullable(computeInfrastructure.getControllerNodeSelector()));
		job.setNodeTolerations(computeInfrastructure.getControllerNodeTolerations());
		job.setVlabPV(computeInfrastructure.getVlabPv());
		job.setVlabPvClaim(computeInfrastructure.getVlabPvClaim());
		job.setRunFolder(vlabRootFolder);

		V1Job kubeJob = job.getJob();

		return submitToControllerAndGetResult(kubeJob, maxwait, "Failed saving files ");

	}

	@Override
	public ContainerOrchestratorCommandResult saveFolderToWebStorage(String folderPath, String bucketName, String s3BaseObjectKey,
			Boolean publicread, Long maxwait) {

		String command = BPUtils.createS3UploadFolderCommand(folderPath, bucketName, s3BaseObjectKey, publicread,
				computeInfrastructure.getS3ServiceUrl());

		logger.info("Saving output folder with command {}", command);

		return save2S3(command, maxwait);
	}

	@Override
	public ContainerOrchestratorReservationResult reserveResources(VLabDockerResources resources) throws BPException {

		logger.info("Reserving resources: memory --> {}, cpu --> {}", resources.getMemory_mb(), resources.getCpu_units());

		String command = BPUtils.createReserveCommand();

		var job = new VLabPrepeareJob(computeInfrastructure.getPrepareImage());

		job.setCommand(command);

		job.setNodeSelector(Optional.ofNullable(computeInfrastructure.getExecutorNodeSelector()));
		job.setNodeTolerations(computeInfrastructure.getExecutorNodeTolerations());

		String requiredMem = resources.getMemory_mb();

		if (requiredMem != null && !requiredMem.equalsIgnoreCase(""))
			job.setMemoryReservation(Integer.valueOf(requiredMem));

		String requiredCpu = resources.getCpu_units();

		if (requiredCpu != null && !requiredCpu.equalsIgnoreCase(""))
			job.setCpu(Integer.valueOf(requiredCpu));
		job.setVlabPV(computeInfrastructure.getVlabPv());

		job.setVlabPvClaim(computeInfrastructure.getVlabPvClaim());

		job.setRunFolder(vlabRootFolder);

		V1Job kubeJob = job.getJob();

		V1Job responseJob = null;

		try {

			responseJob = getBatchApi().createNamespacedJob(JOBS_NS, kubeJob, null, null, null);

		} catch (ApiException e) {

			logger.error("Api err code {} and body {}", e.getCode(), e.getResponseBody());
			logger.error(CANT_SUBMIT_JOB, kubeJob.getMetadata().getName(), e);

			var result = new ContainerOrchestratorReservationResult();

			result.setAcquired(false);

			result.setMessage("Failed to create job for reserving resources " + e.getMessage());

			return result;

		}

		logger.debug("Requested runTask to reserve resources [{} MB, {} CPU]", resources.getMemory_mb(), resources.getCpu_units());

		KubernetesJobManager jobManager = createJobManager(0);

		ContainerOrchestratorReservationResult result = jobManager.getReservationResult(responseJob);

		logger.info("Reserving resources [{} MB, {} CPU] completed with result: {}", resources.getMemory_mb(), resources.getCpu_units(),
				result.isAcquired());

		if (!result.isAcquired()) {
			logger.debug("Failed acquiring, requesting job deletion");

			release(result);

			logger.trace("Job deleted with success, setting arn to null");

			result.setTaskArn(null);
		}

		return result;

	}

	KubernetesPodChecker createPodChecker() {
		return new KubernetesPodChecker();
	}

	@Override
	public void release(ContainerOrchestratorReservationResult reservation) throws BPException {

		if (Boolean.FALSE.equals(reservation.isAcquired())) {

			if (reservation.getTaskArn() != null) {
				logger.warn("Reservation acquired is false, task arn is expceted to be null but is {}, trying to release it",
						reservation.getTaskArn());
			} else {

				logger.info("Reservation acquired is false, returning");
				return;
			}
		}

		logger.debug("Releasing job {}", reservation.getTaskArn());

		var body = new V1DeleteOptions();
		body.setPropagationPolicy(BACKGROUND_PROPAGATION_POLICY);

		try {

			getBatchApi().deleteNamespacedJob(reservation.getTaskArn(), JOBS_NS, null, null, 0, null, null, body);

		} catch (ApiException e) {

			logger.error("Can't release job {}", reservation.getTaskArn(), e);

			throw new BPException("Unable to stop job " + reservation.getTaskArn(), BPException.ERROR_CODES.RESOURCES_RELEASE_ERROR);

		}

		KubernetesPodChecker podChecker = createPodChecker();

		podChecker.setCoreApi(getCoreApi());

		podChecker.waitPodTermination(reservation.getTaskArn());

	}

	private ContainerOrchestratorCommandResult doAppendParamTo(String cmd, String targetFile, String kv) {

		logger.debug("Local command for appending {} to {} with command {}", kv, targetFile, cmd);

		String err = "Can't append vlab param " + kv;

		return submitLocalCmdAndGetResult(cmd, err);

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

		return doAppendParamTo(cmd, targetFile, key + "=" + value);

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

			return doAppendParamTo(cmd, targetFile, key + "=" + BPUtils.toText(value));

		} catch (BPException e) {

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);
			result.setMessage(e.getMessage());

			return result;

		}

	}

	public ContainerOrchestratorCommandResult execCmd(String cmd, String localErr, String localMsgPrefix) {

		logger.debug("Executing {} in local mode", cmd);

		ContainerOrchestratorCommandResult r = submitLocalCmdAndGetResult(cmd, localErr);

		if (!r.isSuccess())

			r.setMessage(localMsgPrefix + r.getMessage());

		return r;

	}

	@Override
	public ContainerOrchestratorCommandResult move(String source, String target, Long maxwait) {

		logger.trace("Requested move command {} to {}", source, target);

		String cmd = BPUtils.createMoveCmd(source, target);

		logger.trace("Move command {} to {} is: {}", source, target, cmd);

		return execCmd(cmd, "Can't move " + source + " to " + target, "");

	}

	@Override
	public BPLogChunk readLogChunk(String taskId, Optional<String> containerName, Optional<Integer> sinceSeconds) throws BPException {

		try {
			String text = getCoreApi().readNamespacedPodLog(taskId, JOBS_NS, containerName.orElse(null), Boolean.FALSE, Boolean.TRUE, null,
					"true", Boolean.FALSE, sinceSeconds.orElse(null), null, Boolean.TRUE);

			return parse(text);

		} catch (ApiException e) {

			throw new BPException("Kubernetes API exception reading logs of task id " + taskId + " with message " + e.getMessage(),
					BPException.ERROR_CODES.TASK_LOG_READ_ERROR);
		}
	}

	private BPLogChunk parse(String text) {

		return new KubernetesLogParser().parse(text);
	}

	public BatchV1Api getBatchApi() {
		return bApi;
	}

	public CoreV1Api getCoreApi() {
		return cApi;
	}

	public AppsV1Api getAppApi() {
		return aApi;
	}

}
