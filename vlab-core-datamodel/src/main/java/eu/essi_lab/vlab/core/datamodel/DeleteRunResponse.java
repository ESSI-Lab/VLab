package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class DeleteRunResponse {

	@JsonInclude
	private String deletedRunId;

	public String getDeletedRunId() {
		return deletedRunId;
	}

	public void setDeletedRunId(String deletedRunId) {
		this.deletedRunId = deletedRunId;
	}
}
