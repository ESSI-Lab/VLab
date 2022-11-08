package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class SearchWorkflowsResponse {

	@JsonInclude
	private List<APIWorkflowDetail> workflows;

	@JsonInclude
	private Integer total;

	public List<APIWorkflowDetail> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<APIWorkflowDetail> workflows) {
		this.workflows = workflows;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
}
