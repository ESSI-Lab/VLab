package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class ValidationMessage {

    @JsonInclude
    private ValidationMessageType type;

    @JsonInclude
    private String message;

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public ValidationMessageType getType() {
	return type;
    }

    public void setType(ValidationMessageType type) {
	this.type = type;
    }
}
