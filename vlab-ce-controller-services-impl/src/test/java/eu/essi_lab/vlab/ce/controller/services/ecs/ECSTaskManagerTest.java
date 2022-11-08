package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * @author Mattia Santoro
 */
public class ECSTaskManagerTest {

	@Test
	public void testSuccess() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);
		ECSTaskManager manager = new ECSTaskManager(client, request);

		ECSTaskStatusChecker checker = Mockito.mock(ECSTaskStatusChecker.class);
		manager.setChecker(checker);
		ECSTaskFailureHandler handler = Mockito.mock(ECSTaskFailureHandler.class);
		manager.setFailureHandler(handler);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(true).when(response).isSuccess();

		Mockito.when(checker.taskCompletedWithSuccess(Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(response);

		ContainerOrchestratorCommandResult result = manager.getTaskResult(taskResult, 10000L, "cluster");

		assertNotNull(result);

		assertTrue(result.isSuccess());

	}

	@Test
	public void testStartedSuccess() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task t = Mockito.mock(Task.class);

		tasks.add(t);

		String arn = "task:arn";

		Mockito.doReturn(arn).when(t).taskArn();

		Mockito.doReturn(tasks).when(taskResult).tasks();

		ECSTaskManager manager = new ECSTaskManager(client, request);

		ECSTaskStatusChecker checker = Mockito.mock(ECSTaskStatusChecker.class);
		manager.setChecker(checker);
		ECSTaskFailureHandler handler = Mockito.mock(ECSTaskFailureHandler.class);
		manager.setFailureHandler(handler);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(true).when(response).isSuccess();

		Mockito.when(checker.taskStartedWithSuccess(Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(response);

		ContainerOrchestratorSubmitResult result = manager.waitStart(taskResult, 10000L, "cluster", 0);

		assertNotNull(result);

		assertTrue(result.submissionSuccess());

		Assert.assertEquals(arn, result.getIdentifier());

	}

	@Test
	public void testStartedFail() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task t = Mockito.mock(Task.class);

		tasks.add(t);

		String arn = "task:arn";

		Mockito.doReturn(arn).when(t).taskArn();

		Mockito.doReturn(tasks).when(taskResult).tasks();

		ECSTaskManager manager = new ECSTaskManager(client, request);

		ECSTaskStatusChecker checker = Mockito.mock(ECSTaskStatusChecker.class);
		manager.setChecker(checker);
		ECSTaskFailureHandler handler = Mockito.mock(ECSTaskFailureHandler.class);
		manager.setFailureHandler(handler);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(false).when(response).isSuccess();

		Mockito.when(checker.taskStartedWithSuccess(Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(response);

		ContainerOrchestratorSubmitResult hr = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(false).when(hr).submissionSuccess();
		Mockito.doReturn(arn).when(hr).getIdentifier();

		Mockito.doReturn(hr).when(handler).handleWaitStartFailure(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
		ContainerOrchestratorSubmitResult result = manager.waitStart(taskResult, 10000L, "cluster", 0);

		assertNotNull(result);

		assertFalse(result.submissionSuccess());

		Assert.assertEquals(arn, result.getIdentifier());

		Mockito.verify(handler, Mockito.times(1)).handleWaitStartFailure(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
	}

	@Test
	public void testReservationFailure() throws BPException {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> list = new ArrayList<>();

		Task t = Mockito.mock(Task.class);

		String taskArn = "task:Arn";
		Mockito.when(t.taskArn()).thenReturn(taskArn);

		list.add(t);

		Mockito.when(taskResult.tasks()).thenReturn(list);

		ECSTaskManager manager = new ECSTaskManager(client, request);

		ECSTaskStatusChecker checker = Mockito.mock(ECSTaskStatusChecker.class);
		manager.setChecker(checker);
		ECSTaskFailureHandler handler = Mockito.mock(ECSTaskFailureHandler.class);
		manager.setFailureHandler(handler);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(false).when(response).isSuccess();

		Mockito.when(checker.taskCompletedWithSuccess(Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(response);

		ContainerOrchestratorReservationResult result = manager.getReservationResult(taskResult, "cluster");

		assertNotNull(result);

		assertFalse(result.isAcquired());

	}

	@Test
	public void testReservationSuccess() throws BPException {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> list = new ArrayList<>();

		Task t = Mockito.mock(Task.class);

		String taskArn = "task:Arn";
		Mockito.when(t.taskArn()).thenReturn(taskArn);

		list.add(t);

		Mockito.when(taskResult.tasks()).thenReturn(list);

		ECSTaskManager manager = new ECSTaskManager(client, request);

		ECSTaskStatusChecker checker = Mockito.mock(ECSTaskStatusChecker.class);
		manager.setChecker(checker);
		ECSTaskFailureHandler handler = Mockito.mock(ECSTaskFailureHandler.class);
		manager.setFailureHandler(handler);

		Mockito.when(checker.reservationTaskRunning(Mockito.any(), Mockito.any())).thenReturn(true);

		ContainerOrchestratorReservationResult result = manager.getReservationResult(taskResult, "cluster");

		assertNotNull(result);

		assertTrue(result.isAcquired());

		Assert.assertEquals(taskArn, result.getTaskArn());

	}

	@Test
	public void testFailure() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);
		ECSTaskManager manager = new ECSTaskManager(client, request);

		ECSTaskStatusChecker checker = Mockito.mock(ECSTaskStatusChecker.class);
		manager.setChecker(checker);

		ECSTaskFailureHandler handler = Mockito.mock(ECSTaskFailureHandler.class);
		manager.setFailureHandler(handler);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(false).when(response).isSuccess();

		Mockito.when(checker.taskCompletedWithSuccess(Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(response);

		ContainerOrchestratorCommandResult result = manager.getTaskResult(taskResult, 10000L, "cluster");

		Mockito.verify(handler, Mockito.times(1)).handleFailure(Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any(),
				Mockito.anyLong(), Mockito.any());

	}

}