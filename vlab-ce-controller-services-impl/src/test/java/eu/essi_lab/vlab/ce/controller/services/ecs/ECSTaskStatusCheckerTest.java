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
public class ECSTaskStatusCheckerTest {

	@Test
	public void testStackOverflow() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");
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

		checker.setUndertest(true);

		Mockito.doReturn(ECSTASK_STATUS.RUNNING).when(checker).status(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());

		checker.setWaitInterval(0L);

		Mockito.doReturn(true).when(checker).exitCodeSuccess();

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10L, "cluster");

		Assert.assertTrue(success.isSuccess());
	}

	@Test
	public void testEmptyTaskList() {

		EcsClient client = Mockito.mock(EcsClient.class);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		Mockito.when(taskResult.tasks()).thenReturn(new ArrayList<Task>());

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testNullTaskList() {

		EcsClient client = Mockito.mock(EcsClient.class);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		Mockito.when(taskResult.tasks()).thenReturn(null);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testOk() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");
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

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertTrue(success.isSuccess());
	}

	@Test
	public void testOkAfterInitialNullTasks() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");
		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(0);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(null, new ArrayList<>(), tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertTrue(success.isSuccess());
	}

	@Test
	public void testTaskFailure() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failues = new ArrayList<>();

		Failure failure = Mockito.mock(Failure.class);
		failues.add(failure);

		Mockito.when(taskResult.failures()).thenReturn(failues);

		EcsClient client = Mockito.mock(EcsClient.class);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 5000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testFailureTimeout() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("RUNNING");

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = Mockito.spy(new ECSTaskStatusChecker(client));

		checker.setUndertest(true);
		checker.setMaxIterations(5);
		checker.setWaitInterval(3000L);

		Mockito.doReturn(true).when(checker).exitCodeSuccess();

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 5000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testFailureStoppedEmptyContainer() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");
		List<Container> containers = new ArrayList<>();

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testFailureStoppedNullContainerExitCode() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");
		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(null);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testFailureStoppedNoContainer() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");

		Mockito.when(task.containers()).thenReturn(null);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testFailureStopped() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");
		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		Mockito.when(container.exitCode()).thenReturn(1);

		containers.add(container);

		Mockito.when(task.containers()).thenReturn(containers);

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(tasks);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testFailureNoStatus() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(null);

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		checker.setMaxTriesToGetStatus(2);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testFailureEmptyStatus() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.when(task.lastStatus()).thenReturn("STOPPED");

		tasks.add(task);

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		EcsClient client = Mockito.mock(EcsClient.class);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		Mockito.when(status.tasks()).thenReturn(new ArrayList<>());

		Mockito.when(client.describeTasks((DescribeTasksRequest) Mockito.any())).thenReturn(status);

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);
		checker.setMaxTriesToGetStatus(2);
		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertFalse(success.isSuccess());
	}

	@Test
	public void testOkRunningStopped() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("RUNNING", "STOPPED").when(task).lastStatus();

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

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertTrue(success.isSuccess());
	}

	@Test
	public void testOkPendingRunningStopped() {

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

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		ECSTaskCheckResponse success = checker.taskCompletedWithSuccess(taskResult, 10000L, "cluster");

		Assert.assertTrue(success.isSuccess());
	}

	@Test
	public void testReservationOkPendingRunning() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("PENDING", "RUNNING").when(task).lastStatus();

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

		boolean success = checker.reservationTaskRunning(taskResult, "cluster");

		Assert.assertTrue(success);

		Mockito.verify(checker, Mockito.times(2)).reservationTaskRunning(Mockito.any(), Mockito.any());
	}

	@Test
	public void testReservationOkRunning() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("RUNNING").when(task).lastStatus();

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

		boolean success = checker.reservationTaskRunning(taskResult, "cluster");

		Assert.assertTrue(success);
		Mockito.verify(checker, Mockito.times(1)).reservationTaskRunning(Mockito.any(), Mockito.any());
	}

	@Test
	public void testReservationFailStopped() {

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		Mockito.doReturn("STOPPED").when(task).lastStatus();

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

		ECSTaskStatusChecker checker = new ECSTaskStatusChecker(client);

		boolean success = checker.reservationTaskRunning(taskResult, "cluster");

		Assert.assertFalse(success);
	}

}