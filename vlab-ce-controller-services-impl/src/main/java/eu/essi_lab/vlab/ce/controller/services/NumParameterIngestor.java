package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import eu.essi_lab.vlab.core.engine.utils.BPUtils;

/**
 * @author Mattia Santoro
 */
public class NumParameterIngestor implements IndividualInputIngestor {

	@Override
	public Boolean canIngest(BPInputDescription input) {
		return "number_parameter".equalsIgnoreCase(input.getInput().getValueSchema());
	}

	@Override
	public ContainerOrchestratorCommandResult ingest(BPInputDescription input, String source,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser parser) throws BPException {

		return commandExecutor.appendParamTo(input.getInput().getId(), BPUtils.toNumber(source), parser.getDockerContainerAbsolutePathParameterFile(),
				1000L * 60 * 60);
	}
}
