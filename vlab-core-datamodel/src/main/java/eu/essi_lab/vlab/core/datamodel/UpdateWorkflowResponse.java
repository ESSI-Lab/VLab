package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class UpdateWorkflowResponse {

	@JsonInclude
	private String updatedWorkflowId;

	public String getUpdatedWorkflowId() {
		return updatedWorkflowId;
	}

	public void setUpdatedWorkflowId(String updatedWorkflowId) {
		this.updatedWorkflowId = updatedWorkflowId;
	}
}
