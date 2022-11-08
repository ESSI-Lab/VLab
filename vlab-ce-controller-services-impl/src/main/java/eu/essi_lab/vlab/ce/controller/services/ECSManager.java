package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSClient;
import eu.essi_lab.vlab.core.configuration.BPECSComputeInfrastructure;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;

/**
 * @author Mattia Santoro
 */
public class ECSManager implements IContainerOrchestratorManager {

	private ECSClient ecsClient;

	@Override
	public IContainerOrchestratorCommandExecutor getExecutor() {
		if (null == ecsClient)
			ecsClient = new ECSClient();
		return ecsClient;
	}

	@Override
	public Boolean supports(BPComputeInfrastructure computeInfrastructure) {

		return computeInfrastructure instanceof BPECSComputeInfrastructure;
	}

	@Override
	public void setRootExecutionFolder(String rootFolder) {
		getExecutor().setRootExecutionFolder(rootFolder);
	}

	@Override
	public void setBPInfrastructure(BPComputeInfrastructure infrastructure) throws BPException {
		getExecutor().setBPInfrastructure(infrastructure);
	}

	@Override
	public void initialize() throws BPException {
		//nothing to do here
	}

	@Override
	public void cleanResources() throws BPException {
		//nothing to do here
	}

}
