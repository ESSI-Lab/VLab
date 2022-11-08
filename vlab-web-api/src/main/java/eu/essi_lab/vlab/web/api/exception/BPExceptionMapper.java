package eu.essi_lab.vlab.web.api.exception;

import eu.essi_lab.vlab.core.client.BPErrorMapper;
import eu.essi_lab.vlab.core.datamodel.BPErrorMessage;
import eu.essi_lab.vlab.core.datamodel.BPException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPExceptionMapper implements ExceptionMapper<BPException> {

	private Logger logger = LogManager.getLogger(BPExceptionMapper.class);

	@Override
	public Response toResponse(BPException exception) {

		logger.error("BPException intercepted");

		BPErrorMessage err = new BPErrorMapper().toBPErrorMessage(exception);

		logger.error("BP Dev Error (code {}) {}", err.getCode(), err.getDevError());

		BPHttpErrorMessage httpErrorMessage = new BPHttpErrorMessage(err);

		return Response.status(httpErrorMessage.getHttpStatusCode()).entity(httpErrorMessage).build();

	}
}
