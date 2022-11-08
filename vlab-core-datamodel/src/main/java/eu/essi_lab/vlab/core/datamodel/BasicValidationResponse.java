package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class BasicValidationResponse {
	@JsonInclude
	private Boolean valid;

	@JsonInclude
	private List<ValidationMessage> messages;

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public List<ValidationMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ValidationMessage> messages) {
		this.messages = messages;
	}

}
