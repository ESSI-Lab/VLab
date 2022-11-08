package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsResponse;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;
import software.amazon.awssdk.services.ec2.model.MemoryInfo;

/**
 * @author Mattia Santoro
 */
public class AWSGroupResourcesMapperTest {

	@Test
	public void test() {
		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);

		Ec2Client ec2client = Mockito.mock(Ec2Client.class);

		AutoScalingGroup group = Mockito.mock(AutoScalingGroup.class);

		String groupLcName = "groupLcName";

		Mockito.doReturn(groupLcName).when(group).launchConfigurationName();

		LaunchConfiguration conf = Mockito.mock(LaunchConfiguration.class);

		String instanceType = "m3.large";

		Mockito.doReturn(instanceType).when(conf).instanceType();

		DescribeLaunchConfigurationsResponse describeLaunchConfigurationsResult = Mockito.mock(DescribeLaunchConfigurationsResponse.class);

		List<LaunchConfiguration> list = new ArrayList<>();
		list.add(conf);

		Mockito.doReturn(list).when(describeLaunchConfigurationsResult).launchConfigurations();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeLaunchConfigurationsRequest describeLaunchConfigurationsRequest = (DescribeLaunchConfigurationsRequest) invocation.getArguments()[0];

				String relcname = describeLaunchConfigurationsRequest.launchConfigurationNames().get(0);

				if (!groupLcName.equalsIgnoreCase(relcname))
					throw new Exception("Invalid launch conf name found " + relcname);

				return describeLaunchConfigurationsResult;
			}

		}).when(client).describeLaunchConfigurations((DescribeLaunchConfigurationsRequest) Mockito.any());

		DescribeInstanceTypesResponse instanceTypesResponse = Mockito.mock(DescribeInstanceTypesResponse.class);

		List<InstanceTypeInfo> types = new ArrayList();

		InstanceTypeInfo info = Mockito.mock(InstanceTypeInfo.class);

		MemoryInfo memifo = Mockito.mock(MemoryInfo.class);

		Mockito.doReturn(Long.valueOf(7500)).when(memifo).sizeInMiB();
		Mockito.doReturn(memifo).when(info).memoryInfo();

		types.add(info);

		Mockito.doReturn(types).when(instanceTypesResponse).instanceTypes();

		Mockito.doReturn(instanceTypesResponse).when(ec2client).describeInstanceTypes((DescribeInstanceTypesRequest) Mockito.any());

		AWSGroupResourcesMapper mapper = new AWSGroupResourcesMapper(group, client, ec2client);

		assertTrue(mapper.meetRequirements(resources));
	}

	@Test
	public void test2() {
		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "10000";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);

		Ec2Client ec2client = Mockito.mock(Ec2Client.class);

		AutoScalingGroup group = Mockito.mock(AutoScalingGroup.class);

		String groupLcName = "groupLcName";

		Mockito.doReturn(groupLcName).when(group).launchConfigurationName();

		LaunchConfiguration conf = Mockito.mock(LaunchConfiguration.class);

		String instanceType = "m3.large";

		Mockito.doReturn(instanceType).when(conf).instanceType();

		DescribeLaunchConfigurationsResponse describeLaunchConfigurationsResult = Mockito.mock(DescribeLaunchConfigurationsResponse.class);

		List<LaunchConfiguration> list = new ArrayList<>();
		list.add(conf);

		Mockito.doReturn(list).when(describeLaunchConfigurationsResult).launchConfigurations();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeLaunchConfigurationsRequest describeLaunchConfigurationsRequest = (DescribeLaunchConfigurationsRequest) invocation.getArguments()[0];

				String relcname = describeLaunchConfigurationsRequest.launchConfigurationNames().get(0);

				if (!groupLcName.equalsIgnoreCase(relcname))
					throw new Exception("Invalid launch conf name found " + relcname);

				return describeLaunchConfigurationsResult;
			}

		}).when(client).describeLaunchConfigurations((DescribeLaunchConfigurationsRequest) Mockito.any());

		DescribeInstanceTypesResponse instanceTypesResponse = Mockito.mock(DescribeInstanceTypesResponse.class);

		List<InstanceTypeInfo> types = new ArrayList();

		InstanceTypeInfo info = Mockito.mock(InstanceTypeInfo.class);

		MemoryInfo memifo = Mockito.mock(MemoryInfo.class);

		Mockito.doReturn(Long.valueOf(7500)).when(memifo).sizeInMiB();
		Mockito.doReturn(memifo).when(info).memoryInfo();

		types.add(info);

		Mockito.doReturn(types).when(instanceTypesResponse).instanceTypes();

		Mockito.doReturn(instanceTypesResponse).when(ec2client).describeInstanceTypes((DescribeInstanceTypesRequest) Mockito.any());

		AWSGroupResourcesMapper mapper = new AWSGroupResourcesMapper(group, client, ec2client);

		assertFalse(mapper.meetRequirements(resources));
	}

	@Test
	public void test3() {
		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);

		Ec2Client ec2client = Mockito.mock(Ec2Client.class);

		AutoScalingGroup group = Mockito.mock(AutoScalingGroup.class);

		String groupLcName = "groupLcName";

		Mockito.doReturn(groupLcName).when(group).launchConfigurationName();

		LaunchConfiguration conf = Mockito.mock(LaunchConfiguration.class);

		String instanceType = "m3.large";

		Mockito.doReturn(instanceType).when(conf).instanceType();

		DescribeLaunchConfigurationsResponse describeLaunchConfigurationsResult = Mockito.mock(DescribeLaunchConfigurationsResponse.class);

		List<LaunchConfiguration> list = new ArrayList<>();
		list.add(conf);

		Mockito.doReturn(list).when(describeLaunchConfigurationsResult).launchConfigurations();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeLaunchConfigurationsRequest describeLaunchConfigurationsRequest = (DescribeLaunchConfigurationsRequest) invocation.getArguments()[0];

				String relcname = describeLaunchConfigurationsRequest.launchConfigurationNames().get(0);

				if (!groupLcName.equalsIgnoreCase(relcname))
					throw new Exception("Invalid launch conf name found " + relcname);

				return describeLaunchConfigurationsResult;
			}

		}).when(client).describeLaunchConfigurations((DescribeLaunchConfigurationsRequest) Mockito.any());

		AWSGroupResourcesMapper mapper = new AWSGroupResourcesMapper(group, client, ec2client);

		assertTrue(mapper.meetRequirements(resources));
	}
}