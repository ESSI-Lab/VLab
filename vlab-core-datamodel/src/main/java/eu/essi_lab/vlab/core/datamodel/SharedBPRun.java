package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class SharedBPRun {

	@JsonInclude
	private String owner;

	@JsonInclude
	private String runid;

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getRunid() {
		return runid;
	}

	public void setRunid(String runid) {
		this.runid = runid;
	}
}
