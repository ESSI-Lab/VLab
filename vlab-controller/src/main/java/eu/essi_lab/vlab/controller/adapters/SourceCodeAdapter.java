package eu.essi_lab.vlab.controller.adapters;

import eu.essi_lab.vlab.controller.executors.SourceCodeExecutor;
import eu.essi_lab.vlab.controller.executors.SourceCodeExecutorResourceCleaner;
import eu.essi_lab.vlab.controller.executors.ingest.ScriptUploaderManager;
import eu.essi_lab.vlab.controller.factory.DockerHostCommandExecutorFactory;
import eu.essi_lab.vlab.controller.services.IBPAdapter;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.BPRunResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.datamodel.utils.BPExceptionLogger;
import eu.essi_lab.vlab.core.engine.factory.ISourceCodeConectorFactory;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class SourceCodeAdapter implements IBPAdapter {

	private Logger logger = LogManager.getLogger(SourceCodeAdapter.class);
	private List<BPInput> inputList;
	private SourceCodeExecutor sce;
	private ISourceCodeConnector connector;
	private BPComputeInfrastructure targetComputeInfrastructure;
	private IContainerOrchestratorManager dccem;

	protected SourceCodeExecutor getSourceCodeExecutor() throws BPException {

		if (sce == null) {

			dccem = initIContainerOrchestratorManager();

			sce = new SourceCodeExecutor(connector);
			sce.setUploader(new ScriptUploaderManager());
			sce.setDockerExecutor(dccem.getExecutor());
			sce.setDockerExecutorManager(dccem);
			sce.setIDockerContainerCommandExecutorManager(dccem);

			sce.setConventionFileLoader(getSourceCodeConventionFileLoader());

		}

		return sce;
	}

	IContainerOrchestratorManager initIContainerOrchestratorManager() throws BPException {
		return new DockerHostCommandExecutorFactory().getExecutor(targetComputeInfrastructure);
	}

	@Override
	public ContainerOrchestratorReservationResult acquireResources(BPRunStatus status) throws BPException {

		logger.debug("Acquiring resources");

		var executor = getSourceCodeExecutor();

		executor.setBPStatus(status);

		ContainerOrchestratorReservationResult acquired = executor.acquireResources();

		logger.debug("Acquired of run {}: {}", status.getRunid(), acquired.isAcquired());

		return acquired;

	}

	@Override
	public void setInputs(List<BPInput> inputs) {
		this.inputList = inputs;

	}

	@Override
	public void execute(BPRunStatus status) {

		launchExecutor(status);

	}

	protected void launchExecutor(BPRunStatus status) {

		logger.info("Submitting execution of {}", status.getRunid());

		ExecutorService ex = Executors.newFixedThreadPool(1);

		SourceCodeExecutor executor = null;
		try {
			executor = getSourceCodeExecutor();
		} catch (BPException e) {

			logger.error("Error obtaining source code executor, setting status to fail");
			status.setMessage("Error obtaining source code executor");
			status.setResult(BPRunResult.FAIL.toString());
			status.setStatus(BPRunStatuses.COMPLETED.toString());

			return;

		}

		executor.setDeleteSourceCodeDironExit(true);

		executor.setDeleteDockerContainerFolerOnExit(true);

		executor.setBPStatus(status);

		executor.setInputs(getInputs());

		ex.submit(executor);

		logger.info("Submitted execution of {}", status.getRunid());
	}

	@Override
	public Boolean supports(BPRealization realization) {

		try {
			ISourceCodeConnector codeConnector = new ISourceCodeConectorFactory().getConnector(realization);

			setConnector(codeConnector);

			return Boolean.TRUE;
		} catch (BPException e) {
			BPExceptionLogger.logBPException(e, logger, Level.WARN);
		}

		return Boolean.FALSE;
	}

	@Override
	public void setConnector(ISourceCodeConnector codeConector) {
		connector = codeConector;
	}

	@Override
	public void releaseResources(ContainerOrchestratorReservationResult resourcesAcquired) throws BPException {
		var executor = getSourceCodeExecutor();
		executor.releaseReservation(executor.getDockerExecutor(), resourcesAcquired);
	}

	@Override
	public void setTargetComputeInfrastructure(BPComputeInfrastructure infra) {
		this.targetComputeInfrastructure = infra;
	}

	@Override
	public void cleanBPRunResources(String runid) throws BPException {

		BPException ex = null;

		try {
			doCleanSourceExecutorResources(runid);
		} catch (BPException e) {
			logger.warn("BPExcception during doCleanSourceExecutorResources, keep cleaning other resources if needed and then this "
					+ "exception will be thrown");
			ex = e;
		}

		connector.deleteCodeFolder();

		if (null != ex)
			throw ex;

	}

	void doCleanSourceExecutorResources(String runid) throws BPException {

		var cleaner = new SourceCodeExecutorResourceCleaner(runid);

		cleaner.cleanAll(getSourceCodeExecutor().getDockerExecutorManager());
	}

	@Override
	public VLabDockerResources getRequiredResources() throws BPException {

		return getSourceCodeConventionFileLoader().loadDockerImage().getResources();

	}

	public List<BPInput> getInputs() {
		return inputList;
	}

	ISourceCodeConventionFileLoader getSourceCodeConventionFileLoader() throws BPException {
		return new ISourceCodeConectorFactory().getSourceCodeConventionFileLoader(connector.getDir().getAbsolutePath());
	}

}
