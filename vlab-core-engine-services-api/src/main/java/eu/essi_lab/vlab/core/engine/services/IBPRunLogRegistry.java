package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;

/**
 * @author Mattia Santoro
 */
public interface IBPRunLogRegistry {

	LogMessagesResponse getLogs(BPRunStatus status, Boolean head, String nextToken, BPUser user);

	IBPRunLogStorage getStorage();

	void setBPRunLogStorage(IBPRunLogStorage bpRunLogStorage);
}
