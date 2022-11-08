package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.TimeZone;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BPRunStatus extends Observable {

	@JsonInclude
	private String runid;

	@JsonInclude
	private String workflowid;

	@JsonInclude
	private String status;

	@JsonInclude
	private String message;

	@JsonInclude
	private List<String> messageList = new ArrayList<>();

	@JsonInclude
	private String result;

	@JsonInclude
	private String modelTaskId;

	@JsonInclude
	private String executionInfrastructureLabel;

	@JsonInclude
	private String executionInfrastructureId;

	public String getResult() {
		return result;
	}

	private void triggerUpdate() {

		setChanged();

		notifyObservers();
	}

	public void setResult(String result) {

		this.result = result;

		triggerUpdate();

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String msg) {

		if (!hasDate(msg)) {

			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

			String date = sdf.format(new Date());

			msg = "[" + date + "] " + msg;
		}

		this.message = msg;

		this.messageList.add(this.message);

		triggerUpdate();

	}

	boolean hasDate(String msg) {
		try {

			String sub = msg.substring(msg.indexOf('[') + 1, msg.indexOf(']'));

			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");

			sdf.parse(sub);

			return true;

		} catch (Exception throwable) {
			//nothing to do here
		}

		return false;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {

		this.status = status;

		triggerUpdate();

	}

	public BPRunStatus() {

	}

	public BPRunStatus(String runid) {
		this.runid = runid;
	}

	public String getRunid() {
		return runid;
	}

	public void setRunid(String runid) {

		this.runid = runid;
	}

	public List<String> getMessageList() {
		return messageList;
	}

	public void setMessageList(List<String> messageList) {
		this.messageList = messageList;
	}

	public void setModelTaskId(String modelTaskId) {
		this.modelTaskId = modelTaskId;

		triggerUpdate();
	}

	public String getModelTaskId() {
		return modelTaskId;
	}

	public String getWorkflowid() {
		return workflowid;
	}

	public void setWorkflowid(String workflowid) {
		this.workflowid = workflowid;
	}

	public String getExecutionInfrastructureId() {
		return executionInfrastructureId;
	}

	public void setExecutionInfrastructureId(String executionInfrastructureId) {
		this.executionInfrastructureId = executionInfrastructureId;
		triggerUpdate();
	}

	public String getExecutionInfrastructureLabel() {
		return executionInfrastructureLabel;
	}

	public void setExecutionInfrastructureLabel(String executionInfrastructureLabel) {
		this.executionInfrastructureLabel = executionInfrastructureLabel;
		triggerUpdate();
	}
}
