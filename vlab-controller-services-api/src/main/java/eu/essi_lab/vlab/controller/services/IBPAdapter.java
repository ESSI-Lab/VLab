package eu.essi_lab.vlab.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public interface IBPAdapter {

	ContainerOrchestratorReservationResult acquireResources(BPRunStatus status) throws BPException;

	void setInputs(List<BPInput> inputs);

	void execute(BPRunStatus status);

	void cleanBPRunResources(String runid) throws BPException;

	VLabDockerResources getRequiredResources() throws BPException;

	Boolean supports(BPRealization realization);

	void setConnector(ISourceCodeConnector codeConector);

	void releaseResources(ContainerOrchestratorReservationResult resourcesAcquired) throws BPException;

	void setTargetComputeInfrastructure(BPComputeInfrastructure infra);

}
