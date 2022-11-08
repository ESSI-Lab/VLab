package eu.essi_lab.vlab.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;

/**
 * @author Mattia Santoro
 */
public interface OutputIngestor {

	Boolean canIngest(BPOutputDescription output);

	ContainerOrchestratorCommandResult ingest(BPOutputDescription input, String runid,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser parser) throws BPException;

	ContainerOrchestratorCommandResult ingestArray(BPOutputDescription input, String runid,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser parser) throws BPException;

}
