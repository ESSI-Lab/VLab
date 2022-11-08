package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IResourceAllocator;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.ResourceRequestResponse;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubernetesResourceAllocator implements IResourceAllocator {

	private Logger logger = LogManager.getLogger(KubernetesResourceAllocator.class);

	@Override
	public ResourceRequestResponse request(VLabDockerResources resources) {

		logger.info("KubernetesResourceAllocator does not support the requested operation: request(VLabDockerResources resources)");
		ResourceRequestResponse response = new ResourceRequestResponse();
		response.setRequestSent(false);
		return response;
	}

	@Override
	public Boolean supports(BPComputeInfrastructure infra) {
		return infra.getType().contains("kubernetes");
	}

	@Override
	public void setInfra(BPComputeInfrastructure infra) {
		//nothing to do here, kubernetes allocator does not implement the operation
	}
}
