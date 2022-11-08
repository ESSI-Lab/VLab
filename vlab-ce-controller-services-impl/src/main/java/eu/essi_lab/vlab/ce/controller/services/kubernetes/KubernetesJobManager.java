package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTaskCheckResponse;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobManager {

	protected KubernetesJobHandler handler;
	private final BatchV1Api api;
	private final Logger logger = LogManager.getLogger(KubernetesJobManager.class);
	private KubernetesJobChecker checker;
	private final CoreV1Api coreApi;

	public KubernetesJobManager(BatchV1Api batchApi, CoreV1Api core) {

		api = batchApi;

		coreApi = core;

		checker = new KubernetesJobChecker(api);

		handler = new KubernetesJobHandler(api);

	}

	public Optional<String> getPodId(V1Job job) {
		checker.setCoreApi(coreApi);
		return checker.getPodChecker().podIdentifier(job);
	}

	public ContainerOrchestratorCommandResult getJobResult(V1Job originalJob, V1Job job, Long maxWait, Integer count) {

		logger.trace("Requested task result of {}", job.getMetadata().getName());

		logger.trace("Started waiting completion of task");

		checker.setCoreApi(coreApi);

		boolean success = checker.jobCompletedWithSuccess(job, maxWait);

		if (!success) {

			logger.warn("Task failed");

			return handler.handleFailure(originalJob, this, count, maxWait);
		}

		logger.info("Task succeded");

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		result.setSuccess(true);

		return result;

	}

	public ContainerOrchestratorReservationResult getReservationResult(V1Job responseJob) {
		logger.debug("Generating reservation task result");

		logger.trace("Started waiting completion of task");

		checker.setCoreApi(coreApi);

		boolean success = checker.reservationTaskRunning(responseJob);

		ContainerOrchestratorReservationResult result = new ContainerOrchestratorReservationResult();

		if (!success) {

			result.setAcquired(false);

			result.setTaskArn(responseJob.getMetadata().getName());

			return result;
		}

		result.setAcquired(true);

		result.setTaskArn(responseJob.getMetadata().getName());

		return result;

	}

	public void setChecker(KubernetesJobChecker checker) {
		this.checker = checker;
	}

	public void setFailureHandler(KubernetesJobHandler handler) {
		this.handler = handler;
	}

	public ContainerOrchestratorSubmitResult waitStart(V1Job job, Long maxwait, Integer count) throws BPException {
		logger.info("Requested task started of {}", job.getMetadata().getName());

		logger.trace("Started waiting execution of task");

		checker.setCoreApi(coreApi);

		ECSTaskCheckResponse response = checker.taskStartedWithSuccess(job, maxwait);

		logger.debug("Completed waiting execution of task");

		if (!response.isSuccess()) {

			logger.warn("Failed wait start, handling failure");

			return handler.handleWaitStartFailure(job, this, count, maxwait, response);

		}

		ContainerOrchestratorSubmitResult result = new ContainerOrchestratorSubmitResult();

		result.setSubmissionSuccess(response.isSuccess());

		result.setIdentifier(job.getMetadata().getName());

		logger.info("Task {} started with result {}", result.getIdentifier(), result.submissionSuccess());

		return result;
	}
}
