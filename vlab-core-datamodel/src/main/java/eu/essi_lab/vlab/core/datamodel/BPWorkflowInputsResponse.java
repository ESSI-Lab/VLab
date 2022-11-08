package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class BPWorkflowInputsResponse {

	@JsonInclude
	private List<BPInput> inputs;

	public List<BPInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<BPInput> inputs) {
		this.inputs = inputs;
	}
}
