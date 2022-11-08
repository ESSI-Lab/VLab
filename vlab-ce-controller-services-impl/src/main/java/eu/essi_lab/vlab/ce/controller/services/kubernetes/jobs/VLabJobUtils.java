package eu.essi_lab.vlab.ce.controller.services.kubernetes.jobs;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Toleration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class VLabJobUtils {


	private static final String VLAB_NODE_SELECTOR_KEY = "vlabkey";
	private static final String MEMORY_REQUEST_KEY = "memory";
	private static final String CPU_REQUEST_KEY = "cpu";
	private static Logger logger = LogManager.getLogger(VLabJobUtils.class);

	private VLabJobUtils() {
		//private constructor to force static usage

	}

	public static void addArgs(V1Job job, String cmd) {
		if (cmd != null)
			addArgs(job, Arrays.asList(cmd));

	}

	public static void addArgs(V1Job job, List<String> commands) {

		commands.forEach(c -> job.getSpec().getTemplate().getSpec().getContainers().get(0).addArgsItem(c));

	}

	public static void addEnvVars(V1Job job, List<V1EnvVar> vars) {

		vars.forEach(v1EnvVar -> job.getSpec().getTemplate().getSpec().getContainers().get(0).addEnvItem(v1EnvVar));

	}



	public static void addRequestResources(V1Job job, Integer requestedMemory) {

		if (requestedMemory != null)

			job.getSpec().getTemplate().getSpec().getContainers().get(0).resources(
					new V1ResourceRequirements().putRequestsItem(MEMORY_REQUEST_KEY, new Quantity(requestedMemory.toString() + "M")));

	}

	public static void addNodeSelector(V1Job job, Optional<String> oselector) {

		if (oselector.isPresent()) {

			String selector = oselector.get();

			logger.trace("Adding node selector {}:{}", VLAB_NODE_SELECTOR_KEY, selector);

			Map<String, String> map = new HashMap<>();

			map.put(VLAB_NODE_SELECTOR_KEY, selector);

			job.getSpec().getTemplate().getSpec().putNodeSelectorItem(VLAB_NODE_SELECTOR_KEY, selector);

		} else
			logger.warn("No selector to add");

	}

	public static void addNodeTolerations(V1Job job, String tolerations) {

		if (tolerations != null) {

			Arrays.asList(tolerations.split(",")).forEach(t -> {

				logger.trace("Adding node toleration {}", t);

				V1Toleration tol = new V1Toleration();
				tol.setKey(t.split("=")[0]);
				tol.setValue(t.split("=")[1]);
				tol.setOperator("Equal");
				tol.setEffect("NoSchedule");
				job.getSpec().getTemplate().getSpec().addTolerationsItem(tol);

			});

			logger.trace("Total number of tolerations {}",
					job.getSpec().getTemplate().getSpec().getTolerations() != null ? job.getSpec().getTemplate().getSpec().getTolerations()
							.size() : 0);

		} else
			logger.warn("No toleration to add");

	}

}
