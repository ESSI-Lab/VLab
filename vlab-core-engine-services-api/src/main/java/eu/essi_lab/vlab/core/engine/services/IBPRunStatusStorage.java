package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPException;

/**
 * @author Mattia Santoro
 */
public interface IBPRunStatusStorage extends IBPConfigurableService {

	boolean store(BPRunStatus status);

	BPRunStatus get(String runid) throws BPException;

	boolean remove(String runid) throws BPException;
}
