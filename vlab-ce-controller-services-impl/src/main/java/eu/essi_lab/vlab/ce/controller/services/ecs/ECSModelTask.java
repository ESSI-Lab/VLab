package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.engine.utils.BPUtils;
import static eu.essi_lab.vlab.core.engine.utils.BPUtils.BPENGINE_RUNID_KEY;
import static eu.essi_lab.vlab.core.engine.utils.BPUtils.VLAB_RUNID_KEY;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ContainerDefinition;
import software.amazon.awssdk.services.ecs.model.ContainerOverride;
import software.amazon.awssdk.services.ecs.model.DescribeTaskDefinitionRequest;
import software.amazon.awssdk.services.ecs.model.DescribeTaskDefinitionResponse;
import software.amazon.awssdk.services.ecs.model.HostVolumeProperties;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.ListTaskDefinitionsRequest;
import software.amazon.awssdk.services.ecs.model.ListTaskDefinitionsResponse;
import software.amazon.awssdk.services.ecs.model.LogConfiguration;
import software.amazon.awssdk.services.ecs.model.MountPoint;
import software.amazon.awssdk.services.ecs.model.NetworkMode;
import software.amazon.awssdk.services.ecs.model.RegisterTaskDefinitionRequest;
import software.amazon.awssdk.services.ecs.model.RegisterTaskDefinitionResponse;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.TaskDefinition;
import software.amazon.awssdk.services.ecs.model.TaskOverride;
import software.amazon.awssdk.services.ecs.model.Volume;

/**
 * @author Mattia Santoro
 */
public class ECSModelTask {

	public static final String LOG_REGION_KEY = "awslogs-region";

	private final EcsClient client;
	private String taskFamily;
	private static final String VLAB_EFS_FOLDER_NAME = "EFSRoot";
	private final String vlabEfsFolderPath;
	private String runFolder;

	private static final String AWSLOGDRIVER = "awslogs";
	private static final String LOG_GRUOP_KEY = "awslogs-group";
	private static final String LOG_STREAMPREFIX_KEY = "awslogs-stream-prefix";

	private Logger logger = LogManager.getLogger(ECSModelTask.class);

	private final String region;

	private final String taskContainerName;
	private final String logPrefix;
	private final String logGroup;
	private final String vlabRoot;

	public ECSModelTask(EcsClient client, String region, String efsFolder, String containerName, String lGroup, String logPrefix,
			String root) {

		this.client = client;
		this.region = region;
		this.vlabEfsFolderPath = efsFolder;
		this.taskContainerName = containerName;
		this.logPrefix = logPrefix;
		this.vlabRoot = root;
		this.logGroup = lGroup;

	}

	public RunTaskRequest createRunTaskRequest(VLabDockerImage dockerImage, String runid) throws BPException {

		var taskDefinition = findTaskDefinition(dockerImage);

		if (taskDefinition == null) {

			taskDefinition = createTaskDefinition(dockerImage);

			taskDefinition = storeTaskDefinition(taskDefinition);

		}

		ContainerDefinition container = taskDefinition.containerDefinitions().get(0);

		var containerOverride = ContainerOverride.builder().name(container.name()).build();

		String requiredMem = dockerImage.getResources().getMemory_mb();

		if (requiredMem != null && !requiredMem.equalsIgnoreCase(""))
			containerOverride = containerOverride.toBuilder().memoryReservation(Integer.valueOf(requiredMem)).build();

		String requiredCpu = dockerImage.getResources().getCpu_units();

		if (requiredCpu != null && !requiredCpu.equalsIgnoreCase(""))
			containerOverride = containerOverride.toBuilder().cpu(Integer.valueOf(requiredCpu)).build();

		List<String> commands = BPUtils.createCommand(dockerImage.getContainer(), runFolder);

		Collection<KeyValuePair> envVars = new ArrayList<>();

		KeyValuePair retroRunidkv = KeyValuePair.builder().name(BPENGINE_RUNID_KEY).value(runid).build();
		KeyValuePair runidkv = KeyValuePair.builder().name(VLAB_RUNID_KEY).value(runid).build();

		envVars.add(retroRunidkv);
		envVars.add(runidkv);

		containerOverride = containerOverride.toBuilder().environment(envVars).build();
		containerOverride = containerOverride.toBuilder().command(commands).build();

		return RunTaskRequest.builder().taskDefinition(taskDefinition.taskDefinitionArn()).overrides(
				TaskOverride.builder().containerOverrides(containerOverride).build()).build();

	}

	/**
	 * Creates a {@link TaskDefinition} which corresponds to the provided {@link VLabDockerImage}.
	 *
	 * @return the created {@link TaskDefinition}
	 */
	public TaskDefinition createTaskDefinition(VLabDockerImage dockerImage) {

		var taskBuider = TaskDefinition.builder();

		var containerDefinitionBuilder = ContainerDefinition.builder();

		containerDefinitionBuilder.image(dockerImage.getImage());

		if (dockerImage.getContainer() != null) {
			containerDefinitionBuilder.entryPoint(dockerImage.getContainer().getEntryPoint());
		}

		Map<String, String> logoptions = new HashMap<>();

		logoptions.put(LOG_GRUOP_KEY, logGroup);
		logoptions.put(LOG_REGION_KEY, region);
		logoptions.put(LOG_STREAMPREFIX_KEY, logPrefix);

		var logConfiguration = LogConfiguration.builder().logDriver(AWSLOGDRIVER).options(logoptions).build();

		containerDefinitionBuilder.logConfiguration(logConfiguration);

		return taskBuider.containerDefinitions(containerDefinitionBuilder.build()).build();

	}

	/**
	 * Stores the provided {@link TaskDefinition} to AWS ECS.
	 *
	 * @return the {@link TaskDefinition} returned by the registration process
	 */
	public TaskDefinition storeTaskDefinition(TaskDefinition task) throws BPException {

		if (taskFamily == null || "".equalsIgnoreCase(taskFamily))

			throw new BPException("Unable to register task definition without task family.", BPException.ERROR_CODES.NO_TASK_FAMILY);

		try {

			var taskContainerDefinition = task.containerDefinitions().get(0);

			var mountPoint = MountPoint.builder().sourceVolume(VLAB_EFS_FOLDER_NAME).containerPath(vlabRoot).build();

			taskContainerDefinition = taskContainerDefinition.toBuilder().mountPoints(mountPoint).build();

			taskContainerDefinition = taskContainerDefinition.toBuilder().name(taskContainerName).build();
			taskContainerDefinition = taskContainerDefinition.toBuilder().memoryReservation(128).build();

			var requestBuilder = RegisterTaskDefinitionRequest.builder().containerDefinitions(taskContainerDefinition).networkMode(
					NetworkMode.BRIDGE).family(taskFamily);

			if (task.executionRoleArn() != null)
				requestBuilder.executionRoleArn(task.executionRoleArn());

			requestBuilder.volumes(Volume.builder().name(VLAB_EFS_FOLDER_NAME)
					.host(HostVolumeProperties.builder().sourcePath(vlabEfsFolderPath).build()).build());

			logger.trace("Registering new model task definition");

			RegisterTaskDefinitionResponse response = client.registerTaskDefinition(requestBuilder.build());

			return response.taskDefinition();
		} catch (Exception thr) {
			throw new BPException("Unable to register task definition for the provided image with exception: " + thr.getMessage(),
					BPException.ERROR_CODES.TASK_REGISTRATION_FAIL);
		}
	}

	/**
	 * Finds an existing {@link TaskDefinition} which corresponds to the provided {@link VLabDockerImage} using the matcher {@link
	 * TaskDefinitionMatcher#match(VLabDockerImage)}.
	 *
	 * @return the existing {@link TaskDefinition} or null if no task definition matches the provided image.
	 */
	public TaskDefinition findTaskDefinition(VLabDockerImage dockerImage) {

		var builder = ListTaskDefinitionsRequest.builder();
		if (taskFamily != null)
			builder.familyPrefix(taskFamily);

		ListTaskDefinitionsRequest request = builder.build();
		ListTaskDefinitionsResponse response = client.listTaskDefinitions(request);

		String nextToken = response.nextToken();

		var task = findTaskDefinition(response, dockerImage);

		while (task == null && nextToken != null) {

			request = request.toBuilder().nextToken(nextToken).build();

			response = client.listTaskDefinitions(request);

			nextToken = response.nextToken();

			task = findTaskDefinition(response, dockerImage);

		}

		return task;
	}

	public TaskDefinitionMatcher createTaskMatcher(TaskDefinition taskDefinition) {
		return new TaskDefinitionMatcher(taskDefinition);
	}

	private TaskDefinition findTaskDefinition(ListTaskDefinitionsResponse listTaskDefinitionsResult, VLabDockerImage dockerImage) {

		for (String arn : listTaskDefinitionsResult.taskDefinitionArns()) {

			DescribeTaskDefinitionResponse response = client.describeTaskDefinition(
					DescribeTaskDefinitionRequest.builder().taskDefinition(arn).build());

			var taskDef = response.taskDefinition();

			if (logger.isTraceEnabled())
				logger.trace("Executing match with {}", taskDef.taskDefinitionArn());

			TaskDefinitionMatcher matcher = createTaskMatcher(taskDef);

			if (matcher.match(dockerImage))
				return taskDef;

		}

		return null;
	}

	public String getTaskFamily() {
		return taskFamily;
	}

	public void setTaskFamily(String taskFamily) {
		this.taskFamily = taskFamily;
	}

	public void setRunFolder(String folder) {
		this.runFolder = folder;
	}
}
