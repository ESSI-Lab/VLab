package eu.essi_lab.vlab.web.api.exception;

import eu.essi_lab.vlab.core.client.BPErrorMapper;
import eu.essi_lab.vlab.core.datamodel.BPErrorMessage;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class GenericExceptionMapper implements jakarta.ws.rs.ext.ExceptionMapper<Exception> {

	private Logger logger = LogManager.getLogger(GenericExceptionMapper.class);

	@Override
	public Response toResponse(Exception exception) {

		logger.error("Generic Exception intercepted");

		BPErrorMessage err = new BPErrorMapper().toBPErrorMessage(exception);

		logger.error("BP Dev Error (code {}) {}", err.getCode(), err.getDevError());

		BPHttpErrorMessage httpErrorMessage = new BPHttpErrorMessage(err);

		return Response.status(httpErrorMessage.getHttpStatusCode()).entity(httpErrorMessage).build();

	}
}
