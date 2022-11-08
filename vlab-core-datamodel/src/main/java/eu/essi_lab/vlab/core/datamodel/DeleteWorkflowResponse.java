package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class DeleteWorkflowResponse {

	@JsonInclude
	private String deletedWorkflowId;

	public String getDeletedWorkflowId() {
		return deletedWorkflowId;
	}

	public void setDeletedWorkflowId(String deletedWorkflowId) {
		this.deletedWorkflowId = deletedWorkflowId;
	}
}
