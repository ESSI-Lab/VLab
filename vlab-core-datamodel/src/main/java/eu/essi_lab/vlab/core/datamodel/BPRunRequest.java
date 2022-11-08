package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class BPRunRequest {

	@JsonInclude
	private List<BPInput> inputs;

	@JsonInclude
	private String name;

	@JsonInclude
	private String description;

	@JsonInclude
	private String infra;

	public List<BPInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<BPInput> inputs) {
		this.inputs = inputs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfra() {
		return infra;
	}

	public void setInfra(String infra) {
		this.infra = infra;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
