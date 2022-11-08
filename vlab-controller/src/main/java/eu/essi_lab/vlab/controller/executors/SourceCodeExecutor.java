package eu.essi_lab.vlab.controller.executors;

import eu.essi_lab.vlab.controller.executors.ingest.ArrayInputManager;
import eu.essi_lab.vlab.controller.executors.ingest.IndividualInputManager;
import eu.essi_lab.vlab.controller.executors.ingest.ScriptUploaderManager;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.BPRunResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;

import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class SourceCodeExecutor implements Callable<BPRunStatus> {

	public static final String VLAB_ROOT_FOLDER = "/vlab";

	private boolean deleteSourceCodeDironExit;
	private BPRunStatus bpStatus;
	private List<BPInput> inputs = new ArrayList<>();
	private Map<String, BPInput> userInputMap = new HashMap<>();

	private Logger logger = LogManager.getLogger(SourceCodeExecutor.class);
	private boolean deleteDockerContainerFolerOnExit;

	private static final String ERRO_LOADING_DOCKER_IMAGE_FILE = "Error loading docker image description ";
	private static final String GENERICLOG = "[{}] {}";
	private static final String AS_INDIVIDUAL = "] as individual";
	private String testRoot;
	private IContainerOrchestratorCommandExecutor dockerExecutor;
	private ScriptUploaderManager uploader;
	private final ISourceCodeConnector sourceCodeConnector;
	private ContainerOrchestratorReservationResult reservedRes;
	private IContainerOrchestratorManager dockerExecutorManager;
	private ISourceCodeConventionFileLoader conventionFileLoader;

	public SourceCodeExecutor(ISourceCodeConnector connector) {

		sourceCodeConnector = connector;

	}

	@Override
	public BPRunStatus call() {

		try {

			logInfo("Started from Source in Folder: " + sourceCodeConnector.getDir().getName());

			bpStatus.setStatus(BPRunStatuses.INGESTING_INPUTS.toString());

			logTrace("Cpying inputs to map");

			copyInputs();

			logInfo("Ingesting inputs and scripts");

			ingestInputs();

			ingestScripts();

			logInfo("Preparing output folder");

			prepareOutputFolder();

			bpStatus.setStatus(BPRunStatuses.EXECUTING.toString());
			logInfo("Executing");
			execute();

			bpStatus.setStatus(BPRunStatuses.SAVING_OUTPUTS.toString());
			logInfo("Saving outputs");
			saveOputputs();

			bpStatus.setResult(BPRunResult.SUCCESS.toString());
			logInfo("Success");

		} catch (BPException ex) {

			bpStatus.setMessage(ex.getMessage());
			logError("BPException in main call method: " + ex.getMessage());

			this.bpStatus.setResult(BPRunResult.FAIL.toString());

		} catch (Exception thr) {

			logger.error("Exception caught", thr);

			bpStatus.setResult(BPRunResult.FAIL.toString());

			String msg = thr.getMessage();
			var append = "";

			if (msg != null && !msg.equalsIgnoreCase("") && !msg.equalsIgnoreCase("null"))
				append = " [" + msg + "] ";

			bpStatus.setMessage("Unknown Error" + append + ". Exit");
			logError("Unknown Error" + append + ". Exit");

		}

		if (deleteSourceCodeDironExit)
			try {
				cleanSourceCodeFolder();
			} catch (Exception thr) {

				logError("Failed to delete source folder directory" + sourceCodeConnector.getDir().getAbsolutePath());

			}

		logTrace("Releasing reservation");
		try {
			releaseReservation(getDockerExecutor(), reservedRes);
		} catch (Exception thr) {

			logError("Failed to release reservation for run " + bpStatus.getRunid());

		}

		SourceCodeExecutorResourceCleaner scerc = new SourceCodeExecutorResourceCleaner(bpStatus.getRunid());

		try {
			scerc.cleanManagerResources(getDockerExecutorManager());
		} catch (BPException e) {
			logError("Failed to clean container manager resources for run " + bpStatus.getRunid());

		}

		if (deleteDockerContainerFolerOnExit) {
			logTrace("Deleting run folder");

			try {
				scerc.cleanDockerContainerExecutionFolder(getDockerExecutor());
			} catch (Exception thr) {

				logError("Failed to delete docker container folder directory for run " + bpStatus.getRunid());

			}
		} else {
			logTrace("Deleting run folder Disabled");
		}
		bpStatus.setStatus(BPRunStatuses.COMPLETED.toString());
		logInfo("Completed");

		return bpStatus;

	}

	void copyInputs() {
		if (inputs != null) {
			for (BPInput i : inputs)
				userInputMap.put(i.getId(), i);
		}
	}

	private void logError(String error) {
		logger.error(GENERICLOG, bpStatus.getRunid(), error);
	}

	private void logInfo(String info) {
		logger.info(GENERICLOG, bpStatus.getRunid(), info);
	}

	private void logDebug(String debug) {
		logger.debug(GENERICLOG, bpStatus.getRunid(), debug);
	}

	private void logTrace(String trace) {
		logger.trace(GENERICLOG, bpStatus.getRunid(), trace);
	}

	void saveOputputs() throws BPException {

		bpStatus.setMessage("Parsing onput description file");
		logDebug("Reading outputs to save...");
		List<BPOutputDescription> outputs = readOutputs();

		for (BPOutputDescription output : outputs) {

			logDebug("Saving output " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]");

			bpStatus.setMessage("Saving output " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]");

			ContainerOrchestratorCommandResult result = saveOutput(output);

			logDebug("Result of saving output " + output.getOutput().getName() + " [" + output.getOutput().getId() + "] is " + result.isSuccess());

			if (result.isSuccess()) {

				bpStatus.setMessage("Successfully saved output " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]");

				logDebug("Successfully saved output " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]");

			} else {

				var sb = new StringBuilder();

				sb.append("Can't save output ").append(output.getOutput().getName()).append(" [").append(output.getOutput().getId()).append("]").toString();

				if (result.getMessage() != null)
					sb.append(result.getMessage());

				var errMsg = sb.toString();

				logError(errMsg);

				bpStatus.setMessage(errMsg);
				this.bpStatus.setResult(BPRunResult.FAIL.toString());
				throw new BPException(errMsg, BPException.ERROR_CODES.SAVE_OUTPUT_ERROR);

			}
		}

	}

	ContainerOrchestratorCommandResult saveOutput(BPOutputDescription output) throws BPException {

		String defaultInputType = output.getOutput().getOutputType();

		if (defaultInputType.equalsIgnoreCase("individual")) {

			logTrace("Saving " + output.getOutput().getName() + " [" + output.getOutput().getId() + AS_INDIVIDUAL);

			var individualManager = getInputManager();

			individualManager.setPathParser(new PathConventionParser(bpStatus.getRunid(), VLAB_ROOT_FOLDER));

			individualManager.setDockerHost(getDockerExecutor());

			ContainerOrchestratorCommandResult r = individualManager.save(output);

			logTrace("Individual manager completed saving " + output.getOutput().getName() + " [" + output.getOutput().getId() + AS_INDIVIDUAL);

			return r;

		}

		logTrace("Saving " + output.getOutput().getName() + " [" + output.getOutput().getId() + "] as array");

		var arrayManager = getArrayManager();

		arrayManager.setPathParser(new PathConventionParser(bpStatus.getRunid(), VLAB_ROOT_FOLDER));

		arrayManager.setDockerHost(getDockerExecutor());

		return arrayManager.save(output);

	}

	void execute() throws BPException {

		VLabDockerImage image = null;

		try {

			image = getConventionFileLoader().loadDockerImage();

		} catch (BPException e) {

			logger.error(ERRO_LOADING_DOCKER_IMAGE_FILE);

			throw new BPException(ERRO_LOADING_DOCKER_IMAGE_FILE + e.getMessage(), e.getErroCode());
		}

		bpStatus.setMessage("Started model execution");

		ContainerOrchestratorCommandResult result = getDockerExecutor().runImage(image, reservedRes,
				new PathConventionParser(bpStatus.getRunid(), VLAB_ROOT_FOLDER).getRunFolder(), 1000L * 60 * 60 * 24 * 7,
				bpStatus.getRunid(), bpStatus);

		if (!result.isSuccess()) {

			throw new BPException("Error executing model: " + result.getMessage(), BPException.ERROR_CODES.MODEL_EXECUTION_ERROR);

		}

		bpStatus.setMessage("Completed model execution");

	}

	void ingestScripts() throws BPException {

		bpStatus.setMessage("Parsing script file");
		List<Script> scripts = readScriptsToCopy();

		for (Script script : scripts) {

			bpStatus.setMessage("Ingesting script file(s) from " + script.getRepoPath());

			uploader.setLoader(getConventionFileLoader());

			uploader.setDockerHost(getDockerExecutor());
			uploader.setPathParser(new PathConventionParser(bpStatus.getRunid(), getVLabRootFolder()));

			ContainerOrchestratorCommandResult result = uploader.ingest(script);

			if (!result.isSuccess()) {

				var sb = new StringBuilder();

				sb.append("Error ingesting scripts");

				String msg = result.getMessage();

				if (msg != null)
					sb.append("[").append(msg).append("]");

				throw new BPException(sb.toString(), BPException.ERROR_CODES.ERROR_INGESTING_SCRIPTS);
			}

			bpStatus.setMessage("Successfully ingested scripts file(s) from " + script.getRepoPath());

		}

	}

	public IContainerOrchestratorCommandExecutor getDockerExecutor() throws BPException {

		if (dockerExecutor == null)
			dockerExecutor = getDockerExecutorManager().getExecutor();

		return dockerExecutor;

	}

	public void setIDockerContainerCommandExecutorManager(IContainerOrchestratorManager executorManager) {
		dockerExecutorManager = executorManager;
	}

	public IContainerOrchestratorManager getDockerExecutorManager() {

		return dockerExecutorManager;

	}

	private String getVLabRootFolder() {

		if (testRoot != null)
			return testRoot;

		return VLAB_ROOT_FOLDER;
	}

	private void deleteFolder() {
		sourceCodeConnector.deleteCodeFolder();
	}

	public void cleanSourceCodeFolder() {

		logTrace("Deleting source folder directory");

		deleteFolder();

	}

	void prepareOutputFolder() throws BPException {

		bpStatus.setMessage("Parsing output file");

		List<BPOutputDescription> outputs = readOutputs();

		for (BPOutputDescription output : outputs) {

			if (Boolean.TRUE.equals(output.getOutput().getCreateFolder())) {

				bpStatus.setMessage("Preparing output folder for " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]");

				ContainerOrchestratorCommandResult result = createOutputFolder(output);

				if (result.isSuccess()) {

					bpStatus.setMessage("Successfully created output folder for " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]");

				} else {

					String errMsg = "Can't create folder for output " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]" + (
							result.getMessage() != null ? result.getMessage() : "");

					throw new BPException(errMsg, BPException.ERROR_CODES.CREATE_OUTPUT_FOLDER_ERROR);

				}
			} else {
				bpStatus.setMessage("Output " + output.getOutput().getName() + " [" + output.getOutput().getId() + "] does NOT require folder "
						+ "creation");
			}
		}

	}

	private ContainerOrchestratorCommandResult createOutputFolder(BPOutputDescription output) throws BPException {

		logTrace("Requesting to create forlder for " + output.getOutput().getName() + " [" + output.getOutput().getId() + AS_INDIVIDUAL + ", folder is "
				+ output.getTarget());

		IndividualInputManager individualManager = getInputManager();

		individualManager.setPathParser(new PathConventionParser(bpStatus.getRunid(), VLAB_ROOT_FOLDER));

		individualManager.setDockerHost(getDockerExecutor());

		ContainerOrchestratorCommandResult r = individualManager.createFolder(output);

		logTrace("Individual manager completed creating folder for  " + output.getOutput().getName() + " [" + output.getOutput().getId() + AS_INDIVIDUAL);

		return r;

	}

	void ingestInputs() throws BPException {

		bpStatus.setMessage("Parsing default input file");
		List<BPInputDescription> defInputs = readDefaultInputs();

		for (BPInputDescription defaultInput : defInputs) {

			bpStatus.setMessage("Ingesting input " + defaultInput.getInput().getName() + " [" + defaultInput.getInput().getId() + "]");
			ContainerOrchestratorCommandResult result = ingestInput(defaultInput, userInputMap.get(defaultInput.getInput().getId()));

			if (result.isSuccess()) {

				userInputMap.remove(defaultInput.getInput().getId());

				bpStatus.setMessage("Successfully ingested input " + defaultInput.getInput().getName() + " [" + defaultInput.getInput().getId() + "]");

			} else {

				String errMsg = "Can't ingest input " + defaultInput.getInput().getName() + " [" + defaultInput.getInput().getId() + "]" + (
						result.getMessage() != null ? result.getMessage() : "");

				throw new BPException(errMsg, BPException.ERROR_CODES.ERROR_INGESTING_INPUT);

			}
		}

	}

	IndividualInputManager getInputManager() {
		return new IndividualInputManager(bpStatus.getRunid());
	}

	ArrayInputManager getArrayManager() {
		return new ArrayInputManager(bpStatus.getRunid());
	}

	private ContainerOrchestratorCommandResult ingestInput(BPInputDescription defaultInput, BPInput bpInput) throws BPException {

		String defaultInputType = defaultInput.getInput().getInputType();

		if (defaultInputType.equalsIgnoreCase("individual")) {

			IndividualInputManager individualManager = getInputManager();

			individualManager.setPathParser(new PathConventionParser(bpStatus.getRunid(), VLAB_ROOT_FOLDER));

			individualManager.setDockerHost(getDockerExecutor());

			return individualManager.ingest(defaultInput, bpInput);

		}

		var arrayInputManager = getArrayManager();

		arrayInputManager.setPathParser(new PathConventionParser(bpStatus.getRunid(), VLAB_ROOT_FOLDER));

		arrayInputManager.setDockerHost(getDockerExecutor());

		arrayInputManager.setStatus(bpStatus);

		return arrayInputManager.ingest(defaultInput, bpInput);

	}

	private List<Script> readScriptsToCopy() throws BPException {

		try {

			return getConventionFileLoader().loadScripts();

		} catch (BPException e) {

			logger.error("Error loading scripts description");

			bpStatus.setStatus(BPRunStatuses.COMPLETED.toString());

			bpStatus.setResult(BPRunResult.FAIL.toString());

			bpStatus.setMessage("Error reading scripts to copy " + e.getMessage() + " (vlab error code " + e.getErroCode() + ")");

			throw new BPException("Error reading scripts to copy " + e.getMessage(), e.getErroCode());
		}
	}

	private List<BPInputDescription> readDefaultInputs() throws BPException {

		try {

			return getConventionFileLoader().loadIOFile().getInputs();

		} catch (BPException e) {

			logger.error("Error loading input description");

			bpStatus.setStatus(BPRunStatuses.COMPLETED.toString());

			bpStatus.setResult(BPRunResult.FAIL.toString());

			bpStatus.setMessage("Error reading default inputs " + e.getMessage() + " (vlab error code " + e.getErroCode() + ")");

			throw new BPException("Error reading default inputs " + e.getMessage(), e.getErroCode());
		}

	}

	List<BPOutputDescription> readOutputs() throws BPException {

		try {

			return getConventionFileLoader().loadIOFile().getOutputs();

		} catch (BPException e) {

			logger.error("Error loading output description");

			bpStatus.setStatus(BPRunStatuses.COMPLETED.toString());

			bpStatus.setResult(BPRunResult.FAIL.toString());

			bpStatus.setMessage("Error reading outputs description " + e.getMessage() + " (vlab error code: " + e.getErroCode() + ")");

			throw new BPException("Error reading outputs description " + e.getMessage(), e.getErroCode());
		}

	}

	public void setBPStatus(BPRunStatus status) {
		this.bpStatus = status;
	}

	public List<BPInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<BPInput> inputs) {
		this.inputs = inputs;
	}

	public ContainerOrchestratorReservationResult acquireResources() throws BPException {

		VLabDockerImage image = null;

		try {

			image = getConventionFileLoader().loadDockerImage();

		} catch (BPException e) {

			logger.error(ERRO_LOADING_DOCKER_IMAGE_FILE);

			throw new BPException("Resource acquisition failed due to " + ERRO_LOADING_DOCKER_IMAGE_FILE + e.getMessage() + " with code "
					+ e.getErroCode(), BPException.ERROR_CODES.RESOURCES_RESERVATION_ERROR);

		}

		VLabDockerResources resources = image.getResources();

		return doAcquireResources(resources, getDockerExecutor());

	}

	ContainerOrchestratorReservationResult doAcquireResources(VLabDockerResources resources, IContainerOrchestratorCommandExecutor executor)
			throws BPException {

		if (resources != null) {

			reservedRes = executor.reserveResources(resources);

			logDebug("Resources Acquired with result " + reservedRes.isAcquired() + " and task arn " + reservedRes.getTaskArn());

			return reservedRes;

		}

		throw new BPException("No resources specified in acquire request", BPException.ERROR_CODES.NO_REQUIRED_RESOURCES_FOUND);
	}

	public void releaseReservation(IContainerOrchestratorCommandExecutor executor, ContainerOrchestratorReservationResult reservationResult)
			throws BPException {
		executor.release(reservationResult);
	}

	public void setDeleteSourceCodeDironExit(Boolean deleteSourceCodeDironExit) {
		this.deleteSourceCodeDironExit = deleteSourceCodeDironExit;
	}

	public void setDeleteDockerContainerFolerOnExit(boolean deleteDockerContainerFolerOnExit) {
		this.deleteDockerContainerFolerOnExit = deleteDockerContainerFolerOnExit;
	}

	public void setTestRoot(String testRoot) {
		this.testRoot = testRoot;
	}

	public void setDockerExecutor(IContainerOrchestratorCommandExecutor dockerExecutor) {
		this.dockerExecutor = dockerExecutor;
	}

	public void setDockerExecutorManager(IContainerOrchestratorManager dockerExecutorManager) {
		this.dockerExecutorManager = dockerExecutorManager;
	}

	public void setUploader(ScriptUploaderManager uploader) {
		this.uploader = uploader;
	}

	public ScriptUploaderManager getUploader() {
		return this.uploader;
	}

	public ISourceCodeConventionFileLoader getConventionFileLoader() {
		return conventionFileLoader;
	}

	public void setConventionFileLoader(ISourceCodeConventionFileLoader conventionFileLoader) {
		this.conventionFileLoader = conventionFileLoader;
	}
}
