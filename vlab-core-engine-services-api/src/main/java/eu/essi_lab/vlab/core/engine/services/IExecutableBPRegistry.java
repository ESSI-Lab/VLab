package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.DeleteWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;

/**
 * @author Mattia Santoro
 */
public interface IExecutableBPRegistry {

	CreateWorkflowResponse createWorkflow(APIWorkflowDetail workflow, BPUser user) throws BPException;

	AtomicExecutableBP getExecutable(String workflowid, BPUser user) throws BPException;

	APIWorkflowDetail getWorkflowDetail(String workflowid, BPUser user) throws BPException;

	SearchWorkflowsResponse search(String text, Integer start, Integer count, Boolean undertest, BPUser user) throws BPException;

	UpdateWorkflowResponse updateWorkflow(String workflowid, APIWorkflowDetail workflowDetail, BPUser user) throws BPException;

	DeleteWorkflowResponse deleteWorkflow(String workflowid, BPUser user) throws BPException;

	void setBPExecutableStorage(IBPWorkflowRegistryStorage bpExecutableStorage);
}
