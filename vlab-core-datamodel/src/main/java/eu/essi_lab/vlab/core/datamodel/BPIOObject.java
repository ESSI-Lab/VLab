package eu.essi_lab.vlab.core.datamodel;

import java.util.List;

/**
 * @author Mattia Santoro
 */
public class BPIOObject {

	private List<BPInputDescription> inputs;

	private List<BPOutputDescription> outputs;

	public List<BPInputDescription> getInputs() {
		return inputs;
	}

	public void setInputs(List<BPInputDescription> inputs) {
		this.inputs = inputs;
	}

	public List<BPOutputDescription> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<BPOutputDescription> outputs) {
		this.outputs = outputs;
	}
}
