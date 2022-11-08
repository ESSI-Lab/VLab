package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import java.util.Optional;

/**
 * @author Mattia Santoro
 */
public interface IBPRunRegistry {

	BPRun registerBPRun(BPRun run) throws BPException;

	Optional<BPRun> nextBPRun() throws BPException;

	void moveToTriggered(BPRun run) throws BPException;

	BPRun get(String runid, BPUser user) throws BPException;

	Boolean unregisterBPRun(BPRun run, BPUser user) throws BPException;

	BPRuns search(BPUser user, String text, int start, int count, String wfid) throws BPException;

	Boolean updateRun(BPRun run, BPUser user) throws BPException;

	void extendVisibilityTimeout(BPRun run, Integer seconds) throws BPException;

	void setBPRunStorage(IBPRunStorage runStorage);

	void setStatusRegistry(IBPRunStatusRegistry bpRunStatusRegistry);
}