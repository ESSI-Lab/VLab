package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class ContainerOrchestratorSubmitResult {

	private boolean success;

	private String message;

	/**
	 * The identifier of the submitted job.
	 */
	private String identifier;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean submissionSuccess() {
		return success;
	}

	public void setSubmissionSuccess(boolean success) {
		this.success = success;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
