package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class BPWorkflowOutputsResponse {

	@JsonInclude
	private List<BPOutput> outputs;

	public List<BPOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<BPOutput> outputs) {
		this.outputs = outputs;
	}
}
