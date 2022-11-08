package eu.essi_lab.vlab.controller.executable;

import eu.essi_lab.vlab.controller.BPRunAgent;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRunPullResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.engine.factory.BPRegistriesFactory;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPEngineCommandLineExecutor {

	private Logger logger = LogManager.getLogger(BPEngineCommandLineExecutor.class);
	private IExecutableBPRegistry exRegistry;
	private IBPRunRegistry registry;
	private IBPRunStatusRegistry statusRegistry;
	private BPRegistriesFactory factory;

	public void execCmd(String[] args) throws BPException {

		if (args.length < 1) {
			logger.error("No argument, exit 1");
			throw new BPException("No argument, exit 1", BPException.ERROR_CODES.INVALID_REQUEST);
		}

		String cmd = args[0];

		if (args.length > 1) {
			ConfigurationLoader.setConfFilePath(args[1]);
		}

		logger.info("Launched with arg {}", cmd);

		EngineCmdRequest request = null;
		try {
			request = decodeRequest(cmd);
		} catch (BPException e) {

			String msg = "Exception decoding command (engine code: " + e.getErroCode() + "): " + e.getMessage();
			logger.error(msg);

			throw new BPException(msg, BPException.ERROR_CODES.INVALID_REQUEST);
		}

		logger.info("Initializing registries");

		try {

			if (factory == null)
				throw new BPException("No registry factory defined", BPException.ERROR_CODES.BAD_CONFIGURATION);

			initRegistries(factory);

		} catch (BPException e) {

			String msg = "BPException initializing registries (engine code: " + e.getErroCode() + "): " + e.getMessage();
			logger.error(msg);

			handleInitRegistryException(request, e);

			return;

		} catch (Exception e) {

			var msg = "Exception initializing registries";
			logger.error(msg, e);

			handleInitRegistryException(request, e);

			return;

		}

		logger.info("Executing {}", request);

		try {

			executeRequest(request);

		} catch (BPException e) {

			String msg = "Exception executing command (engine code: " + e.getErroCode() + "): " + e.getMessage();
			logger.error(msg);

			throw e;

		}

		logger.info("Completed {}", request);
	}

	private void handleInitRegistryException(EngineCmdRequest request, Exception e) throws BPException {

		switch (request) {

		default:
			logger.info("Re-throw original exception");

			if (e instanceof BPException)
				throw (BPException) e;

			throw new BPException(e.getMessage(), BPException.ERROR_CODES.UNKNOWN);

		}

	}

	private void executeRequest(EngineCmdRequest request) throws BPException {

		switch (request) {

		case PULL: {

			logger.debug("Start pulling... ");

			BPRunPullResult pullResult = null;
			try {

				pullResult = execPull();

			} catch (BPException e) {

				String msg = "Exception pulling next run (engine code: " + e.getErroCode() + "): " + e.getMessage();
				logger.error(msg);

				throw new BPException(msg, BPException.ERROR_CODES.BPRUN_PULL_ERROR.getCode());

			}

			logger.debug("Pull result {}", pullResult.getResult());

			try {

				wait(pullResult);

			} catch (BPException e) {

				String msg = "Exception handling pull result (engine code: " + e.getErroCode() + "): " + e.getMessage();

				logger.error(msg);

				throw new BPException(msg, BPException.ERROR_CODES.BPRUN_PULL_RESULT_HANDLING_ERROR.getCode());

			}

			return;
		}

		default:

		}

	}

	void wait(BPRunPullResult pullResult) throws BPException {

		logger.debug("Pull result is {}", pullResult.getResult());

		var status = pullResult.getResult();

		switch (status) {
		case EXECUTION_TRIGGERED:
			String runid = pullResult.getRunid();

			LogManager.getLogger(BPEngineCommandLine.class).trace("Waiting for triggered execution to complete (run id: {})", runid);

			try {
				var bpStatus = statusRegistry.getBPRunStatus(pullResult.getRunid());

				while (!bpStatus.getStatus().equalsIgnoreCase(BPRunStatuses.COMPLETED.toString())) {

					bpStatus = statusRegistry.getBPRunStatus(runid);

					logger.trace("Status of {}: {}", runid, bpStatus.getStatus());
					logger.trace("Message of {}: {}", runid, bpStatus.getMessage());

					Thread.sleep(5000L);

				}

			} catch (BPException e) {

				throw new BPException("Unable to retrieve status of run " + runid,
						BPException.ERROR_CODES.BPSTATUSRUN_REGISTRY_ERROR.getCode());

			} catch (InterruptedException e) {

				logger.error("Interrupted exception requesting status of {}", runid, e);
				//This is suggested by Sonar as best practice
				Thread.currentThread().interrupt();
			}

			break;

		case NO_QUEQUED_RUN:
			logger.trace("No quequed run was found");
			return;

		case NO_RESOUCE_AVAILABLE:
			logger.trace("Resources not available for run {}", pullResult.getRunid());
			return;

		case EXECUTION_EXCEPTION:
			logger.trace("Exception in pull execution for run {}", pullResult.getRunid());
			return;
		}

	}

	BPRunPullResult execPull() throws BPException {

		BPRunAgent agent = initAgent();

		return agent.pullRun();

	}

	BPRunAgent initAgent() {
		logger.debug("Creating agent...");

		var agent = new BPRunAgent();

		logger.trace("Obtaining BPRunStatusRegistry...");

		agent.setStatusRegistry(statusRegistry);

		logger.trace("Obtaining BPRunRegistry...");

		agent.setRegistry(registry);

		agent.setExRegistry(exRegistry);
		logger.debug("Agent created");
		return agent;
	}

	void initRegistries(BPRegistriesFactory factory) throws BPException {
		statusRegistry = factory.getBPRunStatusRegistry();
		registry = factory.getBPRunRegistry();
		exRegistry = factory.getExecutableRegistry();
	}

	private EngineCmdRequest decodeRequest(String request) throws BPException {

		if (request == null || request.equalsIgnoreCase("")) {

			throw new BPException("No request received", BPException.ERROR_CODES.NO_REQUEST_FOUND.getCode());

		}

		for (EngineCmdRequest v : EngineCmdRequest.values()) {

			if (request.equalsIgnoreCase(v.toString()))

				return v;

		}

		throw new BPException("Request not supported: " + request, BPException.ERROR_CODES.UNKNOWN_REQUEST_FOUND.getCode());

	}

	public void setBPRegistriesFactory(BPRegistriesFactory factory) {
		this.factory = factory;
	}
}
