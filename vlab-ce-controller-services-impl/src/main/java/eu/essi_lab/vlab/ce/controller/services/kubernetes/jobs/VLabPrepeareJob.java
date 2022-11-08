package eu.essi_lab.vlab.ce.controller.services.kubernetes.jobs;

import static eu.essi_lab.vlab.ce.controller.services.kubernetes.KubernetesClient.DEFAULT_JOB_TTL_SECONDS;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Mattia Santoro
 */
public class VLabPrepeareJob {

	private static final String KIND = "Job";
	private static final String API_VERSION = "batch/v1";
	private final String imageName;
	private static final String RESTART_POLICY = "Never";
	private String cmd;

	private final String id;
	private Integer requestedMemory;
	//TODO cpu is not taken into account now, need to understand units as described here https://kubernetes
	// .io/docs/concepts/configuration/manage-compute-resources-container/
	private Integer requestedCPU;
	private Optional<String> nodeSelector = Optional.empty();

	private String vlabPvClaim;
	private String vlabPv;

	private String nodeTolerations;
	private String runFolder;

	public VLabPrepeareJob(String image) {

		imageName = image;

		id = UUID.randomUUID().toString();

	}

	public V1Job getJob() {

		var job = new V1Job();

		job.kind(KIND).metadata(new V1ObjectMeta().name(id)).apiVersion(API_VERSION).spec(new V1JobSpec().ttlSecondsAfterFinished(
						DEFAULT_JOB_TTL_SECONDS).completions(1).parallelism(1).backoffLimit(0)
				.template(new V1PodTemplateSpec().metadata(new V1ObjectMeta().name("name")).spec(new V1PodSpec().addVolumesItem(
								new V1Volume().name(vlabPv).persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName(vlabPvClaim)))
						.addContainersItem(new V1Container().image(imageName).name("preparejobcontainer")
								.command(Arrays.asList("/bin/sh", "-c"))
								.addVolumeMountsItem(new V1VolumeMount().name(vlabPv).mountPath(runFolder)))
						.restartPolicy(RESTART_POLICY))));

		addArgs(job);

		addRequestResources(job);

		addSelectorAndTolerations(job);

		return job;

	}

	private void addSelectorAndTolerations(V1Job job) {
		VLabJobUtils.addNodeSelector(job, nodeSelector);
		VLabJobUtils.addNodeTolerations(job, nodeTolerations);
	}

	private void addRequestResources(V1Job job) {

		VLabJobUtils.addRequestResources(job, requestedMemory);

	}

	private void addArgs(V1Job job) {
		VLabJobUtils.addArgs(job, cmd);
	}

	public void setCommand(String command) {
		cmd = command;
	}

	public void setMemoryReservation(Integer memory) {
		requestedMemory = memory;
	}

	public void setCpu(Integer cpu) {
		requestedCPU = cpu;
	}

	public Optional<String> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(Optional<String> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public void setVlabPvClaim(String vlabpvclaim) {
		this.vlabPvClaim = vlabpvclaim;
	}

	public void setVlabPV(String vlabpv) {
		this.vlabPv = vlabpv;
	}

	public void setNodeTolerations(String nodeTolerations) {
		this.nodeTolerations = nodeTolerations;
	}

	public String getNodeTolerations() {
		return nodeTolerations;
	}

	public void setRunFolder(String runFolder) {
		this.runFolder = runFolder;
	}
}
