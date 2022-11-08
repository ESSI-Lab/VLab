package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTASK_STATUS;
import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTaskCheckResponse;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobStatus;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobChecker {

	private Logger logger = LogManager.getLogger(KubernetesJobChecker.class);

	private final BatchV1Api api;

	private Long retrieveJobStatusWaitIntervalMilliSeconds = 2000L;
	private static final String INTERRUPTED_EX = "Interrupted exception requesting status of ";

	private CoreV1Api coreApi;
	private Integer maxRetry = 5;
	private Long jobStatusRetryIntervalMilliSeconds = 5000L;

	public KubernetesJobChecker(BatchV1Api api) {
		this.api = api;
	}

	Long sleep(Long millis, V1Job job) {

		try {

			Thread.sleep(millis);

			return millis;

		} catch (InterruptedException e) {

			logger.error(INTERRUPTED_EX + job.getMetadata().getName(), e);
			//This is suggested by Sonar as best practice
			Thread.currentThread().interrupt();
		}

		return millis;
	}

	public ECSTaskCheckResponse taskStartedWithSuccess(V1Job job, Long maxwait) {

		KubernetesPodChecker podChecker = getPodChecker();

		if (!podChecker.podInstantiated(job)) {

			logger.trace("Failed to launch pod for job {}", job.getMetadata().getName());

			var response = new ECSTaskCheckResponse();

			response.setSuccess(false);
			response.setLastStatus(ECSTASK_STATUS.NOSTATUS);

			return response;

		}

		ECSTASK_STATUS lastStatus = ECSTASK_STATUS.NOSTATUS;

		V1JobStatus status;

		Integer started = 0;

		logger.trace("Started of {} is {}", job.getMetadata().getName(), started);

		var exipredmillis = 0L;

		var response = new ECSTaskCheckResponse();
		response.setSuccess(false);

		while (started - 1 != 0 && exipredmillis <= maxwait) {

			Long e = sleep(retrieveJobStatusWaitIntervalMilliSeconds, job);

			exipredmillis = exipredmillis + e;

			Optional<V1JobStatus> optional = retrieveStatus(job, 0, maxRetry);

			started = 0;

			if (optional.isPresent()) {

				logger.trace("Job Status of {} is present", job.getMetadata().getName());

				status = optional.get();
				Integer active = status.getActive();

				logger.trace("Status Active of {} is {}", job.getMetadata().getName(), active);

				if (active != null && active - 1 == 0) {
					started = 1;
					lastStatus = ECSTASK_STATUS.RUNNING;
					response.setSuccess(true);
				}

				Integer succeeded = status.getSucceeded();

				logger.trace("Status Succeeded of {} is {}", job.getMetadata().getName(), succeeded);

				if (succeeded != null && succeeded - 1 == 0) {
					started = 1;
					lastStatus = ECSTASK_STATUS.STOPPED;
					response.setSuccess(true);
				}

				Integer failed = status.getFailed();

				logger.trace("Status Failed of {} is {}", job.getMetadata().getName(), failed);

				if (failed != null && failed - 1 == 0) {

					logger.warn("Found failed job while waiting fir its start, job {}", job.getMetadata().getName());
					started = 1;
					response.setSuccess(false);
					lastStatus = ECSTASK_STATUS.STOPPED;

				}

			}

			logger.trace("Started of {} is {}", job.getMetadata().getName(), started);

		}

		response.setLastStatus(lastStatus);

		if (!response.isSuccess() && exipredmillis > maxwait) {

			logger.debug("Overriding last status to time out because no start was detected and exipredmillis ({})> maxwait ({})",
					exipredmillis, maxwait);

			response.setLastStatus(ECSTASK_STATUS.TIMEDOUT);
		}

		return response;
	}

	public boolean jobCompletedWithSuccess(V1Job job, Long maxWait) {

		KubernetesPodChecker podChecker = getPodChecker();

		if (!podChecker.podInstantiated(job)) {

			logger.trace("Failed to launch pod for job {}", job.getMetadata().getName());

			return false;

		}

		V1JobStatus status = job.getStatus();

		Integer active = 1;

		logger.trace("Active of {} is {}", job.getMetadata().getName(), active);

		var exipredmillis = 0L;

		while (active - 1 == 0 && exipredmillis <= maxWait) {

			Long e = sleep(retrieveJobStatusWaitIntervalMilliSeconds, job);

			exipredmillis = exipredmillis + e;

			Optional<V1JobStatus> optional = retrieveStatus(job, 0, maxRetry);

			if (optional.isPresent()) {

				status = optional.get();
				active = (status.getActive() != null) ? status.getActive() : 0;

			} else {

				active = 0;

			}

			logger.trace("Active of {} is {}", job.getMetadata().getName(), active);

		}

		Integer succeeded = status.getSucceeded() != null ? status.getSucceeded() : 0;
		return (succeeded - 1 == 0);

	}

	Optional<V1JobStatus> retrieveStatus(V1Job job, Integer i, Integer maxtries) {

		logger.trace("Retrieving job status of job {}", job.getMetadata().getName());

		Optional<V1Job> j = retrieveNameSpacedJob(job, i, maxtries);

		if (j.isPresent())
			return Optional.ofNullable(j.get().getStatus());

		return Optional.ofNullable(null);

	}

	Optional<V1Job> retrieveNameSpacedJob(V1Job job, Integer i, Integer maxtries) {

		logger.trace("Retrieving job {}", job.getMetadata().getName());

		if (i > maxtries)
			return Optional.ofNullable(null);
		try {

			V1Job retrievedJob = api.readNamespacedJobStatus(job.getMetadata().getName(), job.getMetadata().getNamespace(), null);

			if (retrievedJob != null)
				return Optional.of(retrievedJob);

		} catch (ApiException apiEx) {

			logger.error("Can't get job {} at try n. {}", job.getMetadata().getName(), i, apiEx);

		}

		Integer next = i + 1;

		logger.trace("Sleeping before re-trying to get job status of job {}", job.getMetadata().getName());

		sleep(jobStatusRetryIntervalMilliSeconds, job);

		return retrieveNameSpacedJob(job, next, maxtries);

	}

	KubernetesPodChecker getPodChecker() {

		var podChecker = new KubernetesPodChecker();

		podChecker.setCoreApi(coreApi);

		return podChecker;
	}

	public boolean reservationTaskRunning(V1Job job) {

		return getPodChecker().podInstantiated(job);

	}

	public void setCoreApi(CoreV1Api coreApi) {
		this.coreApi = coreApi;
	}

	public CoreV1Api getCoreApi() {
		return coreApi;
	}

	public void setJobStatusRetryIntervalMilliSeconds(Long jobStatusRetryIntervalMilliSeconds) {
		this.jobStatusRetryIntervalMilliSeconds = jobStatusRetryIntervalMilliSeconds;
	}

	public void setRetrieveJobStatusWaitIntervalMilliSeconds(Long retrieveJobStatusWaitIntervalMilliSeconds) {
		this.retrieveJobStatusWaitIntervalMilliSeconds = retrieveJobStatusWaitIntervalMilliSeconds;
	}
}
