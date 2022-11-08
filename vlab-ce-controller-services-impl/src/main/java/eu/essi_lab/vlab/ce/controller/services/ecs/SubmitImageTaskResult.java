package eu.essi_lab.vlab.ce.controller.services.ecs;

import software.amazon.awssdk.services.ecs.model.RunTaskResponse;

/**
 * @author Mattia Santoro
 */
public class SubmitImageTaskResult {
	private final RunTaskResponse runTaskResult;
	private final ECSTaskManager escTaskManager;

	public SubmitImageTaskResult(ECSTaskManager m, RunTaskResponse rtr) {
		this.escTaskManager = m;
		this.runTaskResult = rtr;
	}

	public ECSTaskManager getEscTaskManager() {
		return escTaskManager;
	}

	public RunTaskResponse getRunTaskResult() {
		return runTaskResult;
	}
}
