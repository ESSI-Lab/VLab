package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class ModelLogCollector {

	private Logger logger = LogManager.getLogger(ModelLogCollector.class);

	private final IContainerOrchestratorCommandExecutor dockerContainerCommandExecutor;
	private final IBPRunLogStorage runLogStorage;

	private BPLogWatcher watcher;
	private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	public ModelLogCollector(IContainerOrchestratorCommandExecutor dockerContainerCommandExecutor, IBPRunLogStorage logWriter) {
		this.dockerContainerCommandExecutor = dockerContainerCommandExecutor;
		this.runLogStorage = logWriter;
	}

	public void addModelLog(String modelTaskId, String runid) {
		logger.info("Adding model log of {}", runid);
		Boolean createdLog = runLogStorage.createLog(runid, modelTaskId);
		logger.info("Created model log of {} with result {}", runid, createdLog);
	}

	public void watch(String modelTaskId, String runid) {

		if (null != modelTaskId) {

			doWatchLog(modelTaskId, runid);

		} else
			logger.info("No model task id found for run {}", runid);
	}

	private void doWatchLog(String modelTaskId, String runid) {

		watcher = new BPLogWatcher(this.dockerContainerCommandExecutor, modelTaskId, runid, this.runLogStorage);

		watcher.setSinceSeconds(11);

		scheduledExecutor.scheduleAtFixedRate(watcher, 0L, 10L, TimeUnit.SECONDS);

	}

	public void finalizeModelLogCollection(String modelTaskId, String runid) {

		logger.debug("Shut down log watcher of {} with task id {}", runid, modelTaskId);
		scheduledExecutor.shutdownNow();
		if (null != watcher) {
			logger.debug("Invoke last watch");
			watcher.setSinceSeconds(30);
			watcher.run();
		} else
			logger.debug("No watcher to launch");
	}
}
