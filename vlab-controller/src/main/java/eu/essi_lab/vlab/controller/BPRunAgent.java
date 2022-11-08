package eu.essi_lab.vlab.controller;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunPullResult;
import eu.essi_lab.vlab.core.datamodel.BPRunResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.PullResult;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPRunAgent {

	private IBPRunRegistry registry;
	private IBPRunStatusRegistry statusRegistry;
	private IExecutableBPRegistry exRegistry;
	private Logger logger = LogManager.getLogger(BPRunAgent.class);
	private static final String TO_TRIGGERED = " to triggered";

	private static final String RELEASING_RESOURCES_IF_NEEDED = "Release acquired resources if needed";
	private static final String MOVING = "Moving {}";
	private static final String MOVED = "Moved {}";
	private static final String STATUS_SET_TO_FAIL = "Status of {} was set to fail";
	private static final String STATUS_SETTING_TO_FAIL = "Setting status of {} to fail";
	private ScheduledExecutorService scheduler;

	Optional<BPRun> fetchNextRun() throws BPException {

		logger.trace("Fetching next run from queue");

		Optional<BPRun> run = registry.nextBPRun();

		if (!run.isPresent()) {

			logger.trace("Registry returned null");

			return Optional.empty();
		}

		logger.trace("Registry returned run {}", run.get().getRunid());

		return run;

	}

	/**
	 * Pulls the next {@link BPRun} from the {@link IBPRunRegistry} and tries to trigger its execution. If the execution has been triggered,
	 * the {@link BPRun} is removed from the registry.
	 */
	public BPRunPullResult pullRun() throws BPException {

		logger.info("Agent pull started");

		BPRunPullResult r = new BPRunPullResult();

		Optional<BPRun> optionalRun = fetchNextRun();

		if (!optionalRun.isPresent()) {

			logger.info("Queque is empty");

			r.setResult(PullResult.NO_QUEQUED_RUN);

			logger.info("Agent pull completed");

			return r;
		}

		BPRun run = optionalRun.get();

		logger.info("Next in queque is {}", run.getRunid());

		r = execute(run);

		logger.info("Agent pull completed");

		return r;

	}

	void cleanResources(BPRunner runner) throws BPException {
		runner.cleanBPResources();
	}

	public void cleanBPRunResources(BPRun run) throws BPException {

		BPRunner runner = new BPRunner(run);

		runner.setStatusRegistry(statusRegistry);

		runner.setExRegistry(exRegistry);

		runner.cleanBPResources();

	}

	public BPRunPullResult execute(BPRun run) throws BPException {

		BPRunner runner = getBPRunner(run);

		BPRunPullResult r = new BPRunPullResult();

		try {

			r = tryToExecute(runner);

		} catch (Exception throwable) {
			handleTryToExecuteException(r, runner, run, throwable);
		}

		return r;
	}

	void handleTryToExecuteException(BPRunPullResult r, BPRunner runner, BPRun run, Exception e) throws BPException {
		if (e instanceof BPException bpException)

			logger.error("Exception trying to execute {}: {}", run.getRunid(), bpException.toString());
		else
			logger.error("Unexpected exception trying to execute {} (exception message: {})", run.getRunid(), e.getMessage(), e);

		stopVisibilityTimeoutUpdate();

		logger.info(RELEASING_RESOURCES_IF_NEEDED);

		runner.releaseResources();

		logger.debug(MOVING, runner.getBPRun().getRunid() + TO_TRIGGERED);

		registry.moveToTriggered(runner.getBPRun());

		logger.debug(MOVED, runner.getBPRun().getRunid() + TO_TRIGGERED);

		logger.info(STATUS_SETTING_TO_FAIL, run.getRunid());

		BPRunStatus status = statusRegistry.getBPRunStatus(run.getRunid());

		status.setResult(BPRunResult.FAIL.toString());
		status.setStatus(BPRunStatuses.COMPLETED.toString());
		if (e instanceof BPException bpException)
			status.setMessage("Exception trying to execute " + run.getRunid() + " (engine code: " + bpException.getErroCode() + "): "
					+ e.getMessage());
		else
			status.setMessage("Unexpected exception trying to execute " + run.getRunid() + " (exception message: " + e.getMessage());

		statusRegistry.updateBPRunStatus(status);

		logger.info(STATUS_SET_TO_FAIL, run.getRunid());

		r.setRunid(run.getRunid());
		r.setResult(PullResult.EXECUTION_EXCEPTION);
	}

	public BPRunner getBPRunner(BPRun run) {
		BPRunner runner = new BPRunner(run);
		runner.setStatusRegistry(statusRegistry);

		runner.setExRegistry(exRegistry);

		return runner;
	}

	BPRunPullResult tryToExecute(BPRunner runner) throws BPException {

		BPRunPullResult result = new BPRunPullResult();

		logger.info("Trying to run {}", runner.getBPRun().getRunid());

		runner.setStatusRegistry(statusRegistry);

		runner.setExRegistry(exRegistry);

		scheduleVisibilityTimeoutUpdate(runner.getBPRun());

		BPRunStatus status = runner.runBusinessProcess();

		stopVisibilityTimeoutUpdate();

		if (!status.getStatus().equalsIgnoreCase(BPRunStatuses.QUEUED.toString())) {

			logger.debug("I'm a regular controller, moving to triggered");

			registry.moveToTriggered(runner.getBPRun());

			result.setResult(PullResult.EXECUTION_TRIGGERED);

			result.setRunid(runner.getBPRun().getRunid());

			logger.info("Triggered execution of BPRun {}", runner.getBPRun().getRunid());

			return result;
		}

		logger.info("No resource available for BPRun {}", runner.getBPRun().getRunid());

		result.setResult(PullResult.NO_RESOUCE_AVAILABLE);

		result.setRunid(runner.getBPRun().getRunid());

		runner.requireResources();

		logger.debug("Deleting resources");

		runner.cleanBPResources();

		return result;

	}

	void stopVisibilityTimeoutUpdate() {
		if (null != scheduler) {
			scheduler.shutdownNow();

		}
	}

	void scheduleVisibilityTimeoutUpdate(BPRun bpRun) {
		scheduler = Executors.newSingleThreadScheduledExecutor();

		scheduler.scheduleAtFixedRate(new VisibilityTimeoutExtender(registry, bpRun, 35), 20L, 10L, TimeUnit.SECONDS);
	}

	public void setRegistry(IBPRunRegistry registry) {
		this.registry = registry;
	}

	public void setStatusRegistry(IBPRunStatusRegistry statusRegistry) {
		this.statusRegistry = statusRegistry;
	}

	public void setExRegistry(IExecutableBPRegistry exRegistry) {
		this.exRegistry = exRegistry;
	}



}
