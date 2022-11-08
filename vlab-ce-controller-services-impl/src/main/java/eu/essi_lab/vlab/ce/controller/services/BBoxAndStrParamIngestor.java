package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;

/**
 * @author Mattia Santoro
 */
public class BBoxAndStrParamIngestor implements IndividualInputIngestor {

	@Override
	public Boolean canIngest(BPInputDescription input) {
		return "bbox".equalsIgnoreCase(input.getInput().getValueSchema()) || "string_parameter".equalsIgnoreCase(input.getInput().getValueSchema());
	}

	@Override
	public ContainerOrchestratorCommandResult ingest(BPInputDescription input, String source,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser parser) throws BPException {

		return commandExecutor.appendParamTo(input.getInput().getId(), source, parser.getDockerContainerAbsolutePathParameterFile(), 1000L * 60 * 60);
	}
}
