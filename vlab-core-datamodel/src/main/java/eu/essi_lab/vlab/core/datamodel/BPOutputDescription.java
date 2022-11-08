package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.essi_lab.vlab.core.serialization.json.RetrocompatibleBPOutputFieldDeserializer;

/**
 * This is a description of {@link BPOutput} providing additional information which are realization-specific. This is what the executors (e
 * .g. SourceCodeExecutor) expect to find when reading output objects from convention parsers (e.g. SourceCodeConventionFileLoader).
 *
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = RetrocompatibleBPOutputFieldDeserializer.class)
public class BPOutputDescription {

	@JsonInclude
	@JsonUnwrapped
	private BPOutput output = new BPOutput();

	@JsonInclude
	private String target;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public BPOutput getOutput() {
		return output;
	}

	public void setOutput(BPOutput output) {
		this.output = output;
	}
}
