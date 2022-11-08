package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Container;
import software.amazon.awssdk.services.ecs.model.DescribeTasksRequest;
import software.amazon.awssdk.services.ecs.model.DescribeTasksResponse;
import software.amazon.awssdk.services.ecs.model.Failure;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.StopTaskRequest;
import software.amazon.awssdk.services.ecs.model.StopTaskResponse;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * @author Mattia Santoro
 */
public class ECSTaskFailureHandler {

	static final String TASK_FAILURE_WITH_CAUSE = "Task failure with cause: ";
	static final String UNKNOWN_FAILURE_CAUSE = "Unknown Failure Cause";
	static final String AND_EXIT_CODE = " and exit code: ";
	private Logger logger = LogManager.getLogger(ECSTaskFailureHandler.class);
	private final EcsClient client;
	private int maxRetries = 10;
	private static final String INTERRUPTED_EX = "Interrupted exception waiting to retry an agent-failed task";
	private Long retryInterval = 30000L;

	public ECSTaskFailureHandler(EcsClient ecsClient) {
		client = ecsClient;

	}

	private boolean isAgentFailure(RunTaskResponse runTaskResult, String cluster) {

		return getFailureCause(runTaskResult, cluster).equalsIgnoreCase("agent");
	}

	private String getContainerFailureCause(String cluster, RunTaskResponse runTaskResult) {
		logger.trace("Searching for container failure causes");

		if (runTaskResult.tasks().isEmpty()) {

			logger.trace("Task list is empty");
			return UNKNOWN_FAILURE_CAUSE;

		}

		String arn = runTaskResult.tasks().get(0).taskArn();

		logger.trace("Found task arn {}, searching for container failure causes", arn);

		DescribeTasksResponse status = client.describeTasks(DescribeTasksRequest.builder().cluster(cluster).tasks(arn).build());

		List<Task> stasks = status.tasks();

		Integer foundExitCode = null;

		if (stasks != null && !stasks.isEmpty()) {

			logger.trace("Found non empty list of tasks from task description");

			List<Container> containers = stasks.get(0).containers();

			if (containers != null && !containers.isEmpty()) {

				logger.trace("Found non empty list of containers from task description");

				String reason = containers.get(0).reason();

				foundExitCode = containers.get(0).exitCode();

				if (reason != null) {
					logger.trace("Found container failure cause of task {}: {}", arn, reason);

					return reason + AND_EXIT_CODE + containers.get(0).exitCode();
				}

			} else {
				logger.trace("Found empty list of containers from task description");
			}
		} else {

			logger.trace("Found empty list of tasks from task description");

		}

		var outexit = "";
		if (foundExitCode != null) {
			outexit = AND_EXIT_CODE + foundExitCode;
		}

		return UNKNOWN_FAILURE_CAUSE + outexit;
	}

	private String getFailureCause(RunTaskResponse runTaskResult, String cluster) {

		logger.trace("Searching for task failures");

		List<Failure> failures = runTaskResult.failures();

		if (failures != null && !failures.isEmpty()) {

			String failureCause = failures.get(0).reason();

			if (failureCause != null) {

				logger.trace("Found task failure cause {}", failureCause);

				return failureCause;
			}
		}

		logger.trace("Task failures not found");

		return getContainerFailureCause(cluster, runTaskResult);

	}

	public ContainerOrchestratorCommandResult handleFailure(RunTaskResponse runTaskResult, ECSTaskManager manager, int count,
			String cluster, long maxWait, RunTaskRequest request) {

		if (isAgentFailure(runTaskResult, cluster) && count < maxRetries) {

			logger.trace("Failure n. {} --> Now Sleep and Retrying", count);

			try {

				Thread.sleep(retryInterval);

			} catch (InterruptedException e) {

				logger.error(INTERRUPTED_EX, e);
				//This is suggested by Sonar as best practice
				Thread.currentThread().interrupt();
			}

			logger.trace("Failure n. {} --> Triggering Retry", count);

			RunTaskResponse tr = client.runTask(request);

			return manager.getTaskResult(tr, maxWait, cluster, count + 1);

		}

		logger.trace("Failure n. {} --> Now FAILING", count);

		String msg = TASK_FAILURE_WITH_CAUSE + getFailureCause(runTaskResult, cluster);

		var result = new ContainerOrchestratorCommandResult();

		result.setMessage(msg);

		result.setSuccess(false);

		return result;
	}

	public ContainerOrchestratorSubmitResult handleWaitStartFailure(RunTaskResponse runTaskResult, ECSTaskManager manager, Integer count,
			String cluster, Long maxWait, ECSTaskCheckResponse response) {

		//TODO handle failure, maybe retry or kill
		ECSTASK_STATUS s = response.getLastStatus();

		String arn = runTaskResult.tasks().get(0).taskArn();
		logger.debug("Handling wait start failure with status {} and count {} and max retries {}", s, count, maxRetries);

		switch (s) {
		case PENDING:
		case TIMEDOUT:

			if (count < maxRetries) {
				logger.debug("Waiting more time for {}", arn);
				return manager.waitStart(runTaskResult, maxWait, cluster, count + 1);

			}

			logger.debug("Count {} >= {} --> STOP", count, maxRetries);

			stopTask(arn, cluster);
			break;

		case NOSTATUS:

			stopTask(arn, cluster);

			break;
		case STOPPED:

			//TODO here I might try to re-launch?

		case RUNNING:
			logger.warn("Found failure waiting for start, but status is {}, something is wrong", s);
		}

		var result = new ContainerOrchestratorSubmitResult();

		result.setSubmissionSuccess(false);

		return result;
	}

	private void stopTask(String arn, String cluster) {

		logger.debug("Requesting to stop task {}", arn);

		StopTaskResponse stopResponse = client.stopTask(StopTaskRequest.builder().cluster(cluster).task(arn).build());

		if (logger.isInfoEnabled())
			logger.info("Stop task response, last status {}", stopResponse.task().lastStatus());

	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setRetryInterval(Long retryInterval) {
		this.retryInterval = retryInterval;
	}
}
