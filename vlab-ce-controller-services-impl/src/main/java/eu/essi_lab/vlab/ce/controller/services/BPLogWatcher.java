package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLog;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPLogWatcher implements Runnable {

	private final IContainerOrchestratorCommandExecutor dockerContainerCommandExecutor;
	private BPLogChunk prevChunk = new BPLogChunk();
	private Logger logger = LogManager.getLogger(BPLogWatcher.class);
	private final String runid;
	private final String modelTaskId;

	private Integer sinceSeconds = 30;
	private final IBPRunLogStorage bpRunLogStorage;
	private Integer counter = 0;

	public BPLogWatcher(IContainerOrchestratorCommandExecutor dockerContainerCommandExecutor, String modelTaskId, String runid,
			IBPRunLogStorage bpRunLogStorage) {
		this.dockerContainerCommandExecutor = dockerContainerCommandExecutor;
		this.modelTaskId = modelTaskId;
		this.runid = runid;
		this.bpRunLogStorage = bpRunLogStorage;

	}

	@Override
	public void run() {

		counter++;

		try {
			doRun();
		} catch (BPException e) {

			logger.error("BPException reading logs (code {}) with message {}", e.getErroCode(), e.getMessage());

		} catch (Exception e) {

			logger.error("Exception reading logs", e);

		}

	}

	public Integer doRun() throws BPException {
		BPLogChunk chunck = this.dockerContainerCommandExecutor.readLogChunk(modelTaskId, Optional.empty(), Optional.of(sinceSeconds));

		BPLogChunk towrite = merge(chunck);

		Integer written = doWrite(towrite, runid, modelTaskId);

		prevChunk = towrite;

		return written;
	}

	private Integer doWrite(BPLogChunk towrite, String runid, String modelTaskId) {

		return bpRunLogStorage.writeLog(runid, modelTaskId, towrite);

	}

	private BPLogChunk merge(BPLogChunk newchunk) {

		logger.trace("Merging log messages");

		BPLog last = prevChunk.getSet().stream().max(BPLog::compareTo).orElseGet(() -> new BPLog(0L, 0L));

		logger.trace("Last TS from previous chunk {}", last.getTimestamp());

		Set<BPLog> jj = newchunk.getSet().stream().filter(bpLog ->

				(bpLog.getTimestamp() > last.getTimestamp() || (bpLog.getTimestamp() >= last.getTimestamp()
						&& bpLog.getNanostamp() > last.getNanostamp()))

		).collect(Collectors.toSet());

		logger.trace("Merging log messages - Completed with merged size {}", jj.size());

		return new BPLogChunk(jj);
	}

	public Integer getSinceSeconds() {
		return sinceSeconds;
	}

	public void setSinceSeconds(Integer sinceSeconds) {
		this.sinceSeconds = sinceSeconds;
	}

	public Integer getCounter() {
		return counter;
	}
}
