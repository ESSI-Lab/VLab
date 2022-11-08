package eu.essi_lab.vlab.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import java.util.Collections;
import java.util.List;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BPECSComputeInfrastructure extends BPComputeInfrastructure {

	@JsonInclude
	private String coreCluster;

	@JsonInclude
	private String asgSecretKey;

	@JsonInclude
	private String asgAccessKey;

	@JsonInclude
	private String deployECSSecretKey;

	@JsonInclude
	private String deployECSAccessKey;

	@JsonInclude
	private String executeECSSecretKey;

	@JsonInclude
	private String executeECSAccessKey;

	@JsonInclude
	private String modelCluster;

	@JsonInclude
	private String deployECSRegion;

	@JsonInclude
	private String executeECSRegion;

	@JsonInclude
	private List<String> modelExecutionAutoScalingGroups;

	@JsonInclude
	private String modelExecutionLogPrefix;

	@JsonInclude
	private String modelTaskContainerName;

	@JsonInclude
	private String efsFolder;
	private String deployLogGroup;
	private String modelExecutionLogGroup;
	private String deployLogPrefix;

	public void setModelExecutionAutoScalingGroups(List<String> modelExecutionAutoScalingGroups) {

		Collections.sort(modelExecutionAutoScalingGroups);

		this.modelExecutionAutoScalingGroups = modelExecutionAutoScalingGroups;
	}

	public List<String> getModelExecutionAutoScalingGroups() {
		return modelExecutionAutoScalingGroups;
	}

	public String getExecuteECSRegion() {
		return executeECSRegion;
	}

	public void setExecuteECSRegion(String executeECSRegion) {
		this.executeECSRegion = executeECSRegion;
	}

	public String getDeployECSRegion() {
		return deployECSRegion;
	}

	public void setDeployECSRegion(String deployECSRegion) {
		this.deployECSRegion = deployECSRegion;
	}

	public String getExecuteECSAccessKey() {
		return executeECSAccessKey;
	}

	public void setExecuteECSAccessKey(String executeECSAccessKey) {
		this.executeECSAccessKey = executeECSAccessKey;
	}

	public String getExecuteECSSecretKey() {
		return executeECSSecretKey;
	}

	public void setExecuteECSSecretKey(String executeECSSecretKey) {
		this.executeECSSecretKey = executeECSSecretKey;
	}

	public String getDeployECSAccessKey() {
		return deployECSAccessKey;
	}

	public void setDeployECSAccessKey(String deployECSAccessKey) {
		this.deployECSAccessKey = deployECSAccessKey;
	}

	public String getDeployECSSecretKey() {
		return deployECSSecretKey;
	}

	public void setDeployECSSecretKey(String deployECSSecretKey) {
		this.deployECSSecretKey = deployECSSecretKey;
	}

	public String getCoreCluster() {
		return coreCluster;
	}

	public void setCoreCluster(String coreCluster) {
		this.coreCluster = coreCluster;
	}

	public String getModelCluster() {
		return modelCluster;
	}

	public void setModelCluster(String modelCluster) {
		this.modelCluster = modelCluster;
	}

	public String getAsgAccessKey() {
		return asgAccessKey;
	}

	public void setAsgAccessKey(String asgAccessKey) {
		this.asgAccessKey = asgAccessKey;
	}

	public String getAsgSecretKey() {
		return asgSecretKey;
	}

	public void setAsgSecretKey(String asgSecretKey) {
		this.asgSecretKey = asgSecretKey;
	}

	public void setModelExecutionLogPrefix(String modelExecutionLogPrefix) {
		this.modelExecutionLogPrefix = modelExecutionLogPrefix;
	}

	public String getModelExecutionLogPrefix() {
		return modelExecutionLogPrefix;
	}

	public void setModelTaskContainerName(String modelTaskContainerName) {
		this.modelTaskContainerName = modelTaskContainerName;
	}

	public String getModelTaskContainerName() {
		return modelTaskContainerName;
	}

	public void setEfsFolder(String efsFolder) {
		this.efsFolder = efsFolder;
	}

	public String getEfsFolder() {
		return efsFolder;
	}

	public String getDeployLogGroup() {
		return deployLogGroup;
	}

	public void setDeployLogGroup(String deployLogGroup) {
		this.deployLogGroup = deployLogGroup;
	}

	public String getModelExecutionLogGroup() {
		return modelExecutionLogGroup;
	}

	public void setModelExecutionLogGroup(String modelExecutionLogGroup) {
		this.modelExecutionLogGroup = modelExecutionLogGroup;
	}

	public String getDeployLogPrefix() {
		return deployLogPrefix;
	}

	public void setDeployLogPrefix(String deployLogPrefix) {
		this.deployLogPrefix = deployLogPrefix;
	}
}
