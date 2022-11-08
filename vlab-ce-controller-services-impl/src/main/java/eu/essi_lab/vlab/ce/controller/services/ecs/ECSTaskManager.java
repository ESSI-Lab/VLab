package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;

/**
 * @author Mattia Santoro
 */
public class ECSTaskManager {

	protected ECSTaskFailureHandler handler;
	private Logger logger = LogManager.getLogger(ECSTaskManager.class);
	private final EcsClient client;
	private final RunTaskRequest request;
	private ECSTaskStatusChecker checker;

	public ECSTaskManager(EcsClient ecsclient, RunTaskRequest runTaskRequest) {

		client = ecsclient;

		request = runTaskRequest;

		handler = new ECSTaskFailureHandler(client);
		checker = new ECSTaskStatusChecker(client);
	}

	public ContainerOrchestratorSubmitResult waitStart(RunTaskResponse runTaskResult, Long maxWait, String cluster, Integer count) {

		logger.info("Requested task started of {}", runTaskResult);

		logger.trace("Started waiting execution of task");

		ECSTaskCheckResponse response = checker.taskStartedWithSuccess(runTaskResult, maxWait, cluster);

		logger.debug("Completed waiting execution of task");

		if (!response.isSuccess()) {

			logger.warn("Failed wait start, handling failure");

			return handler.handleWaitStartFailure(runTaskResult, this, count, cluster, maxWait, response);

		}

		ContainerOrchestratorSubmitResult result = new ContainerOrchestratorSubmitResult();

		result.setSubmissionSuccess(response.isSuccess());

		result.setIdentifier(runTaskResult.tasks().get(0).taskArn());

		logger.info("Task {} started with result {}", result.getIdentifier(), result.submissionSuccess());

		return result;

	}

	public ContainerOrchestratorCommandResult getTaskResult(RunTaskResponse runTaskResult, Long maxWait, String cluster, int count) {

		logger.trace("Requested task result of {}", runTaskResult);

		logger.trace("Started waiting completion of task");

		ECSTaskCheckResponse response = checker.taskCompletedWithSuccess(runTaskResult, maxWait, cluster);

		if (!response.isSuccess()) {

			logger.warn("Task failed");

			return handler.handleFailure(runTaskResult, this, count, cluster, maxWait, request);
		}

		logger.info("Task succeded");

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		result.setSuccess(true);

		return result;

	}

	public ContainerOrchestratorReservationResult getReservationResult(RunTaskResponse task, String cluster) {
		logger.debug("Generating reservation task result");

		logger.trace("Started waiting completion of task");

		boolean success = checker.reservationTaskRunning(task, cluster);

		ContainerOrchestratorReservationResult result = new ContainerOrchestratorReservationResult();

		if (!success) {
			result.setAcquired(false);

			return result;
		}

		result.setAcquired(true);

		result.setTaskArn(task.tasks().get(0).taskArn());

		return result;

	}

	public ContainerOrchestratorCommandResult getTaskResult(RunTaskResponse task, Long maxWait, String cluster) {

		return getTaskResult(task, maxWait, cluster, 0);

	}

	public void setChecker(ECSTaskStatusChecker checker) {
		this.checker = checker;
	}

	public void setFailureHandler(ECSTaskFailureHandler handler) {
		this.handler = handler;
	}
}
