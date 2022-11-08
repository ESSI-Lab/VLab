package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Mattia Santoro
 */
public class BPRun {

	@JsonInclude
	private List<BPInput> inputs;

	@JsonInclude
	private String runid;

	@JsonInclude
	private String description;

	@JsonInclude
	private String workflowid;

	@JsonInclude
	private String owner;

	@JsonInclude
	private boolean publicRun;

	@JsonInclude
	private boolean ownedbyrequester;

	@JsonInclude
	private boolean sharedwithrequester;

	@JsonInclude
	private String name;

	@JsonInclude
	private List<String> sharedWith = new ArrayList<>();

	@JsonIgnore
	private static final String ES_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	@JsonInclude
	private Long creationTime;

	@JsonInclude
	private String esCreationTime;

	@JsonInclude
	private String executionInfrastructure;

	public BPRun() {

		// set the English locale
		Locale.setDefault(Locale.ENGLISH);
		// set time zone
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public String getEsCreationTime() {
		return esCreationTime;
	}

	public void setEsCreationTime(String esCreationTime) {
		this.esCreationTime = esCreationTime;
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;

		if (creationTime != null)

			setEsCreationTime(getESTimeStamp(this.creationTime));
	}

	@JsonIgnore
	public static String getESTimeStamp(Long timestamp) {

		SimpleDateFormat sdf = new SimpleDateFormat(ES_FORMAT);

		return sdf.format(new Date(timestamp));

	}

	public List<BPInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<BPInput> inputs) {
		this.inputs = inputs;
	}

	public String getRunid() {
		return runid;
	}

	public void setRunid(String runid) {
		this.runid = runid;
	}

	public String getWorkflowid() {
		return workflowid;
	}

	public void setWorkflowid(String workflowid) {
		this.workflowid = workflowid;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isPublicRun() {
		return publicRun;
	}

	public void setPublicRun(boolean publicRun) {
		this.publicRun = publicRun;
	}

	@JsonIgnore
	public void shareWithUser(String useremail) throws BPException {

		doAddSharedUser(sharedWith, useremail);
	}

	@JsonIgnore
	public void deleteShareWithUser(String useremail) {

		if (sharedWith != null) {

			sharedWith.remove(useremail);

		}

	}

	private void doAddSharedUser(List<String> list, String toadd) throws BPException {

		if (list.size() > 100) {
			BPException ex = new BPException("Too many users", BPException.ERROR_CODES.SHARED_WITH_TOO_MANY);

			ex.setUserMessage("Maximum number of users with whom to share a run is 100, please consider make your run public");

			throw ex;
		}

		list.add(toadd);

	}

	public List<String> getSharedWith() {
		return sharedWith;
	}

	public void setSharedWith(List<String> sharedWith) {
		this.sharedWith = sharedWith;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExecutionInfrastructure() {
		return executionInfrastructure;
	}

	public void setExecutionInfrastructure(String executionInfrastructure) {
		this.executionInfrastructure = executionInfrastructure;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
