package eu.essi_lab.vlab.web.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.essi_lab.vlab.core.datamodel.BPErrorMessage;

/**
 * @author Mattia Santoro
 */
public class BPHttpErrorMessage extends BPErrorMessage {

	@JsonInclude
	private int httpStatusCode;

	public BPHttpErrorMessage() {
		super();
	}

	public BPHttpErrorMessage(BPErrorMessage errorMessage) {

		super();

		setCode(errorMessage.getCode());
		setDevError(errorMessage.getDevError());
		setError(errorMessage.getError());

		setHttpStatusCode(new BPCodesMapper().toHttpStatusCode(errorMessage.getCode()));
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
}
