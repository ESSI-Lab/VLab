package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPMNConventionParser;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResult;
import eu.essi_lab.vlab.core.datamodel.DeleteWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.utils.WorkflowUtils;
import eu.essi_lab.vlab.core.engine.services.IBPWorkflowRegistryStorage;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import eu.essi_lab.vlab.core.utils.URLReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class ExecutableBPRegistry implements IExecutableBPRegistry {

	private IBPWorkflowRegistryStorage storage;
	private Logger logger = LogManager.getLogger(ExecutableBPRegistry.class);
	private static final String NO_DESCRIPTION_PROVIDED = "No description provided";
	private static final String VALIDATION_WF_OBJECT_FAILED = "Validation of workflow object failed with message {}";

	private Boolean responseHasResult(CreateWorkflowResponse response, CreateWorkflowResult result) {
		return response.getResult() != null && response.getResult().toString().equalsIgnoreCase(result.toString());
	}

	public CreateWorkflowResponse createWorkflow(APIWorkflowDetail workflow, BPUser user) throws BPException {
		CreateWorkflowResponse response = new CreateWorkflowResponse();

		validate(workflow, user, response);

		if (Boolean.TRUE.equals(responseHasResult(response, CreateWorkflowResult.FAIL)))
			return response;

		try {
			storage.storeWorkflowDetail(workflow);

			logger.info("Workflow {} not stored", workflow.getId());

			response.setResult(CreateWorkflowResult.SUCCESS);

			response.setMessage("Successfully stored workflow " + workflow.getId());

		} catch (BPException ex) {

			logger.error("Workflow {} was not stored", workflow.getId());

			response.setResult(CreateWorkflowResult.FAIL);
			response.setMessage("An error occured during storage of workflow " + workflow.getId() + " (ex code: " + ex.getErroCode() + ")");

		}

		return response;
	}

	private void validate(APIWorkflowDetail workflow, BPUser user, CreateWorkflowResponse response) {

		logger.trace("Validating workflow ({}) and user", workflow.getId());

		validateWorkflow(workflow, response);

		if (Boolean.TRUE.equals(responseHasResult(response, CreateWorkflowResult.FAIL)))
			return;

		logger.trace("Checking owner and requester for workflopw ({})", workflow.getId());

		if (!workflow.getModelDeveloperEmail().equalsIgnoreCase(user.getEmail())) {
			logger.error("Can't create a workflow owned by a user different from the requester");

			response.setResult(CreateWorkflowResult.FAIL);
			response.setMessage("Only the owner of a workflow can request its storage");

		}

	}

	private void setFailWithMessage(CreateWorkflowResponse response, String message) {
		response.setResult(CreateWorkflowResult.FAIL);
		response.setMessage(message);
	}

	private void validateWorkflow(APIWorkflowDetail workflow, CreateWorkflowResponse response) {

		logger.trace("Validating workflow object");

		if (emptyValue(workflow.getId())) {

			setFailWithMessage(response, "Workflow Id is mandatory");

			logger.trace(VALIDATION_WF_OBJECT_FAILED, response.getMessage());
			return;

		}

		if (emptyValue(workflow.getModelDeveloperEmail())) {

			setFailWithMessage(response, "Workflow Developer Email is mandatory");

			logger.trace(VALIDATION_WF_OBJECT_FAILED, response.getMessage());
			return;
		}

		if (emptyValue(workflow.getModelDeveloper())) {

			setFailWithMessage(response, "Workflow Developer Name is mandatory");

			logger.trace(VALIDATION_WF_OBJECT_FAILED, response.getMessage());
			return;
		}

		if (emptyValue(workflow.getModelDeveloperOrg())) {

			setFailWithMessage(response, "Workflow Developer Organization is mandatory");

			logger.trace(VALIDATION_WF_OBJECT_FAILED, response.getMessage());
			return;
		}

		if (emptyValue(workflow.getBpmn_url())) {

			setFailWithMessage(response, "Workflow BPMN url is mandatory");

			logger.trace(VALIDATION_WF_OBJECT_FAILED, response.getMessage());
			return;
		}

		if (emptyValue(workflow.getName())) {

			setFailWithMessage(response, "Workflow name is mandatory");

			logger.trace(VALIDATION_WF_OBJECT_FAILED, response.getMessage());
			return;
		}

		if (emptyValue(workflow.getDescription())) {

			logger.warn("Workflow {} is provided without description, setting default value", workflow.getId());
			workflow.setDescription(NO_DESCRIPTION_PROVIDED);

		}

	}

	private boolean emptyValue(String value) {
		return value == null || "".equalsIgnoreCase(value);
	}

	public AtomicExecutableBP getExecutable(String workflowid, BPUser user) throws BPException {
		logger.debug("Requested workflow {}", workflowid);

		APIWorkflowDetail workflow = getWorkflowDetail(workflowid, user);

		String bpmnURL = workflow.getBpmn_url();

		logger.debug("Retrieving bpmn @ {}", bpmnURL);

		InputStream stream = readUrl(bpmnURL);

		logger.debug("Parsing bpmn from {}", bpmnURL);

		return new AtomicExecutableBP(stream, loadBpmnParser());

	}

	protected BPMNConventionParser loadBpmnParser() {
		return new BPMNConventionParser();
	}

	InputStream readUrl(String bpmnURL) throws BPException {

		try {

			return getReader().read(bpmnURL);

		} catch (IOException e) {

			String err = "Error reading " + bpmnURL + ": " + e.getMessage();

			logger.error(err);

			throw new BPException(err, BPException.ERROR_CODES.URL_READ_ERROR.getCode());

		}
	}

	URLReader getReader() {
		return new URLReader();

	}

	public APIWorkflowDetail getWorkflowDetail(String workflowid, BPUser user) throws BPException {

		return getWorkflowDetail(workflowid, user, Boolean.TRUE);
	}

	private APIWorkflowDetail getWorkflowDetail(String workflowid, BPUser user, Boolean publicinfoonly) throws BPException {

		logger.debug("Requesting {} to storage", workflowid);

		APIWorkflowDetail workflow = storage.getAPIWorkflowDetail(workflowid, user);

		WorkflowUtils.throwExceptionIfCantRead(user, workflow);

		if (Boolean.TRUE.equals(publicinfoonly))
			return WorkflowUtils.removePrivateInfo(workflow, user.getEmail());

		return workflow;

	}

	public SearchWorkflowsResponse search(String text, Integer start, Integer count, Boolean undertest, BPUser user) throws BPException {

		logger.debug("Search workflow submitted to storage");
		SearchWorkflowsResponse r1 = storage.searchWorkflowDetail(text, start, count, undertest, user);

		logger.debug("Copying search workflow response");
		SearchWorkflowsResponse r2 = new SearchWorkflowsResponse();
		r2.setTotal(r1.getTotal());

		List<APIWorkflowDetail> list = new ArrayList<>();
		logger.debug("Removing private info");
		r1.getWorkflows().forEach(w -> list.add(WorkflowUtils.removePrivateInfo(w, user.getEmail())));
		r2.setWorkflows(list);

		logger.debug("Search workflow completed");

		return r2;

	}

	public UpdateWorkflowResponse updateWorkflow(String workflowid, APIWorkflowDetail workflowDetail, BPUser user) throws BPException {

		UpdateWorkflowResponse response = new UpdateWorkflowResponse();

		logger.trace("Retrieving {} for update operation", workflowid);
		APIWorkflowDetail workflow = getWorkflowDetail(workflowid, user, Boolean.FALSE);

		WorkflowUtils.throwNotAuthorizedExceptionIfNotMyRun(user, workflow, "You are not authorized to perform update of this workflow");

		if (!workflow.getId().equalsIgnoreCase(workflowDetail.getId())) {
			throw new BPException("The workflow identifier can not be changed", BPException.ERROR_CODES.INVALID_REQUEST);
		}

		merge(workflowDetail, workflow);

		storage.updateWorkflowProperties(workflowDetail, workflow);

		response.setUpdatedWorkflowId(workflowid);

		return response;
	}

	void merge(APIWorkflowDetail updatedWorkflow, APIWorkflowDetail existingWorkflow) throws BPException {

		List<String> oldShared = existingWorkflow.getSharedWith();

		for (String s : oldShared) {

			if (!updatedWorkflow.getSharedWith().contains(s))
				updatedWorkflow.shareWithUser(s);

		}

		updatedWorkflow.setModelDeveloperEmail(existingWorkflow.getModelDeveloperEmail());

	}

	public DeleteWorkflowResponse deleteWorkflow(String workflowid, BPUser user) throws BPException {

		DeleteWorkflowResponse response = new DeleteWorkflowResponse();

		logger.trace("Retrieving {} for delete operation", workflowid);
		APIWorkflowDetail workflow = getWorkflowDetail(workflowid, user, Boolean.FALSE);

		logger.trace("Authorizing user to delete workflow {}", workflowid);
		WorkflowUtils.throwNotAuthorizedExceptionIfNotMyRun(user, workflow, "You are not authorized to remove this Workflow");

		logger.trace("Request to storage to delete workflow {}", workflowid);

		storage.deleteWorkflowDetail(workflowid);

		response.setDeletedWorkflowId(workflowid);

		return response;
	}

	@Override
	public void setBPExecutableStorage(IBPWorkflowRegistryStorage bpExecutableStorage) {
		this.storage = bpExecutableStorage;
	}
}
