package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.ce.controller.services.utils.ComputeConfLoader;
import eu.essi_lab.vlab.controller.services.IBPComputeInfraProvider;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class LocalComputeInfraProvider implements IBPComputeInfraProvider {

	private Logger logger = LogManager.getLogger(LocalComputeInfraProvider.class);

	@Override
	public BPComputeInfrastructure selectComputeInfrastructure(BPRun run, VLabDockerResources requiredRes) throws BPException {
		BPComputeInfrastructure localinfra = ComputeConfLoader.getBPComputeInfrastructure();

		if (run.getExecutionInfrastructure() != null && !localinfra.getId().equalsIgnoreCase(run.getExecutionInfrastructure()))

			throw new BPException("Unsupported required execution infrastructure " + run.getExecutionInfrastructure(),
					BPException.ERROR_CODES.UNSUPPORTED_EXECUTION_INFRASTRUCTURE);

		logger.trace("Selected local infrastructure {} ({})", localinfra.getLabel(), localinfra.getId());
		return localinfra;
	}

	@Override
	public List<ConfigurationParameter> configurationParameters() {
		return new ArrayList<>();
	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) throws BPException {

	}

	@Override
	public Boolean supports(String provider) {

		logger.trace("Executing supports of {}", provider);

		return (provider == null) || "".equalsIgnoreCase("");

	}
}
