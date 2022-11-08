package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.ce.controller.services.kubernetes.KubernetesClient;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPKubernetesComputeInfrastructure;

/**
 * @author Mattia Santoro
 */
public class KubernetesClientManager implements IContainerOrchestratorManager {

	private KubernetesClient executor;

	@Override
	public IContainerOrchestratorCommandExecutor getExecutor() {
		if (null == executor)
			executor = new KubernetesClient();
		return executor;
	}

	@Override
	public Boolean supports(BPComputeInfrastructure computeInfrastructure) {
		return computeInfrastructure instanceof BPKubernetesComputeInfrastructure;
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
