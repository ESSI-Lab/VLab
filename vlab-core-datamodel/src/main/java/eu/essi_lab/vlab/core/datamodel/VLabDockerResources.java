package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class VLabDockerResources {

	@JsonInclude
	private String memory_mb;

	@JsonInclude
	private String cpu_units;

	public String getMemory_mb() {
		return memory_mb;
	}

	public void setMemory_mb(String memory_mb) {
		this.memory_mb = memory_mb;
	}

	public String getCpu_units() {
		return cpu_units;
	}

	public void setCpu_units(String cpu_units) {
		this.cpu_units = cpu_units;
	}
}
