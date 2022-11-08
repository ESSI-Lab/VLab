package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BPOutput extends BPObject {

	@JsonInclude
	private String outputType;

	@JsonInclude
	private Boolean createFolder;

	public BPOutput() {
		//empty constructor for json serialization/deserialization
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public Boolean getCreateFolder() {
		//TODO write some tests for SourceCodeExecutor to test this
		return createFolder != null ? createFolder : true;
	}

	public void setCreateFolder(Boolean createFolder) {
		this.createFolder = createFolder;
	}
}
