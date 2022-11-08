package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class UpdateRunResponse {

	@JsonInclude
	private String updatedRunId;

	public String getUpdatedRunId() {
		return updatedRunId;
	}

	public void setUpdatedRunId(String updatedRunId) {
		this.updatedRunId = updatedRunId;
	}
}
