package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class ContainerOrchestratorReservationResult {

	private boolean acquired = false;

	private String message;
	private String taskArn;

	public boolean isAcquired() {
		return acquired;
	}

	public void setAcquired(boolean acquired) {
		this.acquired = acquired;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTaskArn(String taskArn) {
		this.taskArn = taskArn;
	}

	public String getTaskArn() {
		return taskArn;
	}
}
