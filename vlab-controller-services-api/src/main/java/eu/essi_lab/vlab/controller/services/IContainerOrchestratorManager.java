package eu.essi_lab.vlab.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;

/**
 * @author Mattia Santoro
 */
public interface IContainerOrchestratorManager {

	IContainerOrchestratorCommandExecutor getExecutor();

	Boolean supports(BPComputeInfrastructure computeInfrastructure);

	void setRootExecutionFolder(String rootFolder);

	void setBPInfrastructure(BPComputeInfrastructure infrastructure) throws BPException;

	void initialize() throws BPException;

	void cleanResources() throws BPException;

}
