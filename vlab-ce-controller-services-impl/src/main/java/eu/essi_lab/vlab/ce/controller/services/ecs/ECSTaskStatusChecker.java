package eu.essi_lab.vlab.ce.controller.services.ecs;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Container;
import software.amazon.awssdk.services.ecs.model.DescribeTasksRequest;
import software.amazon.awssdk.services.ecs.model.DescribeTasksResponse;
import software.amazon.awssdk.services.ecs.model.Failure;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * @author Mattia Santoro
 */
public class ECSTaskStatusChecker {

	private Logger logger = LogManager.getLogger(ECSTaskStatusChecker.class);
	private final EcsClient client;

	private static final String RETURNING_FALSE = ", returning false";
	private static final String INTERRUPTED_EX = "Interrupted exception requesting status of ";
	private static final String STATUS_OF_TASK = "Status of task ";
	private static final String NO_TASK_RETURNED = "No task returned by description of ";

	private List<Task> lastTaskStatuses;
	private int maxTriesToGetStatus = 20;
	private Long waitInterval = 2000L;
	private int maxtestiterations = 1000;
	private boolean undertest = false;

	public ECSTaskStatusChecker(EcsClient ecsClient) {
		client = ecsClient;
	}

	protected ECSTASK_STATUS status(String arn, String cluster, Long maxwait, Long expiredMillis) {

		logger.trace("Check timeout: max wait is {} millis, expired is {} miliis", maxwait, expiredMillis);

		if (expiredMillis >= maxwait) {

			logger.trace("Max time exceeded for task {}: [{}>={}], returning {}", arn, expiredMillis, maxwait, ECSTASK_STATUS.TIMEDOUT);

			return ECSTASK_STATUS.TIMEDOUT;
		}

		logger.trace("Requesting task description of {} to cluster {}", arn, cluster);

		DescribeTasksResponse status = getTaskStatus(arn, cluster);

		if (status == null) {

			logger.trace("Null status, {}{}{}", NO_TASK_RETURNED, arn, RETURNING_FALSE);

			return ECSTASK_STATUS.NOSTATUS;
		}

		List<Task> stasks = status.tasks();

		if (stasks == null || stasks.isEmpty()) {

			logger.trace("{}{}{}", NO_TASK_RETURNED, arn, RETURNING_FALSE);

			return ECSTASK_STATUS.NOSTATUS;

		}

		String stat = stasks.get(0).lastStatus();

		logger.trace("{}{} is {}", STATUS_OF_TASK, arn, stat);

		if (stat.equalsIgnoreCase("STOPPED")) {

			lastTaskStatuses = stasks;

			return ECSTASK_STATUS.STOPPED;
		}

		if (stat.equalsIgnoreCase("PENDING")) {

			return ECSTASK_STATUS.PENDING;
		}

		return ECSTASK_STATUS.RUNNING;
	}

	private ECSTaskCheckResponse checkECSTask(Task task, String cluster, long maxWaitMillis, long startingmillis, Boolean waitCompleted) {

		var response = new ECSTaskCheckResponse();

		String taskArn = task.taskArn();

		ECSTASK_STATUS ecsStatus = status(taskArn, cluster, maxWaitMillis, startingmillis);

		var iteration = 1;

		long exipredmillis = startingmillis;

		while (ecsStatus.equals(ECSTASK_STATUS.RUNNING) || ecsStatus.equals(ECSTASK_STATUS.PENDING)) {

			logger.trace("Iterations: {}", iteration);

			logger.trace("Task {} is {}", taskArn, ecsStatus);

			if (ecsStatus.equals(ECSTASK_STATUS.RUNNING) && Boolean.FALSE.equals(waitCompleted)) {

				logger.trace("WaitCompleted is false and task is running, return true");

				response.setLastStatus(ecsStatus);
				response.setSuccess(true);
				return response;
			}

			logger.trace("Wait and call status method");
			try {
				Thread.sleep(waitInterval);

				exipredmillis = exipredmillis + waitInterval;

			} catch (InterruptedException e) {

				logger.error(INTERRUPTED_EX + taskArn, e);
				//This is suggested by Sonar as best practice
				Thread.currentThread().interrupt();
			}

			logger.trace("Calling status for task {}", taskArn);

			ecsStatus = status(taskArn, cluster, maxWaitMillis, exipredmillis);

			if (testing(iteration))
				ecsStatus = ECSTASK_STATUS.STOPPED;

			iteration++;

		}

		response.setLastStatus(ecsStatus);

		switch (ecsStatus) {
		case TIMEDOUT:
			logger.trace("Task {} timed out", taskArn);

			response.setSuccess(false);
			return response;

		case NOSTATUS:

			response.setSuccess(false);
			return response;

		case RUNNING:
		case PENDING:

			logger.trace("Task {} is {}, this should not happen", ecsStatus, taskArn);

			response.setSuccess(false);
			return response;

		case STOPPED:
			logger.trace("Task {} is stopped", taskArn);

			break;

		}

		response.setSuccess(exitCodeSuccess());
		return response;

	}

	protected boolean exitCodeSuccess() {
		logger.trace("Looking for exit code");

		List<Container> containers = lastTaskStatuses.get(0).containers();

		if (containers != null && !containers.isEmpty()) {

			Integer exit = containers.get(0).exitCode();

			logger.trace("Exit code: {}", exit);

			if (exit == null) {
				logger.error("Found null exit code");
				return false;
			}

			if (exit - 0 == 0) {
				logger.trace("Exit code 0 [{}]", exit);
				return true;
			}

			logger.trace("Exit code != 0 [{}]", exit);
			return false;
		} else {
			logger.trace("No container from description, returning false");
			return false;
		}
	}

	private boolean testing(int iterations) {
		return undertest && iterations > maxtestiterations;
	}

	private boolean taskFailure(RunTaskResponse taskResult) {
		List<Failure> failures = taskResult.failures();

		return (failures != null && !failures.isEmpty());

	}

	public ECSTaskCheckResponse taskStartedWithSuccess(RunTaskResponse taskResult, Long maxWait, String cluster) {
		return waitTask(taskResult, maxWait, cluster, false);
	}

	private ECSTaskCheckResponse waitTask(RunTaskResponse taskResult, Long maxWaitMillis, String cluster, Boolean waitCompleted) {

		var response = new ECSTaskCheckResponse();

		if (taskFailure(taskResult)) {

			logger.trace("Found task failure, returning false");

			response.setLastStatus(ECSTASK_STATUS.STOPPED);

			response.setSuccess(false);

			return response;
		}

		List<Task> tasks = taskResult.tasks();

		if (tasks != null && !tasks.isEmpty()) {

			var ecsTask = tasks.get(0);

			return checkECSTask(ecsTask, cluster, maxWaitMillis, 0L, waitCompleted);

		} else {

			logger.warn("No task to interrogate was found, returning false");

			response.setLastStatus(ECSTASK_STATUS.NOSTATUS);

			response.setSuccess(false);

			return response;
		}
	}

	public ECSTaskCheckResponse taskCompletedWithSuccess(RunTaskResponse taskResult, Long maxWaitMillis, String cluster) {

		return waitTask(taskResult, maxWaitMillis, cluster, true);

	}

	private DescribeTasksResponse getTaskStatus(String taskArn, String cluster) {
		var tries = 1;

		DescribeTasksResponse status = client.describeTasks(DescribeTasksRequest.builder().cluster(cluster).tasks(taskArn).build());

		List<Task> stasks = status.tasks();

		while ((stasks == null || stasks.isEmpty()) && tries < maxTriesToGetStatus) {

			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e) {

				logger.error(INTERRUPTED_EX + taskArn, e);
				//This is suggested by Sonar as best practice
				Thread.currentThread().interrupt();
			}

			tries++;

			status = client.describeTasks(DescribeTasksRequest.builder().cluster(cluster).tasks(taskArn).build());

			if (status != null)
				stasks = status.tasks();

		}

		if ((stasks == null || stasks.isEmpty())) {
			logger.warn("Unable to get status of {} after {} tries, returning NULL status", taskArn, tries);
		}

		return status;

	}

	public boolean reservationTaskRunning(RunTaskResponse taskResult, String cluster) {

		if (taskFailure(taskResult)) {

			logger.trace("Found task failure, returning false");

			return false;
		}

		List<Task> tasks = taskResult.tasks();

		if (tasks != null && !tasks.isEmpty()) {

			var ecsTask = tasks.get(0);

			String arn = ecsTask.taskArn();

			ECSTASK_STATUS taskStatus = status(arn, cluster, 1000L, 0L);

			switch (taskStatus) {
			case RUNNING:
				logger.trace("Reservation task is running with arn {}", arn);
				return true;
			case PENDING:
				logger.trace("Reservation task is pending with arn {}, I wait and call recursion", arn);

				try {
					Thread.sleep(10000L);

				} catch (InterruptedException e) {

					logger.error(INTERRUPTED_EX + arn, e);
					//This is suggested by Sonar as best practice
					Thread.currentThread().interrupt();
				}

				logger.trace("Calling recursion on status for reservation task {}", arn);
				return reservationTaskRunning(taskResult, cluster);

			default:
				logger.warn("Unexpected status for reservationtask with arn {} --> status {}", arn, taskStatus);
				return false;

			}

		} else {

			logger.trace("No reservation task to interrogate was found, returning false");
			return false;

		}

	}

	public int getMaxTriesToGetStatus() {
		return maxTriesToGetStatus;
	}

	public void setMaxTriesToGetStatus(int maxTriesToGetStatus) {
		this.maxTriesToGetStatus = maxTriesToGetStatus;
	}

	public void setWaitInterval(Long waitInterval) {
		this.waitInterval = waitInterval;
	}

	public void setUndertest(boolean undertest) {
		this.undertest = undertest;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxtestiterations = maxIterations;
	}

}
