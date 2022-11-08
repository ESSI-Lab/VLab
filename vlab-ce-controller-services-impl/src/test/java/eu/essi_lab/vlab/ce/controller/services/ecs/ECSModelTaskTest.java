package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.ce.controller.services.BPExceptionMatcher;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.VLabDockerContainer;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ContainerDefinition;
import software.amazon.awssdk.services.ecs.model.DescribeTaskDefinitionRequest;
import software.amazon.awssdk.services.ecs.model.DescribeTaskDefinitionResponse;
import software.amazon.awssdk.services.ecs.model.ListTaskDefinitionsRequest;
import software.amazon.awssdk.services.ecs.model.ListTaskDefinitionsResponse;
import software.amazon.awssdk.services.ecs.model.MountPoint;
import software.amazon.awssdk.services.ecs.model.NetworkMode;
import software.amazon.awssdk.services.ecs.model.RegisterTaskDefinitionRequest;
import software.amazon.awssdk.services.ecs.model.RegisterTaskDefinitionResponse;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.TaskDefinition;
import software.amazon.awssdk.services.ecs.model.Volume;

/**
 * @author Mattia Santoro
 */
public class ECSModelTaskTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testCreateTaskDefinitionImageEntryPoint() {

		ECSModelTask etm = new ECSModelTask(null, "us-east-1", "folder", "name", "", "", "");

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		Mockito.when(image.getImage()).thenReturn("repo/alpine:3.5-sftp-ssl");

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getCommand()).thenReturn(Arrays.asList(new String[] { "echo ciao" }));

		Mockito.when(container.getEntryPoint()).thenReturn(Arrays.asList(new String[] { "/bin/sh", "-c" }));

		Mockito.when(image.getContainer()).thenReturn(container);

		TaskDefinition taskDefinition = etm.createTaskDefinition(image);

		assertNotNull(taskDefinition);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(taskDefinition);

		assertTrue(matcher.match(image));

	}

	@Test
	public void testCreateTaskDefinitionImage() {

		ECSModelTask etm = new ECSModelTask(null, "us-east-1", "folder", "name", "", "", "");

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		Mockito.when(image.getImage()).thenReturn("repo/alpine:3.5-sftp-ssl");

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getCommand()).thenReturn(Arrays.asList(new String[] { "echo ciao" }));

		Mockito.when(image.getContainer()).thenReturn(container);

		TaskDefinition taskDefinition = etm.createTaskDefinition(image);

		assertNotNull(taskDefinition);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(taskDefinition);

		assertTrue(matcher.match(image));

	}

	@Test
	public void testCreateRunRequest() throws BPException {

		EcsClient ecsClient = Mockito.mock(EcsClient.class);

		String region = "us-east-1";

		ECSModelTask etm = Mockito.spy(new ECSModelTask(ecsClient, region, "folder", "name", "", "", ""));
		etm.setRunFolder("/runfolder");
		String runid = "runid";

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getCommand()).thenReturn(Arrays.asList(new String[] { "echo ciao" }));

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String mem = "100";
		String cpu = "2";

		Mockito.when(resources.getMemory_mb()).thenReturn(mem);
		Mockito.when(resources.getCpu_units()).thenReturn(cpu);

		Mockito.when(image.getResources()).thenReturn(resources);

		Mockito.when(image.getContainer()).thenReturn(container);

		Mockito.doReturn(null).when(etm).findTaskDefinition(Mockito.any());

		TaskDefinition taskDefinition = Mockito.mock(TaskDefinition.class);

		Mockito.doReturn(taskDefinition).when(etm).createTaskDefinition(Mockito.any());
		Mockito.doReturn(taskDefinition).when(etm).storeTaskDefinition(Mockito.any());

		String arn = "arn:task";

		Mockito.doReturn(arn).when(taskDefinition).taskDefinitionArn();

		ContainerDefinition containerCreated = Mockito.mock(ContainerDefinition.class);

		List<ContainerDefinition> containers = new ArrayList<>();

		containers.add(containerCreated);

		String containerName = "containerName";

		Mockito.doReturn(containerName).when(containerCreated).name();

		Mockito.doReturn(containers).when(taskDefinition).containerDefinitions();

		RunTaskRequest run = etm.createRunTaskRequest(image, runid);

		assertNotNull(run);

		assertEquals(arn, run.taskDefinition());

		assertEquals("cd /runfolder && echo ciao", run.overrides().containerOverrides().get(0).command().get(0));

		assertEquals("BENGINERUNID", run.overrides().containerOverrides().get(0).environment().get(0).name());
		assertEquals(runid, run.overrides().containerOverrides().get(0).environment().get(0).value());


		assertEquals("vlabrunid", run.overrides().containerOverrides().get(0).environment().get(1).name());
		assertEquals(runid, run.overrides().containerOverrides().get(0).environment().get(1).value());

		assertEquals(Integer.valueOf(mem), run.overrides().containerOverrides().get(0).memoryReservation());
		assertEquals(Integer.valueOf(cpu), run.overrides().containerOverrides().get(0).cpu());

	}

	@Test
	public void testCreateRunRequest2() throws BPException {

		EcsClient ecsClient = Mockito.mock(EcsClient.class);

		String region = "us-east-1";
		ECSModelTask etm = Mockito.spy(new ECSModelTask(ecsClient, region, "folder", "name", "", "", ""));
		etm.setRunFolder("/runfolder");
		String runid = "runid";

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getCommand()).thenReturn(Arrays.asList(new String[] { "echo ciao" }));

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String mem = "100";
		String cpu = "2";

		Mockito.when(resources.getMemory_mb()).thenReturn(mem);
		Mockito.when(resources.getCpu_units()).thenReturn(cpu);

		Mockito.when(image.getResources()).thenReturn(resources);

		Mockito.when(image.getContainer()).thenReturn(container);

		TaskDefinition taskDefinition = Mockito.mock(TaskDefinition.class);

		String arn = "arn:task";

		Mockito.doReturn(arn).when(taskDefinition).taskDefinitionArn();

		ContainerDefinition containerCreated = Mockito.mock(ContainerDefinition.class);

		List<ContainerDefinition> containers = new ArrayList<>();

		containers.add(containerCreated);

		String containerName = "containerName";

		Mockito.doReturn(containerName).when(containerCreated).name();

		Mockito.doReturn(containers).when(taskDefinition).containerDefinitions();

		Mockito.doReturn(taskDefinition).when(etm).findTaskDefinition(Mockito.any());

		RunTaskRequest run = etm.createRunTaskRequest(image, runid);

		assertNotNull(run);

		assertEquals(arn, run.taskDefinition());

		assertEquals("cd /runfolder && echo ciao", run.overrides().containerOverrides().get(0).command().get(0));

		assertEquals("BENGINERUNID", run.overrides().containerOverrides().get(0).environment().get(0).name());
		assertEquals(runid, run.overrides().containerOverrides().get(0).environment().get(0).value());


		assertEquals("vlabrunid", run.overrides().containerOverrides().get(0).environment().get(1).name());
		assertEquals(runid, run.overrides().containerOverrides().get(0).environment().get(1).value());

		assertEquals(Integer.valueOf(mem), run.overrides().containerOverrides().get(0).memoryReservation());
		assertEquals(Integer.valueOf(cpu), run.overrides().containerOverrides().get(0).cpu());

		Mockito.verify(etm, Mockito.times(0)).storeTaskDefinition(Mockito.any());
		Mockito.verify(etm, Mockito.times(0)).createTaskDefinition(Mockito.any());

	}

	@Test
	public void testStoreTaskDefinition() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Unable to register task definition without task family", BPException.ERROR_CODES.NO_TASK_FAMILY));

		EcsClient ecsClient = Mockito.mock(EcsClient.class);

		String region = "us-east-1";
		ECSModelTask etm = Mockito.spy(new ECSModelTask(ecsClient, region, "folder", "name", "", "", ""));

		TaskDefinition task = Mockito.mock(TaskDefinition.class);

		etm.storeTaskDefinition(task);
	}

	@Test
	public void testStoreTaskDefinition2() throws BPException {

		EcsClient ecsClient = Mockito.mock(EcsClient.class);

		String region = "us-east-1";
		ECSModelTask etm = Mockito.spy(new ECSModelTask(ecsClient, region, "/folder", "name", "", "", "/rp"));

		String family = "family";

		etm.setTaskFamily(family);

		TaskDefinition task = Mockito.mock(TaskDefinition.class);

		ContainerDefinition containerDefinition = ContainerDefinition.builder().build();

		List<ContainerDefinition> containers = new ArrayList<>();

		containers.add(containerDefinition);

		Mockito.doReturn(containers).when(task).containerDefinitions();

		String execArn = "execArn";

		Mockito.doReturn(execArn).when(task).executionRoleArn();

		RegisterTaskDefinitionResponse response = Mockito.mock(RegisterTaskDefinitionResponse.class);

		Mockito.doReturn(task).when(response).taskDefinition();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				RegisterTaskDefinitionRequest request = (RegisterTaskDefinitionRequest) invocation.getArguments()[0];

				ContainerDefinition container = request.containerDefinitions().get(0);

				if (!"name".equalsIgnoreCase(container.name()))
					throw new Exception("Bad container name: " + container.name());

				if (128 - container.memoryReservation() != 0)
					throw new Exception("Bad memory: " + container.memoryReservation());

				MountPoint mount = container.mountPoints().get(0);

				if (!"EFSRoot".equalsIgnoreCase(mount.sourceVolume()))
					throw new Exception("Bad volume source name: " + mount.sourceVolume());

				if (!"/rp".equalsIgnoreCase(mount.containerPath()))
					throw new Exception("Bad volume path: " + mount.containerPath());

				if (!family.equalsIgnoreCase(request.family()))
					throw new Exception("Bad family: " + request.family());

				if (!NetworkMode.BRIDGE.toString().equalsIgnoreCase(request.networkMode().toString()))
					throw new Exception("Bad network: " + request.networkMode());

				if (!execArn.equalsIgnoreCase(request.executionRoleArn()))
					throw new Exception("Bad ExecutionRoleArn: " + request.executionRoleArn());

				Volume volume = request.volumes().get(0);

				if (!"EFSRoot".equalsIgnoreCase(volume.name()))
					throw new Exception("Bad volume name: " + volume.name());

				if (!"/folder".equalsIgnoreCase(volume.host().sourcePath()))
					throw new Exception("Bad volume path: " + volume.host().sourcePath());

				return response;
			}
		}).when(ecsClient).registerTaskDefinition((RegisterTaskDefinitionRequest) Mockito.any());

		assertNotNull(etm.storeTaskDefinition(task));
	}

	@Test
	public void testStoreTaskDefinition3() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Unable to register task definition for the provided image with exception",
				BPException.ERROR_CODES.TASK_REGISTRATION_FAIL));

		EcsClient ecsClient = Mockito.mock(EcsClient.class);

		String region = "us-east-1";
		ECSModelTask etm = Mockito.spy(new ECSModelTask(ecsClient, region, "folder", "name", "", "", ""));

		String family = "family";

		etm.setTaskFamily(family);

		TaskDefinition task = Mockito.mock(TaskDefinition.class);

		ContainerDefinition containerDefinition = ContainerDefinition.builder().build();

		List<ContainerDefinition> containers = new ArrayList<>();

		containers.add(containerDefinition);

		Mockito.doReturn(containers).when(task).containerDefinitions();

		String execArn = "execArn";

		Mockito.doReturn(execArn).when(task).executionRoleArn();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				RegisterTaskDefinitionRequest request = (RegisterTaskDefinitionRequest) invocation.getArguments()[0];

				ContainerDefinition container = request.containerDefinitions().get(0);

				if (!"AutogeneratedBPEngineContainer".equalsIgnoreCase(container.name()))
					throw new Exception("Bad container name: " + container.name());

				if (128 - container.memoryReservation() != 0)
					throw new Exception("Bad memory: " + container.memoryReservation());

				MountPoint mount = container.mountPoints().get(0);

				if (!"EFSRoot".equalsIgnoreCase(mount.sourceVolume()))
					throw new Exception("Bad volume source name: " + mount.sourceVolume());

				if (!"/vlab".equalsIgnoreCase(mount.containerPath()))
					throw new Exception("Bad volume path: " + mount.containerPath());

				if (!family.equalsIgnoreCase(request.family()))
					throw new Exception("Bad family: " + request.family());

				if (!NetworkMode.BRIDGE.toString().equalsIgnoreCase(request.networkMode().toString()))
					throw new Exception("Bad network: " + request.networkMode());

				if (!execArn.equalsIgnoreCase(request.executionRoleArn()))
					throw new Exception("Bad ExecutionRoleArn: " + request.executionRoleArn());

				Volume volume = request.volumes().get(0);

				if (!"EFSRoot".equalsIgnoreCase(volume.name()))
					throw new Exception("Bad volume name: " + volume.name());

				if (!"/efs/models".equalsIgnoreCase(volume.host().sourcePath()))
					throw new Exception("Bad volume path: " + volume.host().sourcePath());

				throw new Exception();
			}
		}).when(ecsClient).registerTaskDefinition((RegisterTaskDefinitionRequest) Mockito.any());

		etm.storeTaskDefinition(task);
	}

	@Test
	public void testFindTaskDefinition() throws BPException {

		EcsClient ecsClient = Mockito.mock(EcsClient.class);

		String region = "us-east-1";
		ECSModelTask etm = Mockito.spy(new ECSModelTask(ecsClient, region, "folder", "name", "", "", ""));

		String family = "family";

		etm.setTaskFamily(family);

		VLabDockerImage dockerImage = Mockito.mock(VLabDockerImage.class);

		String nextToken = "nextToken";

		ListTaskDefinitionsResponse response1 = Mockito.mock(ListTaskDefinitionsResponse.class);

		Mockito.doReturn(nextToken).when(response1).nextToken();

		List<String> arns1 = new ArrayList<>();

		arns1.add("arn");

		Mockito.doReturn(arns1).when(response1).taskDefinitionArns();

		String nextToken2 = "nextToken2";
		ListTaskDefinitionsResponse response2 = Mockito.mock(ListTaskDefinitionsResponse.class);

		Mockito.doReturn(nextToken2).when(response2).nextToken();

		List<String> arns2 = new ArrayList<>();

		arns2.add("arn2");

		Mockito.doReturn(arns2).when(response2).taskDefinitionArns();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListTaskDefinitionsRequest request = (ListTaskDefinitionsRequest) invocation.getArguments()[0];

				if (!family.equalsIgnoreCase(request.familyPrefix()))
					throw new Exception("Bad family: " + request.familyPrefix());

				if (request.nextToken() != null)
					throw new Exception("Expected null token");

				return response1;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListTaskDefinitionsRequest request = (ListTaskDefinitionsRequest) invocation.getArguments()[0];

				if (!family.equalsIgnoreCase(request.familyPrefix()))
					throw new Exception("Bad family: " + request.familyPrefix());

				if (!nextToken.equalsIgnoreCase(request.nextToken()))
					throw new Exception("Expected next token found: " + request.nextToken());

				return response2;
			}
		}).when(ecsClient).listTaskDefinitions((ListTaskDefinitionsRequest) Mockito.any());

		DescribeTaskDefinitionResponse describeTaskDefinitionResult1 = Mockito.mock(DescribeTaskDefinitionResponse.class);

		TaskDefinition t1 = Mockito.mock(TaskDefinition.class);
		Mockito.doReturn(t1).when(describeTaskDefinitionResult1).taskDefinition();

		DescribeTaskDefinitionResponse describeTaskDefinitionResult2 = Mockito.mock(DescribeTaskDefinitionResponse.class);

		TaskDefinition t2 = Mockito.mock(TaskDefinition.class);
		Mockito.doReturn(t2).when(describeTaskDefinitionResult2).taskDefinition();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeTaskDefinitionRequest request = (DescribeTaskDefinitionRequest) invocation.getArguments()[0];

				if (!arns1.get(0).equalsIgnoreCase(request.taskDefinition()))
					throw new Exception("Bad arn: " + request.taskDefinition());

				return describeTaskDefinitionResult1;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeTaskDefinitionRequest request = (DescribeTaskDefinitionRequest) invocation.getArguments()[0];

				if (!arns2.get(0).equalsIgnoreCase(request.taskDefinition()))
					throw new Exception("Bad arn: " + request.taskDefinition());

				return describeTaskDefinitionResult2;
			}
		}).when(ecsClient).describeTaskDefinition((DescribeTaskDefinitionRequest) Mockito.any());

		TaskDefinitionMatcher matcher = Mockito.mock(TaskDefinitionMatcher.class);

		Mockito.doReturn(false).doReturn(true).when(matcher).match(Mockito.any());

		Mockito.doReturn(matcher).when(etm).createTaskMatcher(Mockito.any());

		TaskDefinition task = etm.findTaskDefinition(dockerImage);

		assertNotNull(task);
	}
}