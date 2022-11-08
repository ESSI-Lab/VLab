package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.util.Observer;

/**
 * @author Mattia Santoro
 */
public interface IBPRunStatusRegistry extends Observer {

	Boolean createBPRunStatus(BPRunStatus status);

	Boolean updateBPRunStatus(BPRunStatus status);

	Boolean deleteBPRunStatus(BPRunStatus status) throws BPException;

	BPRunStatus getBPRunStatus(String runid) throws BPException;

	void setBPRunStatusStorage(IBPRunStatusStorage bpRunStatusStorage);
}
