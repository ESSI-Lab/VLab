package eu.essi_lab.vlab.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;

/**
 * @author Mattia Santoro
 */
public interface IndividualInputIngestor {

	Boolean canIngest(BPInputDescription input);

	ContainerOrchestratorCommandResult ingest(BPInputDescription input, String source,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser parser) throws BPException;
}
