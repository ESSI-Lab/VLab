package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class LogMessagesResponse {

	@JsonInclude
	private String nextBackwardToken;

	@JsonInclude
	private String nextForwardToken;

	@JsonInclude
	private List<LogEvent> events;

	public String getNextForwardToken() {
		return nextForwardToken;
	}

	public void setNextForwardToken(String nextForwardToken) {
		this.nextForwardToken = nextForwardToken;
	}

	public String getNextBackwardToken() {
		return nextBackwardToken;
	}

	public void setNextBackwardToken(String nextBackwardToken) {
		this.nextBackwardToken = nextBackwardToken;
	}

	public List<LogEvent> getEvents() {
		return events;
	}

	public void setEvents(List<LogEvent> events) {
		this.events = events;
	}
}
