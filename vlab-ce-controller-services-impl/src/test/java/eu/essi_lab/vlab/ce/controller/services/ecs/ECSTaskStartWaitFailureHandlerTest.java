package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.StopTaskRequest;
import software.amazon.awssdk.services.ecs.model.StopTaskResponse;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * @author Mattia Santoro
 */
public class ECSTaskStartWaitFailureHandlerTest {

	@Test
	public void keepWaitTestPending() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);
		Mockito.doReturn(tasks).when(taskResult).tasks();

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.PENDING).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(manager).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(taskResult, manager, 0, "cluster", 10000L, response);

		Mockito.verify(manager, Mockito.times(1)).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertTrue(r.submissionSuccess());

	}

	@Test
	public void keepWaitTestTimedout() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);
		Mockito.doReturn(tasks).when(taskResult).tasks();

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.TIMEDOUT).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(manager).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(taskResult, manager, 0, "cluster", 10000L, response);

		Mockito.verify(manager, Mockito.times(1)).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertTrue(r.submissionSuccess());

	}

	@Test
	public void tooManyRetriesTimedout() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);
		Mockito.doReturn(tasks).when(taskResult).tasks();

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);
		handler.setMaxRetries(1);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.TIMEDOUT).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(manager).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		StopTaskResponse stopResponse = Mockito.mock(StopTaskResponse.class);

		Task stopTask = Mockito.mock(Task.class);

		Mockito.doReturn(stopTask).when(stopResponse).task();

		String lastStatus = "lastStatus";

		Mockito.doReturn(lastStatus).when(task).lastStatus();

		Mockito.doReturn(stopResponse).when(client).stopTask((StopTaskRequest) Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(taskResult, manager, 1, "cluster", 10000L, response);

		Mockito.verify(manager, Mockito.times(0)).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(1)).stopTask((StopTaskRequest) Mockito.any());

		Assert.assertFalse(r.submissionSuccess());

	}

	@Test
	public void keepWaitTestNoStatus() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);
		Mockito.doReturn(tasks).when(taskResult).tasks();

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.NOSTATUS).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(manager).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		StopTaskResponse stopResponse = Mockito.mock(StopTaskResponse.class);

		Task stopTask = Mockito.mock(Task.class);

		Mockito.doReturn(stopTask).when(stopResponse).task();

		String lastStatus = "lastStatus";

		Mockito.doReturn(lastStatus).when(task).lastStatus();

		Mockito.doReturn(stopResponse).when(client).stopTask((StopTaskRequest) Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(taskResult, manager, 0, "cluster", 10000L, response);

		Mockito.verify(manager, Mockito.times(0)).waitStart(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(1)).stopTask((StopTaskRequest) Mockito.any());

		Assert.assertFalse(r.submissionSuccess());

	}

}