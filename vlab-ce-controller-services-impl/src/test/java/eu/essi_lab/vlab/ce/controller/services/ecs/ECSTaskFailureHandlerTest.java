package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
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
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * @author Mattia Santoro
 */
public class ECSTaskFailureHandlerTest {

	@Test
	public void rerunTest() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failures = new ArrayList<>();
		Failure failure = Mockito.mock(Failure.class);
		failures.add(failure);

		Mockito.when(failure.reason()).thenReturn("AGENT");

		Mockito.when(taskResult.failures()).thenReturn(failures);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		//	ECSTaskManager manager = Mockito.spy(new ECSTaskManager(client, request));
		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ContainerOrchestratorCommandResult r = handler.handleFailure(taskResult, manager, 0, "cluster", 10000L, request);

		Mockito.verify(client, Mockito.times(1)).runTask((RunTaskRequest) Mockito.any());

	}

	@Test
	public void searchContainerFailure() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failures = new ArrayList<>();

		Mockito.when(taskResult.failures()).thenReturn(failures);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ContainerOrchestratorCommandResult r = handler.handleFailure(taskResult, manager, 0, "cluster", 10000L, request);

		Assert.assertEquals(ECSTaskFailureHandler.TASK_FAILURE_WITH_CAUSE + ECSTaskFailureHandler.UNKNOWN_FAILURE_CAUSE, r.getMessage());

	}

	@Test
	public void foundContainerFailure() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failures = new ArrayList<>();

		Mockito.when(taskResult.failures()).thenReturn(failures);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		List<Task> containerTasks = new ArrayList<>();

		Task containerTask = Mockito.mock(Task.class);

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		String containerReason = "containerReason";

		Mockito.doReturn(containerReason).when(container).reason();

		Integer containerExitCode = 12;

		Mockito.doReturn(containerExitCode).when(container).exitCode();

		containers.add(container);

		Mockito.doReturn(containers).when(containerTask).containers();

		containerTasks.add(containerTask);

		Mockito.doReturn(containerTasks).when(status).tasks();

		Mockito.doReturn(status).when(client).describeTasks((DescribeTasksRequest) Mockito.any());

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ContainerOrchestratorCommandResult r = handler.handleFailure(taskResult, manager, 0, "cluster", 10000L, request);

		Assert.assertEquals(ECSTaskFailureHandler.TASK_FAILURE_WITH_CAUSE + containerReason + ECSTaskFailureHandler.AND_EXIT_CODE + containerExitCode, r.getMessage());

	}

	@Test
	public void searchContainerNoContainers() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failures = new ArrayList<>();

		Mockito.when(taskResult.failures()).thenReturn(failures);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		List<Task> containerTasks = new ArrayList<>();

		Task containerTask = Mockito.mock(Task.class);

		List<Container> containers = new ArrayList<>();

		Mockito.doReturn(containers).when(containerTask).containers();

		containerTasks.add(containerTask);

		Mockito.doReturn(containerTasks).when(status).tasks();

		Mockito.doReturn(status).when(client).describeTasks((DescribeTasksRequest) Mockito.any());

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ContainerOrchestratorCommandResult r = handler.handleFailure(taskResult, manager, 0, "cluster", 10000L, request);

		Assert.assertEquals(ECSTaskFailureHandler.TASK_FAILURE_WITH_CAUSE + ECSTaskFailureHandler.UNKNOWN_FAILURE_CAUSE, r.getMessage());

	}

	@Test
	public void searchContainerNoContainerTasks() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failures = new ArrayList<>();

		Mockito.when(taskResult.failures()).thenReturn(failures);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		List<Task> containerTasks = new ArrayList<>();

		Mockito.doReturn(containerTasks).when(status).tasks();

		Mockito.doReturn(status).when(client).describeTasks((DescribeTasksRequest) Mockito.any());

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ContainerOrchestratorCommandResult r = handler.handleFailure(taskResult, manager, 0, "cluster", 10000L, request);

		Assert.assertEquals(ECSTaskFailureHandler.TASK_FAILURE_WITH_CAUSE + ECSTaskFailureHandler.UNKNOWN_FAILURE_CAUSE, r.getMessage());

	}

	@Test
	public void foundContainerFailureNoReason() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failures = new ArrayList<>();

		Mockito.when(taskResult.failures()).thenReturn(failures);

		List<Task> tasks = new ArrayList<>();

		Task task = Mockito.mock(Task.class);

		String taskArn = "test:task:arn";

		Mockito.doReturn(taskArn).when(task).taskArn();

		tasks.add(task);

		DescribeTasksResponse status = Mockito.mock(DescribeTasksResponse.class);

		List<Task> containerTasks = new ArrayList<>();

		Task containerTask = Mockito.mock(Task.class);

		List<Container> containers = new ArrayList<>();

		Container container = Mockito.mock(Container.class);

		String containerReason = null;

		Mockito.doReturn(containerReason).when(container).reason();

		Integer containerExitCode = 12;

		Mockito.doReturn(containerExitCode).when(container).exitCode();

		containers.add(container);

		Mockito.doReturn(containers).when(containerTask).containers();

		containerTasks.add(containerTask);

		Mockito.doReturn(containerTasks).when(status).tasks();

		Mockito.doReturn(status).when(client).describeTasks((DescribeTasksRequest) Mockito.any());

		Mockito.when(taskResult.tasks()).thenReturn(tasks);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);

		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setRetryInterval(2000L);

		ContainerOrchestratorCommandResult r = handler.handleFailure(taskResult, manager, 0, "cluster", 10000L, request);

		Assert.assertEquals(
				ECSTaskFailureHandler.TASK_FAILURE_WITH_CAUSE + ECSTaskFailureHandler.UNKNOWN_FAILURE_CAUSE + ECSTaskFailureHandler.AND_EXIT_CODE + containerExitCode, r.getMessage());

	}

	@Test
	public void rerunTestMaxRetriesZero() {

		EcsClient client = Mockito.mock(EcsClient.class);

		RunTaskResponse taskResult = Mockito.mock(RunTaskResponse.class);

		List<Failure> failures = new ArrayList<>();
		Failure failure = Mockito.mock(Failure.class);
		failures.add(failure);

		Mockito.when(failure.reason()).thenReturn("AGENT");

		Mockito.when(taskResult.failures()).thenReturn(failures);

		RunTaskRequest request = Mockito.mock(RunTaskRequest.class);
		//	ECSTaskManager manager = Mockito.spy(new ECSTaskManager(client, request));
		ECSTaskManager manager = Mockito.mock(ECSTaskManager.class);

		ContainerOrchestratorCommandResult result = new ContainerOrchestratorCommandResult();

		ECSTaskFailureHandler handler = new ECSTaskFailureHandler(client);
		handler.setMaxRetries(0);
		ContainerOrchestratorCommandResult r = handler.handleFailure(taskResult, manager, 0, "cluster", 10000L, request);

		Mockito.verify(client, Mockito.times(0)).runTask((RunTaskRequest) Mockito.any());

	}

}