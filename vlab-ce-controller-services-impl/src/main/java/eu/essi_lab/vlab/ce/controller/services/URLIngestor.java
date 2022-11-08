package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;

/**
 * @author Mattia Santoro
 */
public class URLIngestor implements IndividualInputIngestor {

	@Override
	public Boolean canIngest(BPInputDescription input) {
		return input.getInput().getValueSchema() == null || "url".equalsIgnoreCase(input.getInput().getValueSchema());
	}

	@Override
	public ContainerOrchestratorCommandResult ingest(BPInputDescription input, String source,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser parser) {

		return commandExecutor.downloadFileTo(source, parser.getDockerContainerAbsolutePath(input), 1000L * 60 * 60);
	}

}
