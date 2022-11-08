package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class BPRealization {

    @JsonInclude
    private String realizationURI;

    public String getRealizationURI() {
	return realizationURI;
    }

    public void setRealizationURI(String realizationURI) {
	this.realizationURI = realizationURI;
    }
}
