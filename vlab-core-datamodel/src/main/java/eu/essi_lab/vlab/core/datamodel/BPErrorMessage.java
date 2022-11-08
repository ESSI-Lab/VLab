package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class BPErrorMessage {

	@JsonInclude
	private String error;

	@JsonInclude
	private String devError;

	@JsonInclude
	private Integer code;

	public BPErrorMessage() {
	}

	public BPErrorMessage(String userError) {
		this.error = userError;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getDevError() {
		return devError;
	}

	public void setDevError(String devError) {
		this.devError = devError;
	}
}
