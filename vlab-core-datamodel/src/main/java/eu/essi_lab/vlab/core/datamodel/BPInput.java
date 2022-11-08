package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BPInput extends BPObject {

	@JsonInclude
	private Boolean obligation;

	@JsonInclude
	private Boolean hasDefault;

	@JsonInclude
	private String inputType;

	@JsonInclude
	private String valueArrayInterpretation;

	@JsonInclude
	private ValueKey valueKey;

	@JsonInclude
	private List<ValueKey> valueKeyArray;

	public BPInput() {
		//empty constructor for json serialization/deserialization
	}

	public Boolean getObligation() {
		return obligation;
	}

	public void setObligation(Boolean obligation) {
		this.obligation = obligation;
	}

	public Boolean getHasDefault() {
		return hasDefault;
	}

	public void setHasDefault(Boolean hasDefault) {
		this.hasDefault = hasDefault;
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public String getValueArrayInterpretation() {
		return valueArrayInterpretation;
	}

	public void setValueArrayInterpretation(String valueArrayInterpretation) {
		this.valueArrayInterpretation = valueArrayInterpretation;
	}

	public ValueKey getValueKey() {
		return valueKey;
	}

	public void setValueKey(ValueKey valueKey) {
		this.valueKey = valueKey;
	}

	public List<ValueKey> getValueKeyArray() {
		return valueKeyArray;
	}

	public void setValueKeyArray(List<ValueKey> valueKeyArray) {
		this.valueKeyArray = valueKeyArray;
	}
}
