package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class ValidateRealizationResponse extends BasicValidationResponse {

	@JsonInclude
	private APIWorkflowDetail workflow;

	public APIWorkflowDetail getWorkflow() {
		return workflow;
	}

	public void setWorkflow(APIWorkflowDetail workflow) {
		this.workflow = workflow;
	}
}
