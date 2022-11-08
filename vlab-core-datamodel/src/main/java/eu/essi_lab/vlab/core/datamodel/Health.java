package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class Health {

	@JsonInclude
	private Boolean healthy;

	public Boolean getHealthy() {
		return healthy;
	}

	public void setHealthy(Boolean healthy) {
		this.healthy = healthy;
	}
}
