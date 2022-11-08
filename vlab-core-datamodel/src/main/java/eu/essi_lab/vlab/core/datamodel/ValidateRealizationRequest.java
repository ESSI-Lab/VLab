package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
public class ValidateRealizationRequest {

	@JsonInclude
	private String modelDeveloper;

	@JsonInclude
	private String modelDeveloperEmail;

	@JsonInclude
	private String modelDeveloperOrg;

	@JsonInclude
	private BPRealization realization;

	@JsonInclude
	private String modelName;

	@JsonInclude
	private String modelDescription;

	public BPRealization getRealization() {
		return realization;
	}

	public void setRealization(BPRealization realization) {
		this.realization = realization;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelDescription() {
		return modelDescription;
	}

	public void setModelDescription(String modelDescription) {
		this.modelDescription = modelDescription;
	}

	public String getModelDeveloper() {
		return modelDeveloper;
	}

	public void setModelDeveloper(String modelDeveloper) {
		this.modelDeveloper = modelDeveloper;
	}

	public String getModelDeveloperEmail() {
		return modelDeveloperEmail;
	}

	public void setModelDeveloperEmail(String modelDeveloperEmail) {
		this.modelDeveloperEmail = modelDeveloperEmail;
	}

	public String getModelDeveloperOrg() {
		return modelDeveloperOrg;
	}

	public void setModelDeveloperOrg(String modelDeveloperOrg) {
		this.modelDeveloperOrg = modelDeveloperOrg;
	}
}
