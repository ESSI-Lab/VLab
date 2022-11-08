package eu.essi_lab.vlab.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.ResourceRequestResponse;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;

/**
 * @author Mattia Santoro
 */
public interface IResourceAllocator {
	ResourceRequestResponse request(VLabDockerResources resources);

	Boolean supports(BPComputeInfrastructure infra);
	void setInfra(BPComputeInfrastructure infra);
}
