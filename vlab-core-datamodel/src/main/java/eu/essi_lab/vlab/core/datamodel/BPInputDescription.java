package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.essi_lab.vlab.core.serialization.json.RetrocompatibleBPInputFieldDeserializer;
import java.util.List;

/**
 * This is an extension of {@link BPInput} providing additional information which are realization-specific. This is what the executors (e
 * .g. SourceCodeExecutor) expect to find when reading default input values from convention parsers (e.g. SourceCodeConventionFileLoader).
 *
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = RetrocompatibleBPInputFieldDeserializer.class)
public class BPInputDescription {

	@JsonInclude
	@JsonUnwrapped
	private BPInput input = new BPInput();

	@JsonInclude
	private String target;

	@JsonInclude
	private String defaultValue;

	@JsonInclude
	private List<Object> defaultValueArray;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<Object> getDefaultValueArray() {
		return defaultValueArray;
	}

	public void setDefaultValueArray(List<Object> defaultValueArray) {
		this.defaultValueArray = defaultValueArray;
	}

	public BPInput getInput() {
		return input;
	}

	public void setInput(BPInput input) {
		this.input = input;
	}
}
