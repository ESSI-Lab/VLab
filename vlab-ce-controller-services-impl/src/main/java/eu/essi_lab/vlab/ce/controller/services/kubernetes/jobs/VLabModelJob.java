package eu.essi_lab.vlab.ce.controller.services.kubernetes.jobs;

import static eu.essi_lab.vlab.ce.controller.services.kubernetes.KubernetesClient.DEFAULT_JOB_TTL_SECONDS;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.engine.utils.BPUtils;
import static eu.essi_lab.vlab.core.engine.utils.BPUtils.BPENGINE_RUNID_KEY;
import static eu.essi_lab.vlab.core.engine.utils.BPUtils.VLAB_RUNID_KEY;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Mattia Santoro
 */
public class VLabModelJob {

	private static final String KIND = "Job";
	private static final String API_VERSION = "batch/v1";

	private static final String RESTART_POLICY = "Never";

	private final String id;
	private final VLabDockerImage dockerImage;
	private final String runid;
	private String runFolder;
	private Integer requestedMemory;
	//TODO cpu is not taken into account now, need to understand units as described here https://kubernetes
	// .io/docs/concepts/configuration/manage-compute-resources-container/
	private Integer requestedCPU;
	private Optional<String> nodeSelector = Optional.empty();

	private String vlabPvClaim;
	private String vlabPv;
	private Map<String, String> envVars;
	private String nodeTolerations;
	private String rootFolder;

	public VLabModelJob(VLabDockerImage image, String run) {

		id = UUID.randomUUID().toString();

		runid = run;

		dockerImage = image;

	}

	public V1Job getJob() {

		var job = new V1Job();

		job.kind(KIND).metadata(new V1ObjectMeta().name(id)).apiVersion(API_VERSION).spec(new V1JobSpec().ttlSecondsAfterFinished(
						DEFAULT_JOB_TTL_SECONDS).completions(1).parallelism(1).backoffLimit(0)
				.template(new V1PodTemplateSpec().metadata(new V1ObjectMeta().name("name")).spec(new V1PodSpec().addVolumesItem(//
								new V1Volume().name(vlabPv).persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName(vlabPvClaim)))
						.addContainersItem(new V1Container().image(dockerImage.getImage()).name(runid).command(
										dockerImage.getContainer().getEntryPoint())
								.addVolumeMountsItem(new V1VolumeMount().name(vlabPv).mountPath(rootFolder))
								.env(Arrays.asList(new V1EnvVar().name(BPENGINE_RUNID_KEY).value(runid),
										new V1EnvVar().name(VLAB_RUNID_KEY).value(runid)))).restartPolicy(RESTART_POLICY))));

		addArgs(job);

		addRequestResources(job);

		addSelectorAndTolerations(job);

		addEnvironmetVariables(job);

		return job;

	}

	private void addEnvironmetVariables(V1Job job) {
		if (null == envVars)
			return;

		List<V1EnvVar> vars = new ArrayList<>();

		envVars.entrySet().stream().forEach(e -> vars.add(new V1EnvVar().name(e.getKey()).value(e.getValue())));

		VLabJobUtils.addEnvVars(job, vars);
	}

	private void addSelectorAndTolerations(V1Job job) {
		VLabJobUtils.addNodeSelector(job, nodeSelector);
		VLabJobUtils.addNodeTolerations(job, nodeTolerations);
	}

	private void addRequestResources(V1Job job) {

		VLabJobUtils.addRequestResources(job, requestedMemory);
	}

	public void setMemoryReservation(Integer memory) {
		requestedMemory = memory;
	}

	private void addArgs(V1Job job) {

		List<String> commands = BPUtils.createCommand(dockerImage.getContainer(), runFolder);

		VLabJobUtils.addArgs(job, commands);

	}

	public void setRunFolder(String runFolder) {
		this.runFolder = runFolder;
	}

	public void setCpu(Integer requestedCPU) {
		this.requestedCPU = requestedCPU;
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

	public Map<String, String> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(Map<String, String> envVars) {
		this.envVars = envVars;
	}

	public void setNodeTolerations(String nodeTolerations) {
		this.nodeTolerations = nodeTolerations;
	}

	public String getNodeTolerations() {
		return nodeTolerations;
	}

	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}
}
