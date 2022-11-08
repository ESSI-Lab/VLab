package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class LogEvent {

	@JsonInclude
	private String message;

	@JsonInclude
	private Long timestamp;

	@JsonInclude
	private Long ingestionTime;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getIngestionTime() {
		return ingestionTime;
	}

	public void setIngestionTime(Long ingestionTime) {
		this.ingestionTime = ingestionTime;
	}
}
