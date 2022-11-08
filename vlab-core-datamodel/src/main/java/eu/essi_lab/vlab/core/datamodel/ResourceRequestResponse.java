package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class ResourceRequestResponse {

	private String groupName;
	private String policy;
	private boolean requested;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getUsableScalingPolicy() {
		return policy;
	}

	public void setUsableScalingPolicy(String policy) {
		this.policy = policy;
	}

	public boolean requestSent() {
		return requested;
	}

	public void setRequestSent(boolean requested) {
		this.requested = requested;
	}
}
