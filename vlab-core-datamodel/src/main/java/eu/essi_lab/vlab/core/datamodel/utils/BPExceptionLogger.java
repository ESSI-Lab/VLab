package eu.essi_lab.vlab.core.datamodel.utils;

import eu.essi_lab.vlab.core.datamodel.BPException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPExceptionLogger {

	public static void logBPException(BPException ex, Logger logger, Level level) {

		logger.log(level, ex.toString());

	}
}
