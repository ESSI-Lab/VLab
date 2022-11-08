package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;

/**
 * @author Mattia Santoro
 */
public interface IBPRunLogStorage extends IBPConfigurableService {

	LogMessagesResponse readLog(String modeltaskid, Boolean head, String nextToken);

	Integer writeLog(String runid, String modelTaskId, BPLogChunk towrite);

	Boolean createLog(String runid, String modelTaskId);



}
