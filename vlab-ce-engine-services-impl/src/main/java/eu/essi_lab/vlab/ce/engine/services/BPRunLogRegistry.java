package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.LogEvent;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPRunLogRegistry implements IBPRunLogRegistry {

	private Logger logger = LogManager.getLogger(BPRunLogRegistry.class);

	private static final String NOT_STARTED_MESSAGE = "Model Execution has not produced any log yet.";
	private IBPRunLogStorage storage;

	@Override
	public LogMessagesResponse getLogs(BPRunStatus status, Boolean head, String nextToken, BPUser user) {

		//TODO authorize user
		String modeltaskid = status.getModelTaskId();

		logger.debug("Requesting log of model task id {}", modeltaskid);

		if (null == modeltaskid || "".equalsIgnoreCase(modeltaskid)) {
			logger.info("Model task id not found for {}", status.getRunid());
			return notStarted();
		}

		return storage.readLog(modeltaskid, head, nextToken);

	}

	private LogMessagesResponse notStarted() {

		LogMessagesResponse response = new LogMessagesResponse();

		LogEvent event = new LogEvent();
		event.setMessage(NOT_STARTED_MESSAGE);

		response.setEvents(Arrays.asList(event));

		return response;
	}

	@Override
	public IBPRunLogStorage getStorage() {
		return storage;
	}

	@Override
	public void setBPRunLogStorage(IBPRunLogStorage storage) {
		this.storage = storage;
	}

}
