package eu.essi_lab.vlab.core.client;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPErrorMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPErrorMapper {

	private Logger logger = LogManager.getLogger(BPErrorMapper.class);

	public BPErrorMessage toBPErrorMessage(BPException bpException) {
		return toBPErrorMessage(bpException, new BPErrorMessage());
	}

	public BPErrorMessage toBPErrorMessage(BPException bpException, BPErrorMessage err) {

		err.setError(bpException.getUserMessage());

		err.setDevError(bpException.getMessage());

		err.setCode(bpException.getErroCode());

		return err;
	}

	public BPErrorMessage toBPErrorMessage(Exception bpException) {
		return toBPErrorMessage(bpException, new BPErrorMessage());
	}

	public BPErrorMessage toBPErrorMessage(Exception exception, BPErrorMessage err) {
		String userError = "An error occourred processing your request. Administrators were notified.";

		String devError = exception.getClass().getName() + ": " + exception.getMessage();

		logger.error("Exception: {}", devError, exception);

		err.setError(userError);

		err.setDevError(devError);

		err.setCode(BPException.ERROR_CODES.UNKNOWN.getCode());

		return err;
	}
}
