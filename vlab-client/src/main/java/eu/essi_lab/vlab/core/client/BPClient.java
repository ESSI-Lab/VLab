package eu.essi_lab.vlab.core.client;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPObject;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunRequest;
import eu.essi_lab.vlab.core.datamodel.BPRunResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.BPWorkflowInputsResponse;
import eu.essi_lab.vlab.core.datamodel.BPWorkflowOutputsResponse;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.DeleteRunResponse;
import eu.essi_lab.vlab.core.datamodel.DeleteWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateRunResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.utils.BPRunUtils;
import eu.essi_lab.vlab.core.engine.BPEngine;
import eu.essi_lab.vlab.core.engine.BPPreprocessor;
import eu.essi_lab.vlab.core.engine.factory.BPRegistriesFactory;
import eu.essi_lab.vlab.core.engine.factory.StorageFactory;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class BPClient {

	private Logger logger = LogManager.getLogger(BPClient.class);
	private static final String JSON_OBJECT_LOG = "JSONObject: {}";

	public BPRun getRun(String runid, BPUser user) throws BPException {

		logger.info("Executing GetRun with runid: {}", runid);

		BPRegistriesFactory factory = getFactory();

		return factory.getBPRunRegistry().get(runid, user);
	}

	public DeleteRunResponse deleteRun(String runid, BPUser user) throws BPException {
		logger.info("Executing Delete Run with runid: {}", runid);

		BPRegistriesFactory factory = getFactory();

		BPRun tobedeleted = getRun(runid, user);

		boolean deleted = factory.getBPRunRegistry().unregisterBPRun(tobedeleted, user);

		if (deleted) {

			DeleteRunResponse response = new DeleteRunResponse();

			response.setDeletedRunId(tobedeleted.getRunid());

			return response;

		}

		logger.info("Unknown error during delete run operation of run id {}", runid);

		BPException ex = new BPException("BPRunRegistry returned false on deregisterBPRun for " + tobedeleted.getRunid(),
				BPException.ERROR_CODES.UNKNOWN);

		ex.setUserMessage("An internal error occurred deleting your run, please try again later");

		throw ex;

	}

	public UpdateRunResponse updateRun(String runid, JSONObject json, BPUser user) throws BPException {

		logger.info("Executing Update Run with runid: {}", runid);

		logger.debug("Parsing run to update");

		BPRun run = null;

		try {

			run = new JSONDeserializer().deserialize(json.toString(), BPRun.class);

		} catch (BPException e) {

			logger.error("Error parsing BPRunRequest: {}", json);

			throw new BPException("Error parsing BPRun to be updated: " + json.toString(),
					BPException.ERROR_CODES.INVALID_REQUEST.getCode());

		}
		return updateRun(runid, run, user);
	}

	public UpdateRunResponse updateRun(String runid, BPRun run, BPUser user) throws BPException {

		logger.info("Executing Update Run with runid: {} and provided run id {}", runid, run.getRunid());

		if (runid == null || !runid.equalsIgnoreCase(run.getRunid())) {

			logger.error("Run identifier mismatch: path id is {} but body run id is {}", runid, run.getRunid());

			throw new BPException("Run identifier mismatch: path id is " + runid + " but body run id is " + run.getRunid(),
					BPException.ERROR_CODES.INVALID_REQUEST.getCode());

		}

		BPRegistriesFactory factory = getFactory();

		boolean updated = factory.getBPRunRegistry().updateRun(run, user);

		if (updated) {

			UpdateRunResponse response = new UpdateRunResponse();

			response.setUpdatedRunId(runid);

			return response;

		}

		logger.info("Unknown error during update run operation of run id {}", runid);

		BPException ex = new BPException("BPRunRegistry returned false on updateRun for " + runid, BPException.ERROR_CODES.UNKNOWN);

		ex.setUserMessage("An internal error occurred deleting your run, please try again later");

		throw ex;

	}

	public BPRun createRun(String workflowid, JSONObject json, BPUser user) throws BPException {

		logger.info("Executing CreateRun with workflowid: {}", workflowid);

		logger.trace(JSON_OBJECT_LOG, json);

		logger.debug("Parsing input list");

		BPRunRequest bprunRequest = null;

		try {

			bprunRequest = new JSONDeserializer().deserialize(json.toString(), BPRunRequest.class);

		} catch (BPException e) {

			logger.error("Error parsing BPRunRequest: {}", json);

			throw new BPException("Error parsing BPRunRequest: " + json, BPException.ERROR_CODES.INVALID_REQUEST.getCode());

		}

		return createRun(workflowid, bprunRequest, user);

	}

	public BPRun createRun(String workflowid, BPRunRequest bprunRequest, BPUser user) throws BPException {

		logger.info("Executing CreateRun with workflowid: {}", workflowid);

		logger.debug("Instantiating BPEngine");

		BPEngine engine = new BPEngine();

		engine.setBpRunRegistry(getFactory().getBPRunRegistry());

		logger.debug("Lauching execute");

		BPRun run = engine.execute(workflowid, bprunRequest.getInputs(), user, bprunRequest.getName(), bprunRequest.getDescription(),
				bprunRequest.getInfra());

		if (logger.isDebugEnabled())
			logger.debug("Generated run with runid: {}", run.getRunid());

		return BPRunUtils.removeInfo(run, user.getEmail());
	}

	public BPRunStatus getStatus(String runid) throws BPException {

		logger.info("Executing GetStatus with runid: {}", runid);

		return getFactory().getBPRunStatusRegistry().getBPRunStatus(runid);

	}

	public BPRuns searchRuns(String text, Integer start, Integer count, BPUser user, String wfid) throws BPException {

		return getFactory().getBPRunRegistry().search(user, text, start, count, wfid);
	}

	BPRegistriesFactory getFactory() {
		return BPRegistriesFactory.getFactory();
	}

	public ValidateRealizationResponse validateRealization(JSONObject json) throws BPException {

		logger.debug("Parsing validate request");

		ValidateRealizationRequest request = null;

		try {

			request = new JSONDeserializer().deserialize(json.toString(), ValidateRealizationRequest.class);

		} catch (BPException e) {

			logger.error("Error parsing ValidateRealizationRequest: {}", json);

			BPException ex = new BPException("Error parsing ValidateRealizationRequest: " + json,
					BPException.ERROR_CODES.INVALID_REQUEST.getCode());

			ex.setUserMessage("An invalid request was submitted.");

			throw ex;

		}
		return validateRealization(request);
	}

	public ValidateRealizationResponse validateRealization(ValidateRealizationRequest request) throws BPException {

		logger.debug("Instantiating BPPreprocessor");

		BPPreprocessor preprocessor = getPreprocessor();

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		logger.info("Completed validation with result {}", response.getValid());

		return response;
	}

	BPPreprocessor getPreprocessor() {
		return new BPPreprocessor();
	}

	public LogMessagesResponse getLogs(String runid, Boolean head, String nextToken, BPUser user) throws BPException {

		logger.debug("Retrieving status of {}", runid);

		BPRunStatus status = getStatus(runid);

		logger.trace("Requesting logs to registry");

		return getFactory().getBPRunLogRegistry().getLogs(status, head, nextToken, user);
	}

	public APIWorkflowDetail getWorkflowDetail(String workflowid, BPUser user) throws BPException {
		logger.debug("Requesting {} to registry", workflowid);
		return getFactory().getExecutableRegistry().getWorkflowDetail(workflowid, user);

	}

	public DeleteWorkflowResponse deleteWorkflow(String workflowid, BPUser user) throws BPException {

		logger.debug("Requesting delete of {} to registry", workflowid);
		return getFactory().getExecutableRegistry().deleteWorkflow(workflowid, user);

	}

	public UpdateWorkflowResponse updateWorkflow(String workflowid, APIWorkflowDetail workflowDetail, BPUser user) throws BPException {

		logger.debug("Requesting update of {} to registry", workflowid);
		return getFactory().getExecutableRegistry().updateWorkflow(workflowid, workflowDetail, user);

	}

	public CreateWorkflowResponse createWorkflowDetail(APIWorkflowDetail workflow, BPUser user) throws BPException {
		logger.debug("Requesting {} to registry", workflow.getId());
		return getFactory().getExecutableRegistry().createWorkflow(workflow, user);

	}

	public SearchWorkflowsResponse searchWorkflows(String text, Integer start, Integer count, Boolean undertest, BPUser user)
			throws BPException {

		logger.debug("Sending search workflows with text {} ;; page {}-{} ;; include test {} to registry", text, start, (start + count - 1),
				undertest);

		return getFactory().getExecutableRegistry().search(text, start, count, undertest, user);

	}

	public BPInput getBPInput(String workflowid, String inputid, BPUser user) throws BPException {

		logger.debug("Loading all inputs of {}", workflowid);

		List<BPInput> outs = getBPInputs(workflowid, user).getInputs();

		return (BPInput) findObjectByid(inputid, outs);
	}

	public BPObject findObjectByid(String id, List<? extends BPObject> objects) throws BPException {

		logger.debug("Searching {} in retrieved objects (size: {})", id, objects.size());

		Optional<? extends BPObject> found = objects.stream().filter(o -> id.equalsIgnoreCase(o.getId())).findFirst();

		if (found.isPresent()) {
			logger.debug("Found requested object {} with name {}", id, found.get().getName());
			return found.get();
		}

		logger.warn("Not found object with id {}", id);

		throw new BPException("Can't find requested object " + id, BPException.ERROR_CODES.RESOURCE_NOT_FOUND);

	}

	private AtomicExecutableBP loadExecutable(String worflowid, BPUser user) throws BPException {
		logger.debug("Loading executable of {}", worflowid);
		return getFactory().getExecutableRegistry().getExecutable(worflowid, user);

	}

	public BPWorkflowOutputsResponse getBPOutputs(String worflowid, BPUser user) throws BPException {

		List<BPOutput> outputs = loadExecutable(worflowid, user).getOutputs().stream().toList();

		BPWorkflowOutputsResponse response = new BPWorkflowOutputsResponse();
		response.setOutputs(outputs);

		return response;

	}

	public BPWorkflowInputsResponse getBPInputs(String worflowid, BPUser user) throws BPException {

		List<BPInput> inputs = loadExecutable(worflowid, user).getInputs().stream().toList();

		BPWorkflowInputsResponse response = new BPWorkflowInputsResponse();
		response.setInputs(inputs);

		return response;
	}

	public BPOutput getBPOutput(String workflowid, String outputid, BPUser user) throws BPException {

		return loadOutputObject(workflowid, null, outputid, user, false);
	}

	public BPOutput getBPRunOutput(String runid, String outputid, BPUser user) throws BPException {
		return loadOutputObject(null, runid, outputid, user, true);
	}

	private BPOutput loadOutputObject(String workflowid, String runid, String outputid, BPUser user, Boolean runoutputs)
			throws BPException {

		List<BPOutput> outs;

		logger.debug("Loading all outputs of workflow id {} runid {}", workflowid, runid);

		if (Boolean.TRUE.equals(runoutputs))
			outs = getBPRunOutputs(runid, user).getOutputs();
		else
			outs = getBPOutputs(workflowid, user).getOutputs();

		return (BPOutput) findObjectByid(outputid, outs);
	}

	public BPWorkflowOutputsResponse getBPRunOutputs(String runid, BPUser user) throws BPException {

		logger.debug("Searching output values of run {}", runid);

		BPRun run = getRun(runid, user);

		String wid = run.getWorkflowid();

		logger.trace("Found run {} which refers to workflow {}", runid, wid);

		BPWorkflowOutputsResponse bpoutputs = getBPOutputs(wid, user);

		logger.trace("Searching status of {}", run);
		BPRunStatus status = getStatus(runid);

		logger.trace("Status of {}: {} {}", run, status.getStatus(), status.getResult());

		if (BPRunStatuses.COMPLETED.toString().equalsIgnoreCase(status.getStatus()) &&//
				BPRunResult.SUCCESS.toString().equalsIgnoreCase(status.getResult())) {

			setOutputValues(bpoutputs, runid);

		}

		return bpoutputs;

	}

	private void setOutputValues(BPWorkflowOutputsResponse bpoutputs, String runid) {

		String bucket = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_S3_OUTPUT_BUCKET_NAME.getParameter());

		String region = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter());

		String accesskey = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter());

		String secretkey = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter());

		String webstorageurl = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter());

		logger.trace("Setting value of outputs of run {}", runid);

		bpoutputs.getOutputs().forEach(o -> {
			try {
				setOutputValue(o, runid, bucket, region, accesskey, secretkey, webstorageurl);
			} catch (BPException bpException) {
				logger.error("BPException (code: {}) setting output value of {} of run id {}", bpException.getErroCode(), o.getId(), runid);
			}
		});

	}

	IWebStorage getWebStorage(String bucket) throws BPException {

		return new StorageFactory().getWebStorage(bucket);

	}

	IBPOutputWebStorage getBPOutputWebStorag(String bucket) throws BPException {

		return new StorageFactory().getBPOutputWebStorage(bucket);

	}

	void setOutputValue(BPOutput output, String runid, String bucket, String region, String accesskey, String secretkey,
			String webstorageurl) throws BPException {

		logger.trace("Setting value of output {} ({}) of run {}", output.getName(), output.getId(), runid);

		IBPOutputWebStorage bpOutputWebStorage = getBPOutputWebStorag(bucket);

		bpOutputWebStorage.addValue(output, runid, getWebStorage(bucket));

	}

}
