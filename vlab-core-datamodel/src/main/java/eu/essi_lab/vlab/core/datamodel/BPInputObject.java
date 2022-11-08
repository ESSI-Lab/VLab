package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class BPInputObject {

    @JsonInclude
    private BPInput inputObject;

    public BPInput getInputObject() {
	return inputObject;
    }

    public void setInputObject(BPInput inputObject) {
	this.inputObject = inputObject;
    }
}
