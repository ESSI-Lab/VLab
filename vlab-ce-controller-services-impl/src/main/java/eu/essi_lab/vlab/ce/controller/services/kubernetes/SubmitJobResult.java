package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import io.kubernetes.client.openapi.models.V1Job;

/**
 * @author Mattia Santoro
 */
public class SubmitJobResult {
	private final V1Job responseJob;
	private final KubernetesJobManager kubernetesJobManager;
	private final V1Job originalJob;

	public SubmitJobResult(KubernetesJobManager m, V1Job oJob, V1Job respJob) {
		this.kubernetesJobManager = m;
		this.originalJob = oJob;
		this.responseJob = respJob;
	}

	public KubernetesJobManager getKubernetesJobManager() {
		return kubernetesJobManager;
	}

	public V1Job getResponseJob() {
		return responseJob;
	}

	public V1Job getOriginalJob() {
		return originalJob;
	}
}
