package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class BPRealizationObject {

	@JsonInclude
	private BPRealization realization;

	public BPRealization getRealization() {
		return realization;
	}

	public void setRealization(BPRealization realization) {
		this.realization = realization;
	}
}
