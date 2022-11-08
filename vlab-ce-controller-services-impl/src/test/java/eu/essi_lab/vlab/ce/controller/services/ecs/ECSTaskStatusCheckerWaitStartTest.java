package eu.essi_lab.vlab.ce.controller.services.ecs;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Container;
import software.amazon.awssdk.services.ecs.model.DescribeTasksRequest;
import software.amazon.awssdk.services.ecs.model.DescribeTasksResponse;
import software.amazon.awssdk.services.ecs.model.Failure;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * @author Mattia Santoro
 */
public class ECSTaskStatusCheckerWaitStartTest {

	@Test
	public void testStartedWithSuccessOkPendingRunningStopped() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("PENDING", "RUNNING", "STOPPED").when(task).lastStatus();

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(0);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = Mockito.spy(new ECSTaskStatusChecker(client));

		ECSTaskCheckResponse success = checker.taskStartedWithSuccess(taskResult, 10000L, "cluster");

		Mockito.verify(checker, Mockito.times(2)).status(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertTrue(success.isSuccess());

		Assert.assertEquals(ECSTASK_STATUS.RUNNING, success.getLastStatus());

	}

	@Test
	public void testStartedWithSuccessOkPendingRunningStoppedWithContainerFailure() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("PENDING", "STOPPED").when(task).lastStatus();

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(-1);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = Mockito.spy(new ECSTaskStatusChecker(client));

		ECSTaskCheckResponse success = checker.taskStartedWithSuccess(taskResult, 10000L, "cluster");

		Mockito.verify(checker, Mockito.times(2)).status(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertFalse(success.isSuccess());

		Mockito.verify(container, Mockito.times(1)).exitCode();

		Assert.assertEquals(ECSTASK_STATUS.STOPPED, success.getLastStatus());

	}

	@Test
	public void testStartedWithSuccessTimeoutPending() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING",
				"PENDING").when(task).lastStatus();

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(0);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = Mockito.spy(new ECSTaskStatusChecker(client));

		ECSTaskCheckResponse success = checker.taskStartedWithSuccess(taskResult, 2000L, "cluster");

		Mockito.verify(checker, Mockito.times(2)).status(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertFalse(success.isSuccess());

		Assert.assertEquals(ECSTASK_STATUS.TIMEDOUT, success.getLastStatus());

	}

	@Test
	public void testStartedWithSuccessPendingStopped() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("PENDING", "STOPPED").when(task).lastStatus();

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(0);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = Mockito.spy(new ECSTaskStatusChecker(client));

		ECSTaskCheckResponse success = checker.taskStartedWithSuccess(taskResult, 5000L, "cluster");

		Mockito.verify(checker, Mockito.times(2)).status(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertEquals(ECSTASK_STATUS.STOPPED, success.getLastStatus());

		Assert.assertTrue(success.isSuccess());
	}

	@Test
	public void testStartedFailureWithNoTask() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(0);

		containers.add(container);

		Mockito.when(taskResult.tasks()).thenReturn(null);

		EcsClient client = Mockito.mock(EcsClient.class);

		ECSTaskStatusChecker checker = Mockito.spy(new ECSTaskStatusChecker(client));

		ECSTaskCheckResponse success = checker.taskStartedWithSuccess(taskResult, 5000L, "cluster");

		Mockito.verify(checker, Mockito.times(0)).status(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertFalse(success.isSuccess());

		Assert.assertEquals(ECSTASK_STATUS.NOSTATUS, success.getLastStatus());
	}

	@Test
	public void testStartedWithStoppedFailure() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("STOPPED").when(task).lastStatus();

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(-1);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		List<Failure> failures = new ArrayList<>();

		Failure failure = Mockito.mock(Failure.class);

		failures.add(failure);

		Mockito.when(taskResult.failures()).thenReturn(failures);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = Mockito.spy(new ECSTaskStatusChecker(client));

		ECSTaskCheckResponse success = checker.taskStartedWithSuccess(taskResult, 5000L, "cluster");

		Mockito.verify(checker, Mockito.times(0)).status(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertFalse(success.isSuccess());

		Assert.assertEquals(ECSTASK_STATUS.STOPPED, success.getLastStatus());
	}
}
