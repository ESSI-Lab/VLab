package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import static eu.essi_lab.vlab.ce.controller.services.kubernetes.KubernetesClient.JOBS_NS;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubernetesPodChecker {

	private Logger logger = LogManager.getLogger(KubernetesPodChecker.class);

	private long waitIntervalPodInstantitionInitial = 10000L;
	private static final String INTERRUPTED_EX = "Interrupted exception requesting status of ";

	private CoreV1Api coreApi;

	void setWaitIntervalPodInstantitionInitial(Long interval) {
		waitIntervalPodInstantitionInitial = interval;
	}

	private Optional<V1Pod> getPod(String jobid) {

		logger.trace("Retrieving pod of {}", jobid);

		try {

			V1PodList podList = coreApi.listNamespacedPod(JOBS_NS, null, null, null, null, "job-name=" + jobid, 1, null, null, 10, false);

			if (podList == null || podList.getItems() == null || podList.getItems().isEmpty()) {

				logger.warn("No pod found for job {}", jobid);

				return Optional.ofNullable(null);
			}

			logger.warn("Pod list of job {} has size {}", jobid, podList.getItems().size());

			return Optional.of(podList.getItems().get(0));

		} catch (ApiException e) {

			logger.warn("Can't get pod of job {}", jobid, e);

			return Optional.ofNullable(null);

		}

	}

	public Optional<String> podIdentifier(String jobid) {
		try {

			Thread.sleep(waitIntervalPodInstantitionInitial);

		} catch (InterruptedException e) {

			logger.error(INTERRUPTED_EX + jobid, e);
			//This is suggested by Sonar as best practice
			Thread.currentThread().interrupt();
		}

		logger.trace("Checking pod instantiation of job {}", jobid);

		Optional<V1Pod> optionalPod = getPod(jobid);

		if (!optionalPod.isPresent()) {

			logger.warn("Can't find pod for job {}, returning false", jobid);

			return Optional.empty();
		}

		V1Pod pod = optionalPod.get();

		List<V1PodCondition> conditions = pod.getStatus().getConditions();
		boolean unschedulable = unschedulable(conditions);
		logger.trace("Pod is unschedulable: {}", unschedulable);

		if (unschedulable)
			return Optional.empty();

		boolean imagePullErr = imagePullErr(pod.getStatus().getContainerStatuses());
		logger.trace("Pod can't pull image: {}", imagePullErr);

		if (imagePullErr)
			return Optional.empty();

		return Optional.ofNullable(pod.getMetadata().getName());

	}

	public boolean podInstantiated(String jobid) {
		return podIdentifier(jobid).isPresent();
	}

	public Optional<String> podIdentifier(V1Job job) {

		return podIdentifier(job.getMetadata().getName());

	}

	public boolean podInstantiated(V1Job job) {

		return podIdentifier(job.getMetadata().getName()).isPresent();

	}

	public boolean unschedulable(List<V1PodCondition> conditions) {

		Optional<V1PodCondition> faultCondition = conditions.stream().filter(
				c -> (c.getStatus() != null && c.getStatus().equalsIgnoreCase("false") && c.getReason() != null && c.getReason()
						.equalsIgnoreCase("unschedulable"))).findAny();

		if (!faultCondition.isPresent())
			return false;

		logger.trace("Found fault condition {}", faultCondition.get());

		return true;
	}

	public boolean imagePullErr(List<V1ContainerStatus> conditions) {
		return conditions.stream().anyMatch(
				c -> ((c.getState() != null && c.getState().getWaiting() != null && c.getState().getWaiting().getReason() != null && c
						.getState().getWaiting().getReason().equalsIgnoreCase("ImagePullBackOff"))));
	}

	public void setCoreApi(CoreV1Api coreApi) {
		this.coreApi = coreApi;
	}

	public CoreV1Api getCoreApi() {
		return this.coreApi;
	}

	public void waitPodTermination(String jobid) {

		logger.debug("Waiting for pod termination of job {}", jobid);

		Optional<V1Pod> optionalPod = getPod(jobid);

		while (optionalPod.isPresent()) {

			logger.trace("Pod of job {} was found", jobid);

			try {

				Thread.sleep(waitIntervalPodInstantitionInitial);

			} catch (InterruptedException e) {

				logger.error(INTERRUPTED_EX + jobid, e);
				//This is suggested by Sonar as best practice
				Thread.currentThread().interrupt();
			}

			logger.trace("Retrying get pod for job {}", jobid);

			optionalPod = getPod(jobid);
		}

		logger.debug("Pod of job {} no longer exists, returning", jobid);

	}
}
