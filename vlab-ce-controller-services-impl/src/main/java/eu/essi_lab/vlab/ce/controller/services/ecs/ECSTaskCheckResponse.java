package eu.essi_lab.vlab.ce.controller.services.ecs;

/**
 * @author Mattia Santoro
 */
public class ECSTaskCheckResponse {

	private boolean success;
	private ECSTASK_STATUS lastStatus;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public ECSTASK_STATUS getLastStatus() {
		return lastStatus;
	}

	public void setLastStatus(ECSTASK_STATUS lastStatus) {
		this.lastStatus = lastStatus;
	}
}
