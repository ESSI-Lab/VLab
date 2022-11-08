package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;
import eu.essi_lab.vlab.core.datamodel.utils.BPRunUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPRunRegistry implements IBPRunRegistry {

	private IBPRunStatusRegistry statusRegistry;

	private IBPRunStorage storage;

	private Logger logger = LogManager.getLogger(BPRunRegistry.class);

	public BPRun registerBPRun(BPRun run) throws BPException {

		logger.debug("Registering new run for workflow: {}", run.getWorkflowid());

		if (null == run.getRunid() || "".equalsIgnoreCase(run.getRunid()))
			run.setRunid(UUID.randomUUID().toString());

		BPRunStatus status = new BPRunStatus(run.getRunid());

		status.setStatus(BPRunStatuses.QUEUED.toString());

		status.setWorkflowid(run.getWorkflowid());

		logger.trace("Created Run object with id {}. Registering status {}", run.getRunid(), status.getStatus());

		if (statusRegistry.createBPRunStatus(status)) {

			logger.trace("Registered status of run {}", run.getRunid());

			if (storage.queue(run)) {

				logger.trace("Registered new run {}", run.getRunid());

				status.setMessage("Run has been added to queque");

				return run;

			}

			status.setMessage("Unable to queque run " + run.getRunid());

			logger.warn("Unable to queue new run {}", run.getRunid());

			throw new BPException("Unable to queue new run " + run.getRunid(), BPException.ERROR_CODES.BPRUN_REGISTRY_ERROR.getCode());
		}

		logger.warn("Unable to queue status of new run {}", run.getRunid());

		throw new BPException("Unable to queue status of new run " + run.getRunid(),
				BPException.ERROR_CODES.STATUS_REGISTRY_ERROR.getCode());
	}

	public Optional<BPRun> nextBPRun() throws BPException {

		logger.debug("Fetching next run");

		Optional<BPRun> optionalRun = storage.nextQuequedBPRun();

		if (!optionalRun.isPresent()) {
			logger.info("No run in queque");

			return Optional.empty();
		}

		BPRun nextRun = optionalRun.get();

		logger.debug("Found queued run with runid: {}", nextRun.getRunid());

		boolean exists = storage.exists(nextRun.getRunid());

		while (!exists) {

			logger.info("Run {} was deleted, fetching next", nextRun.getRunid());

			storage.moveToTriggered(nextRun);

			optionalRun = storage.nextQuequedBPRun();

			if (!optionalRun.isPresent()) {
				logger.info("No run in queue");
				return Optional.empty();
			}

			nextRun = optionalRun.get();

			logger.debug("Found queued run with runid: {}", nextRun.getRunid());

			exists = storage.exists(nextRun.getRunid());

		}

		logger.debug("Returning {}", nextRun.getRunid());

		return Optional.of(nextRun);

	}

	public void moveToTriggered(BPRun run) throws BPException {
		logger.debug("Moving {} to triggered", run.getRunid());

		if (!storage.moveToTriggered(run))
			throw new BPException("Unable to move " + run.getRunid() + " to triggered set.", BPException.ERROR_CODES.ERR_MOVE_TO_TRIGGERED);
		logger.debug("Moved {} to triggered", run.getRunid());
	}

	public void setStatusRegistry(IBPRunStatusRegistry statusRegistry) {
		this.statusRegistry = statusRegistry;
	}

	@Override
	public void setBPRunStorage(IBPRunStorage storage) {
		this.storage = storage;
	}

	public BPRun get(String runid, BPUser user) throws BPException {
		logger.debug("Retrieving {}", runid);
		BPRun run = BPRunUtils.removeInfo(storage.get(runid, user.getEmail()), user.getEmail());
		logger.debug("Retrieved {}", runid);

		return run;
	}

	public Boolean unregisterBPRun(BPRun run, BPUser user) throws BPException {
		logger.debug("Removing {}", run.getRunid());
		boolean result = storage.remove(run, user.getEmail());
		logger.debug("Removed {}", run.getRunid());
		return result;
	}

	public BPRuns search(BPUser user, String text, int start, int count, String wfid) throws BPException {

		logger.debug("Searching {} {} {} {}", text, start, count, wfid);
		BPRuns runs = storage.search(user, text, start, count, wfid);

		BPRuns response = new BPRuns();

		response.setTotal(runs.getTotal());

		List<BPRun> list = new ArrayList<>();

		runs.getRuns().forEach(r -> list.add(BPRunUtils.removeInfo(r, user.getEmail())));

		response.setRuns(list);

		logger.debug("Completed searching {} {} {} {}", text, start, count, wfid);

		return response;

	}

	public Boolean updateRun(BPRun run, BPUser user) throws BPException {

		logger.debug("Updating {}", run.getRunid());

		boolean updatedrun = storage.udpdateRun(run, user.getEmail());
		logger.debug("Updated {}", run.getRunid());

		return updatedrun;
	}

	public void extendVisibilityTimeout(BPRun run, Integer seconds) throws BPException {
		logger.debug("Extending visibility of {} for {} seconds", run.getRunid(), seconds);
		storage.extendVisibilityTimeout(run, seconds);
		logger.debug("Extended visibility of {} for {} seconds", run.getRunid(), seconds);
	}
}
