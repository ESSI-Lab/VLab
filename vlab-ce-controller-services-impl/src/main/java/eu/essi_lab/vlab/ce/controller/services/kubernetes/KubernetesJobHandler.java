package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTASK_STATUS;
import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTaskCheckResponse;
import static eu.essi_lab.vlab.ce.controller.services.kubernetes.KubernetesClient.JOBS_NS;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1Status;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobHandler {

	private final Logger logger = LogManager.getLogger(KubernetesJobHandler.class);
	private final BatchV1Api api;
	private int maxRetries = 10;

	private static final String INTERRUPTED_EX = "Interrupted exception waiting to retry an agent-failed task";
	private Long retryInterval = 10000L;

	private final List<String> retryableContainerNames = Arrays.asList("awsclicontainer", "preparejobcontainer");

	public KubernetesJobHandler(BatchV1Api api) {

		this.api = api;
	}

	public ContainerOrchestratorCommandResult handleFailure(V1Job job, KubernetesJobManager jobManager, int count, Long maxWait) {

		boolean retryableJob = isRetryableJob(job);

		boolean maxRetriesNotReached = count < maxRetries;

		if (retryableJob && maxRetriesNotReached) {

			logger.trace("Failure n. {} --> Now Sleep and Retrying", count);

			try {

				Thread.sleep(retryInterval);

			} catch (InterruptedException e) {

				logger.error(INTERRUPTED_EX, e);
				//This is suggested by Sonar as best practice
				Thread.currentThread().interrupt();
			}

			var newid = UUID.randomUUID().toString();

			logger.trace("Failure n. {} --> Triggering Retry with id {}", count, newid);

			V1Job responseJob;

			try {

				job.getMetadata().setName(newid);
				responseJob = api.createNamespacedJob(JOBS_NS, job, null, null, null);

			} catch (ApiException e) {

				logger.error("Can't submit job {}", job.getMetadata().getName(), e);

				var result = new ContainerOrchestratorCommandResult();

				result.setSuccess(false);

				result.setMessage("Failed re-submitting job " + job.getMetadata().getName() + " with message " + e.getMessage());

				return result;

			}

			return jobManager.getJobResult(job, responseJob, maxWait, count + 1);

		}

		logger.trace("Failure n. {} [isRetryable: {} - max retries reached: {}] --> Now FAILING", count, retryableJob,
				!maxRetriesNotReached);

		String msg = "Task Failed [dev: job " + job.getMetadata().getName() + "]";

		var result = new ContainerOrchestratorCommandResult();

		result.setMessage(msg);

		result.setSuccess(false);

		return result;
	}

	Optional<String> findContainerName(V1Job job) {

		try {

			return Optional.ofNullable(job.getSpec().getTemplate().getSpec().getContainers().get(0).getName());
		} catch (NullPointerException e) {

			logger.warn("Null pointer looking for container name of a job", e);

		}

		return Optional.empty();

	}

	public boolean isRetryableJob(V1Job job) {

		String containerName = findContainerName(job).orElse("non-retryable");

		return retryableContainerNames.contains(containerName);

	}

	public List<String> getRetryableContainerNames() {
		return retryableContainerNames;
	}

	public void setRetryInterval(long interval) {
		retryInterval = interval;
	}

	public void setMaxRetries(int tries) {
		maxRetries = tries;
	}

	public ContainerOrchestratorSubmitResult handleWaitStartFailure(V1Job job, KubernetesJobManager kubernetesJobManager, Integer count,
			Long maxwait, ECSTaskCheckResponse response) throws BPException {

		ECSTASK_STATUS s = response.getLastStatus();

		String arn = job.getMetadata().getName();
		logger.debug("Handling wait start failure with status {} and count {}", s, count);

		switch (s) {
		case PENDING:
		case TIMEDOUT:

			if (count < maxRetries) {
				logger.debug("Waiting more time for {}", arn);
				return kubernetesJobManager.waitStart(job, maxwait, count);

			}

			logger.debug("Count {} >= {} --> STOP", count, maxRetries);

			tryStopJob(arn);
			break;

		case NOSTATUS:

			tryStopJob(arn);
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

	private void tryStopJob(String jobname) throws BPException {

		logger.debug("Requesting to stop task {}", jobname);

		V1DeleteOptions body = new V1DeleteOptions().propagationPolicy("Background");
		V1Status st;

		try {

			st = api.deleteNamespacedJob(jobname, JOBS_NS, null, null, 0, null, null, body);

		} catch (ApiException e) {

			logger.error("Can't delete job {}", jobname, e);

			throw new BPException("Unable to stop job " + jobname, BPException.ERROR_CODES.MOVED_CONTROLLER_STOP_ERROR);

		}

		logger.info("Stop task response, last status code {}", st.getCode());
	}
}
