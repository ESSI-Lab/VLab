package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BPKubernetesComputeInfrastructure extends BPComputeInfrastructure {

	@JsonInclude
	private String serverUrl;

	@JsonInclude
	private String token;

	@JsonInclude
	private String controllerNodeSelector;

	@JsonInclude
	private String executorNodeSelector;

	@JsonInclude
	private String vlabPv;
	@JsonInclude
	private String vlabPvClaim;

	@JsonInclude
	private String controllerNodeTolerations;

	@JsonInclude
	private String executorNodeTolerations;

	public String getServerUrl() {
		return serverUrl;
	}

	public String getVlabPv() {
		return vlabPv;
	}

	public void setVlabPv(String vlabPv) {
		this.vlabPv = vlabPv;
	}

	public String getVlabPvClaim() {
		return vlabPvClaim;
	}

	public void setVlabPvClaim(String vlabPvClaim) {
		this.vlabPvClaim = vlabPvClaim;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getControllerNodeSelector() {
		return controllerNodeSelector;
	}

	public void setControllerNodeSelector(String controllerNodeSelector) {
		this.controllerNodeSelector = controllerNodeSelector;
	}

	public String getExecutorNodeSelector() {
		return executorNodeSelector;
	}

	public void setExecutorNodeSelector(String executorNodeSelector) {
		this.executorNodeSelector = executorNodeSelector;
	}

	public void setControllerNodeTolerations(String controllerNodeTolerations) {
		this.controllerNodeTolerations = controllerNodeTolerations;
	}

	public String getControllerNodeTolerations() {
		return controllerNodeTolerations;
	}

	public void setExecutorNodeTolerations(String executorNodeTolerations) {
		this.executorNodeTolerations = executorNodeTolerations;
	}

	public String getExecutorNodeTolerations() {
		return executorNodeTolerations;
	}
}
