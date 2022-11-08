package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class CreateWorkflowResponse {

	@JsonInclude
	private String message;

	@JsonInclude
	private CreateWorkflowResult result;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public CreateWorkflowResult getResult() {
		return result;
	}

	public void setResult(CreateWorkflowResult result) {
		this.result = result;
	}
}
