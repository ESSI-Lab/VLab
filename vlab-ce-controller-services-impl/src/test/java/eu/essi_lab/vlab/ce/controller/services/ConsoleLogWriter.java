package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLog;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mattia Santoro
 */
public class ConsoleLogWriter implements IBPRunLogStorage {
	private Logger logger = LoggerFactory.getLogger(ConsoleLogWriter.class);

	@Override
	public LogMessagesResponse readLog(String modeltaskid, Boolean head, String nextToken) {
		return null;
	}

	@Override
	public Integer writeLog(String runid, String modelTaskId, BPLogChunk towrite) {

		logger.trace("Printing chunk");
		Iterator<BPLog> iterator = towrite.getSet().stream().sorted(BPLog::compareTo).collect(Collectors.toList()).iterator();

		Integer written = 0;
		while (iterator.hasNext()) {
			BPLog bplog = iterator.next();
			logger.trace("{} {} {}", bplog.getTimestamp(), bplog.getNanostamp(), bplog.getMessage());
			written++;
		}

		logger.trace("Printing chunk - Completed");
		return written;
	}

	@Override
	public Boolean createLog(String runid, String modelTaskId) {

		return Boolean.FALSE;
	}

	@Override
	public List<ConfigurationParameter> configurationParameters() {
		return null;
	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) throws BPException {

	}

	@Override
	public Boolean supports(String type) {
		return null;
	}
}
