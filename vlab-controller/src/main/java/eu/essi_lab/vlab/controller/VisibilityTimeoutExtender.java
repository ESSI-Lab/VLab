package eu.essi_lab.vlab.controller;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class VisibilityTimeoutExtender implements Runnable {

	private Logger logger = LogManager.getLogger(VisibilityTimeoutExtender.class);

	private final IBPRunRegistry registry;
	private final BPRun run;
	private final Integer extensionSeconds;

	public VisibilityTimeoutExtender(IBPRunRegistry bpRunRegistry, BPRun bpRun, Integer extensionSeconds) {

		this.registry = bpRunRegistry;
		this.run = bpRun;
		this.extensionSeconds = extensionSeconds;
	}

	@Override
	public void run() {

		try {
			this.registry.extendVisibilityTimeout(run, extensionSeconds);
		} catch (BPException e) {
			logger.error("Can't extend visibility of run {} (code: {}): {}", run.getRunid(), e.getErroCode(), e.getMessage());
		}

	}
}
