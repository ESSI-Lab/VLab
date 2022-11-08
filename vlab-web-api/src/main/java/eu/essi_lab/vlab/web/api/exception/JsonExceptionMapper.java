package eu.essi_lab.vlab.web.api.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.essi_lab.vlab.core.datamodel.BPErrorMessage;
import eu.essi_lab.vlab.core.datamodel.BPException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class JsonExceptionMapper implements ExceptionMapper<JsonProcessingException> {

	private Logger logger = LogManager.getLogger(JsonExceptionMapper.class);

	@Override
	public Response toResponse(JsonProcessingException exception) {

		logger.error("JsonProcessingException intercepted");

		BPErrorMessage err = new BPErrorMessage();
		err.setCode(BPException.ERROR_CODES.INVALID_REQUEST.getCode());
		err.setDevError(exception.getMessage());
		err.setError("Your request can't be processed because it is not recognized.");

		logger.error("BP Dev Error (code {}) {}", err.getCode(), err.getDevError());

		BPHttpErrorMessage httpErrorMessage = new BPHttpErrorMessage(err);

		return Response.status(httpErrorMessage.getHttpStatusCode()).entity(httpErrorMessage).build();

	}

}
