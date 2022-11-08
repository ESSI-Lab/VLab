package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class APIWorkflowDetail {

	@JsonInclude
	private String description;

	@JsonInclude
	private String name;

	@JsonInclude
	private String id;

	@JsonInclude
	private String bpmn_url;

	@JsonInclude
	private boolean under_test;

	@JsonInclude
	private String modelDeveloper;

	@JsonInclude
	private String modelDeveloperEmail;

	@JsonInclude
	private String modelDeveloperOrg;

	@JsonInclude
	private List<String> sharedWith = new ArrayList<>();

	@JsonInclude
	private boolean ownedbyrequester;

	@JsonInclude
	private boolean sharedwithrequester;

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

	public String getBpmn_url() {
		return bpmn_url;
	}

	public void setBpmn_url(String bpmnurl) {
		this.bpmn_url = bpmnurl;
	}

	public boolean isUnder_test() {
		return under_test;
	}

	public void setUnder_test(boolean undertest) {
		this.under_test = undertest;
	}

	public String getModelDeveloperOrg() {
		return modelDeveloperOrg;
	}

	public void setModelDeveloperOrg(String modelDeveloperOrg) {
		this.modelDeveloperOrg = modelDeveloperOrg;
	}

	public String getModelDeveloperEmail() {
		return modelDeveloperEmail;
	}

	public void setModelDeveloperEmail(String modelDeveloperEmail) {
		this.modelDeveloperEmail = modelDeveloperEmail;
	}

	public String getModelDeveloper() {
		return modelDeveloper;
	}

	public void setModelDeveloper(String modelDeveloper) {
		this.modelDeveloper = modelDeveloper;
	}

	public List<String> getSharedWith() {
		return sharedWith;
	}

	public void setSharedWith(List<String> sharedWith) {
		this.sharedWith = sharedWith;
	}

	@JsonIgnore
	public void shareWithUser(String useremail) throws BPException {

		doAddSharedUser(sharedWith, useremail);
	}

	private void doAddSharedUser(List<String> list, String toadd) throws BPException {

		if (list.size() > 100) {
			BPException ex = new BPException("Too many users", BPException.ERROR_CODES.SHARED_WITH_TOO_MANY);

			ex.setUserMessage("Maximum number of users with whom to share a run is 100, please consider make your run public");

			throw ex;
		}

		list.add(toadd);

	}

	public boolean isOwnedbyrequester() {
		return ownedbyrequester;
	}

	public void setOwnedbyrequester(boolean ownedbyrequester) {
		this.ownedbyrequester = ownedbyrequester;
	}

	public boolean isSharedwithrequester() {
		return sharedwithrequester;
	}

	public void setSharedwithrequester(boolean sharedwithrequester) {
		this.sharedwithrequester = sharedwithrequester;
	}
}
