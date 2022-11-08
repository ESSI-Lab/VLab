package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BPObject {

	@JsonInclude
	private String valueType;

	@JsonInclude
	private String description;

	@JsonInclude
	private String name;

	@JsonInclude
	private String id;

	@JsonInclude
	private Object value;

	@JsonInclude
	private List<Object> valueArray;

	@JsonInclude
	private String valueSchema;// url, bbox (west,south,east,north,?xres,?yres), number_parameter, string_parameter

	public BPObject() {
		//empty constructor for json serialization/deserialization
	}
	public String getValueSchema() {
		return valueSchema;
	}

	public void setValueSchema(String valueSchema) {
		this.valueSchema = valueSchema;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public List<Object> getValueArray() {
		return valueArray;
	}

	public void setValueArray(List<Object> valueArray) {
		this.valueArray = valueArray;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
