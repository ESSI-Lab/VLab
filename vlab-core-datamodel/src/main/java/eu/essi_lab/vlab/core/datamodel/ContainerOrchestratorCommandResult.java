package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class ContainerOrchestratorCommandResult {

	private boolean success;

	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
