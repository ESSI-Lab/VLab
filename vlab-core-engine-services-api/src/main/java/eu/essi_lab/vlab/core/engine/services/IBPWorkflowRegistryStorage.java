package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;

/**
 * @author Mattia Santoro
 */
public interface IBPWorkflowRegistryStorage extends IBPConfigurableService{

	APIWorkflowDetail getAPIWorkflowDetail(String workflowid, BPUser user) throws BPException;

	void storeWorkflowDetail(APIWorkflowDetail bp) throws BPException;

	void deleteWorkflowDetail(String workflowid) throws BPException;

	SearchWorkflowsResponse searchWorkflowDetail(String text, Integer start, Integer count, Boolean undertest, BPUser user)
			throws BPException;

	void updateWorkflowProperties(APIWorkflowDetail updatedWorkflow, APIWorkflowDetail existingWorkflow) throws BPException;

	Boolean supports(String storagetype);
}
