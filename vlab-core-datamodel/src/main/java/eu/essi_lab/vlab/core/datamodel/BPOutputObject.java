package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class BPOutputObject {

	@JsonInclude
	private BPOutput outputObject;

	public BPOutput getOutputObject() {
		return outputObject;
	}

	public void setOutputObject(BPOutput outputObject) {
		this.outputObject = outputObject;
	}
}
