package eu.essi_lab.vlab.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.engine.services.IBPConfigurableService;

/**
 * @author Mattia Santoro
 */
public interface IBPComputeInfraProvider extends IBPConfigurableService {

	BPComputeInfrastructure selectComputeInfrastructure(BPRun run, VLabDockerResources requiredRes) throws BPException;

}
