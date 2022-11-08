package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusStorage;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Mattia Santoro
 */
public class BPRunStatusRegistry implements Observer, IBPRunStatusRegistry {

	private IBPRunStatusStorage storage;

	@Override
	public Boolean createBPRunStatus(BPRunStatus status) {

		boolean stored = storage.store(status);
		if (stored)
			status.addObserver(this);

		return stored;
	}

	@Override
	public Boolean updateBPRunStatus(BPRunStatus status) {

		return storage.store(status);

	}

	@Override
	public Boolean deleteBPRunStatus(BPRunStatus status) throws BPException {

		return storage.remove(status.getRunid());

	}

	@Override
	public BPRunStatus getBPRunStatus(String runid) throws BPException {

		BPRunStatus status = storage.get(runid);

		status.addObserver(this);

		return status;

	}

	@Override
	public void setBPRunStatusStorage(IBPRunStatusStorage bpRunStatusStorage) {
		this.storage = bpRunStatusStorage;
	}

	@Override
	public void update(Observable o, Object arg) {
		updateBPRunStatus((BPRunStatus) o);
	}

}
