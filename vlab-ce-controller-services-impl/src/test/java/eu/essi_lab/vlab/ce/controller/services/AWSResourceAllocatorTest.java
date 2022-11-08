package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.ce.controller.services.ecs.AWSGroupResourcesMapper;
import eu.essi_lab.vlab.core.datamodel.ResourceRequestResponse;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.autoscaling.model.DescribePoliciesRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribePoliciesResponse;
import software.amazon.awssdk.services.autoscaling.model.ExecutePolicyRequest;
import software.amazon.awssdk.services.autoscaling.model.ExecutePolicyResponse;
import software.amazon.awssdk.services.autoscaling.model.ScalingPolicy;

/**
 * @author Mattia Santoro
 */
public class AWSResourceAllocatorTest {

	@Test
	public void testRequestNoGroupMatching() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return Optional.empty();
			}
		}).when(awsResourceAllocator).findRequiredGroup(Mockito.any());

		ResourceRequestResponse allocatorResponse = awsResourceAllocator.request(resources);

		assertFalse(allocatorResponse.requestSent());

	}

	@Test
	public void testRequestNoPolicyFound() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		AutoScalingGroup foundGroup = Mockito.mock(AutoScalingGroup.class);

		String groupName = "autoscalingName";

		Mockito.doReturn(groupName).when(foundGroup).autoScalingGroupName();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return Optional.of(foundGroup);
			}
		}).when(awsResourceAllocator).findRequiredGroup(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				AutoScalingGroup gname = (AutoScalingGroup) invocation.getArguments()[0];

				if (!groupName.equalsIgnoreCase(gname.autoScalingGroupName()))
					throw new Exception("Found invalid group name " + gname.autoScalingGroupName());

				return Optional.empty();
			}
		}).when(awsResourceAllocator).findIncreasePolicy(Mockito.any());

		ResourceRequestResponse allocatorResponse = awsResourceAllocator.request(resources);

		assertFalse(allocatorResponse.requestSent());

		assertEquals(groupName, allocatorResponse.getGroupName());

	}

	@Test
	public void testRequestExecPolicyFound() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		AutoScalingGroup foundGroup = Mockito.mock(AutoScalingGroup.class);

		String groupName = "autoscalingName";

		Mockito.doReturn(groupName).when(foundGroup).autoScalingGroupName();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return Optional.of(foundGroup);
			}
		}).when(awsResourceAllocator).findRequiredGroup(Mockito.any());

		ScalingPolicy policy = Mockito.mock(ScalingPolicy.class);

		String policyName = "polName";

		Mockito.doReturn(policyName).when(policy).policyName();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				AutoScalingGroup gname = (AutoScalingGroup) invocation.getArguments()[0];

				if (!groupName.equalsIgnoreCase(gname.autoScalingGroupName()))
					throw new Exception("Found invalid group name " + gname.autoScalingGroupName());

				return Optional.of(policy);
			}
		}).when(awsResourceAllocator).findIncreasePolicy(Mockito.any());

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ExecutePolicyRequest executePolicyRequest = (ExecutePolicyRequest) invocation.getArguments()[0];

				if (!groupName.equalsIgnoreCase(executePolicyRequest.autoScalingGroupName()))
					throw new Exception("Found invalid group name " + executePolicyRequest.autoScalingGroupName());

				if (!policyName.equalsIgnoreCase(executePolicyRequest.policyName()))
					throw new Exception("Found invalid policy name " + executePolicyRequest.policyName());

				return Mockito.mock(ExecutePolicyResponse.class);
			}

		}).when(client).executePolicy((ExecutePolicyRequest) Mockito.any());

		ResourceRequestResponse allocatorResponse = awsResourceAllocator.request(resources);

		Mockito.verify(awsResourceAllocator, Mockito.times(1)).execPolicy(Mockito.any(), Mockito.any());

		assertTrue(allocatorResponse.requestSent());

		assertEquals(groupName, allocatorResponse.getGroupName());
		assertEquals(policyName, allocatorResponse.getUsableScalingPolicy());

	}

	@Test
	public void testFindPolicyNoPolicyFound() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		AutoScalingGroup foundGroup = Mockito.mock(AutoScalingGroup.class);

		String groupName = "autoscalingName";

		Mockito.doReturn(groupName).when(foundGroup).autoScalingGroupName();

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribePoliciesResponse policyResponse = Mockito.mock(DescribePoliciesResponse.class);

		List<ScalingPolicy> policies = new ArrayList<>();

		Mockito.doReturn(policies).when(policyResponse).scalingPolicies();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribePoliciesRequest describePoliciesRequest = (DescribePoliciesRequest) invocation.getArguments()[0];

				if (!groupName.equalsIgnoreCase(describePoliciesRequest.autoScalingGroupName()))
					throw new Exception("Found invalid group name " + describePoliciesRequest.autoScalingGroupName());

				return policyResponse;
			}

		}).when(client).describePolicies((DescribePoliciesRequest) Mockito.any());

		Optional<ScalingPolicy> optPolicy = awsResourceAllocator.findIncreasePolicy(foundGroup);

		assertFalse(optPolicy.isPresent());

	}

	@Test
	public void testFindPolicyNoIncreasePolicyFound() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		AutoScalingGroup foundGroup = Mockito.mock(AutoScalingGroup.class);

		String groupName = "autoscalingName";

		Mockito.doReturn(groupName).when(foundGroup).autoScalingGroupName();

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribePoliciesResponse policyResponse = Mockito.mock(DescribePoliciesResponse.class);

		List<ScalingPolicy> policies = new ArrayList<>();

		ScalingPolicy p = Mockito.mock(ScalingPolicy.class);

		Mockito.doReturn(-1).when(p).scalingAdjustment();

		policies.add(p);

		Mockito.doReturn(policies).when(policyResponse).scalingPolicies();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribePoliciesRequest describePoliciesRequest = (DescribePoliciesRequest) invocation.getArguments()[0];

				if (!groupName.equalsIgnoreCase(describePoliciesRequest.autoScalingGroupName()))
					throw new Exception("Found invalid group name " + describePoliciesRequest.autoScalingGroupName());

				return policyResponse;
			}

		}).when(client).describePolicies((DescribePoliciesRequest) Mockito.any());

		Optional<ScalingPolicy> optPolicy = awsResourceAllocator.findIncreasePolicy(foundGroup);

		assertFalse(optPolicy.isPresent());

	}

	@Test
	public void testFindPolicyIncreasePolicyFound() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		AutoScalingGroup foundGroup = Mockito.mock(AutoScalingGroup.class);

		String groupName = "autoscalingName";

		Mockito.doReturn(groupName).when(foundGroup).autoScalingGroupName();

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribePoliciesResponse policyResponse = Mockito.mock(DescribePoliciesResponse.class);

		List<ScalingPolicy> policies = new ArrayList<>();

		ScalingPolicy p = Mockito.mock(ScalingPolicy.class);

		Mockito.doReturn(1).when(p).scalingAdjustment();

		policies.add(p);

		Mockito.doReturn(policies).when(policyResponse).scalingPolicies();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribePoliciesRequest describePoliciesRequest = (DescribePoliciesRequest) invocation.getArguments()[0];

				if (!groupName.equalsIgnoreCase(describePoliciesRequest.autoScalingGroupName()))
					throw new Exception("Found invalid group name " + describePoliciesRequest.autoScalingGroupName());

				return policyResponse;
			}

		}).when(client).describePolicies((DescribePoliciesRequest) Mockito.any());

		Optional<ScalingPolicy> optPolicy = awsResourceAllocator.findIncreasePolicy(foundGroup);

		assertTrue(optPolicy.isPresent());

	}

	@Test
	public void testFindRequiredGroupNullDescFound() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		List<String> groups = new ArrayList<>();

		String g1 = "g1";
		String g2 = "g2";

		groups.add(g1);
		groups.add(g2);

		awsResourceAllocator.setAutoScalingGroups(groups);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribeAutoScalingGroupsResponse result = Mockito.mock(DescribeAutoScalingGroupsResponse.class);

		Mockito.doReturn(null).when(result).autoScalingGroups();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = (DescribeAutoScalingGroupsRequest) invocation.getArguments()[0];

				if (groups.size() - describeAutoScalingGroupsRequest.autoScalingGroupNames().size() != 0)
					throw new Exception("Found invalid group size " + describeAutoScalingGroupsRequest.autoScalingGroupNames().size());

				return result;
			}

		}).when(client).describeAutoScalingGroups((DescribeAutoScalingGroupsRequest) Mockito.any());

		Optional<AutoScalingGroup> requiredGroup = awsResourceAllocator.findRequiredGroup(resources);

		assertFalse(requiredGroup.isPresent());

	}

	@Test
	public void testFindRequiredGroupNoGroupDescFound() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		List<String> groups = new ArrayList<>();

		String g1 = "g1";
		String g2 = "g2";

		groups.add(g1);
		groups.add(g2);

		awsResourceAllocator.setAutoScalingGroups(groups);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribeAutoScalingGroupsResponse result = Mockito.mock(DescribeAutoScalingGroupsResponse.class);

		List<AutoScalingGroup> resultGroups = new ArrayList<>();

		Mockito.doReturn(resultGroups).when(result).autoScalingGroups();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = (DescribeAutoScalingGroupsRequest) invocation.getArguments()[0];

				if (groups.size() - describeAutoScalingGroupsRequest.autoScalingGroupNames().size() != 0)
					throw new Exception("Found invalid group size " + describeAutoScalingGroupsRequest.autoScalingGroupNames().size());

				return result;
			}

		}).when(client).describeAutoScalingGroups((DescribeAutoScalingGroupsRequest) Mockito.any());

		Optional<AutoScalingGroup> requiredGroup = awsResourceAllocator.findRequiredGroup(resources);

		assertFalse(requiredGroup.isPresent());

	}

	@Test
	public void testFindRequiredGroupNoGroupMeetsReq() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		List<String> groups = new ArrayList<>();

		String g1 = "g1";
		String g2 = "g2";

		groups.add(g1);
		groups.add(g2);

		awsResourceAllocator.setAutoScalingGroups(groups);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribeAutoScalingGroupsResponse result = Mockito.mock(DescribeAutoScalingGroupsResponse.class);

		List<AutoScalingGroup> resultGroups = new ArrayList<>();

		AutoScalingGroup asg1 = Mockito.mock(AutoScalingGroup.class);
		AutoScalingGroup asg2 = Mockito.mock(AutoScalingGroup.class);

		resultGroups.add(asg1);
		resultGroups.add(asg2);

		Mockito.doReturn(resultGroups).when(result).autoScalingGroups();

		AWSGroupResourcesMapper mapper = Mockito.mock(AWSGroupResourcesMapper.class);

		Mockito.doReturn(mapper).when(awsResourceAllocator).initMapper(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = (DescribeAutoScalingGroupsRequest) invocation.getArguments()[0];

				if (groups.size() - describeAutoScalingGroupsRequest.autoScalingGroupNames().size() != 0)
					throw new Exception("Found invalid group size " + describeAutoScalingGroupsRequest.autoScalingGroupNames().size());

				return result;
			}

		}).when(client).describeAutoScalingGroups((DescribeAutoScalingGroupsRequest) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return false;
			}
		}).when(mapper).meetRequirements(Mockito.any());

		Optional<AutoScalingGroup> requiredGroup = awsResourceAllocator.findRequiredGroup(resources);

		assertFalse(requiredGroup.isPresent());

		Mockito.verify(mapper, Mockito.times(2)).meetRequirements(Mockito.any());

	}

	@Test
	public void testFindRequiredGroupFirstGroupMatches() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		List<String> groups = new ArrayList<>();

		String g1 = "g1";
		String g2 = "g2";

		groups.add(g1);
		groups.add(g2);

		awsResourceAllocator.setAutoScalingGroups(groups);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribeAutoScalingGroupsResponse result = Mockito.mock(DescribeAutoScalingGroupsResponse.class);

		List<AutoScalingGroup> resultGroups = new ArrayList<>();

		AutoScalingGroup asg1 = Mockito.mock(AutoScalingGroup.class);
		String asg1Name = "asg1Name";
		Mockito.doReturn(asg1Name).when(asg1).autoScalingGroupName();

		AutoScalingGroup asg2 = Mockito.mock(AutoScalingGroup.class);
		String asg2Name = "asg2Name";
		Mockito.doReturn(asg2Name).when(asg2).autoScalingGroupName();

		resultGroups.add(asg1);
		resultGroups.add(asg2);

		Mockito.doReturn(resultGroups).when(result).autoScalingGroups();

		AWSGroupResourcesMapper mapper1 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg1).when(mapper1).getAutoScalingGroup();

		AWSGroupResourcesMapper mapper2 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg2).when(mapper2).getAutoScalingGroup();

		Mockito.doReturn(mapper1).doReturn(mapper2).when(awsResourceAllocator).initMapper(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = (DescribeAutoScalingGroupsRequest) invocation.getArguments()[0];

				if (groups.size() - describeAutoScalingGroupsRequest.autoScalingGroupNames().size() != 0)
					throw new Exception("Found invalid group size " + describeAutoScalingGroupsRequest.autoScalingGroupNames().size());

				return result;
			}

		}).when(client).describeAutoScalingGroups((DescribeAutoScalingGroupsRequest) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return true;
			}
		}).when(mapper1).meetRequirements(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return false;
			}
		}).when(mapper2).meetRequirements(Mockito.any());

		Optional<AutoScalingGroup> requiredGroup = awsResourceAllocator.findRequiredGroup(resources);

		assertTrue(requiredGroup.isPresent());

		Mockito.verify(mapper1, Mockito.times(1)).meetRequirements(Mockito.any());
		Mockito.verify(mapper2, Mockito.times(1)).meetRequirements(Mockito.any());

		assertEquals(asg1Name, requiredGroup.get().autoScalingGroupName());

	}

	@Test
	public void testFindRequiredGroupsecondGroupMatches() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		List<String> groups = new ArrayList<>();

		String g1 = "g1";
		String g2 = "g2";

		groups.add(g1);
		groups.add(g2);

		awsResourceAllocator.setAutoScalingGroups(groups);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribeAutoScalingGroupsResponse result = Mockito.mock(DescribeAutoScalingGroupsResponse.class);

		List<AutoScalingGroup> resultGroups = new ArrayList<>();

		AutoScalingGroup asg1 = Mockito.mock(AutoScalingGroup.class);
		String asg1Name = "asg1Name";
		Mockito.doReturn(asg1Name).when(asg1).autoScalingGroupName();

		AutoScalingGroup asg2 = Mockito.mock(AutoScalingGroup.class);
		String asg2Name = "asg2Name";
		Mockito.doReturn(asg2Name).when(asg2).autoScalingGroupName();

		resultGroups.add(asg1);
		resultGroups.add(asg2);

		Mockito.doReturn(resultGroups).when(result).autoScalingGroups();

		AWSGroupResourcesMapper mapper1 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg1).when(mapper1).getAutoScalingGroup();

		AWSGroupResourcesMapper mapper2 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg2).when(mapper2).getAutoScalingGroup();

		Mockito.doReturn(mapper1).doReturn(mapper2).when(awsResourceAllocator).initMapper(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = (DescribeAutoScalingGroupsRequest) invocation.getArguments()[0];

				if (groups.size() - describeAutoScalingGroupsRequest.autoScalingGroupNames().size() != 0)
					throw new Exception("Found invalid group size " + describeAutoScalingGroupsRequest.autoScalingGroupNames().size());

				return result;
			}

		}).when(client).describeAutoScalingGroups((DescribeAutoScalingGroupsRequest) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return false;
			}
		}).when(mapper1).meetRequirements(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return true;
			}
		}).when(mapper2).meetRequirements(Mockito.any());

		Optional<AutoScalingGroup> requiredGroup = awsResourceAllocator.findRequiredGroup(resources);

		assertTrue(requiredGroup.isPresent());

		Mockito.verify(mapper1, Mockito.times(1)).meetRequirements(Mockito.any());
		Mockito.verify(mapper2, Mockito.times(1)).meetRequirements(Mockito.any());

		assertEquals(asg2Name, requiredGroup.get().autoScalingGroupName());

	}

	@Test
	public void testFindRequiredGroupBothMatchFirstBetter() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		List<String> groups = new ArrayList<>();

		String g1 = "g1";
		String g2 = "g2";

		groups.add(g1);
		groups.add(g2);

		awsResourceAllocator.setAutoScalingGroups(groups);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribeAutoScalingGroupsResponse result = Mockito.mock(DescribeAutoScalingGroupsResponse.class);

		List<AutoScalingGroup> resultGroups = new ArrayList<>();

		AutoScalingGroup asg1 = Mockito.mock(AutoScalingGroup.class);
		String asg1Name = "asg1Name";
		Mockito.doReturn(asg1Name).when(asg1).autoScalingGroupName();

		AutoScalingGroup asg2 = Mockito.mock(AutoScalingGroup.class);
		String asg2Name = "asg2Name";
		Mockito.doReturn(asg2Name).when(asg2).autoScalingGroupName();

		resultGroups.add(asg1);
		resultGroups.add(asg2);

		Mockito.doReturn(resultGroups).when(result).autoScalingGroups();

		AWSGroupResourcesMapper mapper1 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg1).when(mapper1).getAutoScalingGroup();

		Mockito.doReturn(7000).when(mapper1).getInstanceTypeSorting();

		AWSGroupResourcesMapper mapper2 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg2).when(mapper2).getAutoScalingGroup();
		Mockito.doReturn(14000).when(mapper2).getInstanceTypeSorting();

		Mockito.doReturn(mapper1).doReturn(mapper2).when(awsResourceAllocator).initMapper(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = (DescribeAutoScalingGroupsRequest) invocation.getArguments()[0];

				if (groups.size() - describeAutoScalingGroupsRequest.autoScalingGroupNames().size() != 0)
					throw new Exception("Found invalid group size " + describeAutoScalingGroupsRequest.autoScalingGroupNames().size());

				return result;
			}

		}).when(client).describeAutoScalingGroups((DescribeAutoScalingGroupsRequest) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return true;
			}
		}).when(mapper1).meetRequirements(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return true;
			}
		}).when(mapper2).meetRequirements(Mockito.any());

		Optional<AutoScalingGroup> requiredGroup = awsResourceAllocator.findRequiredGroup(resources);

		assertTrue(requiredGroup.isPresent());

		Mockito.verify(mapper1, Mockito.times(1)).meetRequirements(Mockito.any());
		Mockito.verify(mapper2, Mockito.times(1)).meetRequirements(Mockito.any());

		assertEquals(asg1Name, requiredGroup.get().autoScalingGroupName());

	}

	@Test
	public void testFindRequiredGroupBothMatchSecondBetter() {

		AWSResourceAllocator awsResourceAllocator = Mockito.spy(new AWSResourceAllocator());

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String memRequested = "100";
		String cpuRequested = "10";

		Mockito.doReturn(memRequested).when(resources).getMemory_mb();
		Mockito.doReturn(cpuRequested).when(resources).getCpu_units();

		List<String> groups = new ArrayList<>();

		String g1 = "g1";
		String g2 = "g2";

		groups.add(g1);
		groups.add(g2);

		awsResourceAllocator.setAutoScalingGroups(groups);

		AutoScalingClient client = Mockito.mock(AutoScalingClient.class);
		awsResourceAllocator.setClient(client);

		DescribeAutoScalingGroupsResponse result = Mockito.mock(DescribeAutoScalingGroupsResponse.class);

		List<AutoScalingGroup> resultGroups = new ArrayList<>();

		AutoScalingGroup asg1 = Mockito.mock(AutoScalingGroup.class);
		String asg1Name = "asg1Name";
		Mockito.doReturn(asg1Name).when(asg1).autoScalingGroupName();

		AutoScalingGroup asg2 = Mockito.mock(AutoScalingGroup.class);
		String asg2Name = "asg2Name";
		Mockito.doReturn(asg2Name).when(asg2).autoScalingGroupName();

		resultGroups.add(asg1);
		resultGroups.add(asg2);

		Mockito.doReturn(resultGroups).when(result).autoScalingGroups();

		AWSGroupResourcesMapper mapper1 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg1).when(mapper1).getAutoScalingGroup();

		Mockito.doReturn(7000).when(mapper1).getInstanceTypeSorting();

		AWSGroupResourcesMapper mapper2 = Mockito.mock(AWSGroupResourcesMapper.class);
		Mockito.doReturn(asg2).when(mapper2).getAutoScalingGroup();
		Mockito.doReturn(4000).when(mapper2).getInstanceTypeSorting();

		Mockito.doReturn(mapper1).doReturn(mapper2).when(awsResourceAllocator).initMapper(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = (DescribeAutoScalingGroupsRequest) invocation.getArguments()[0];

				if (groups.size() - describeAutoScalingGroupsRequest.autoScalingGroupNames().size() != 0)
					throw new Exception("Found invalid group size " + describeAutoScalingGroupsRequest.autoScalingGroupNames().size());

				return result;
			}

		}).when(client).describeAutoScalingGroups((DescribeAutoScalingGroupsRequest) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return true;
			}
		}).when(mapper1).meetRequirements(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				VLabDockerResources res = (VLabDockerResources) invocation.getArguments()[0];

				if (!memRequested.equalsIgnoreCase(res.getMemory_mb()))
					throw new Exception("Found invalid memory request " + res.getMemory_mb());

				if (!cpuRequested.equalsIgnoreCase(res.getCpu_units()))
					throw new Exception("Found invalid cpu request " + res.getCpu_units());

				return true;
			}
		}).when(mapper2).meetRequirements(Mockito.any());

		Optional<AutoScalingGroup> requiredGroup = awsResourceAllocator.findRequiredGroup(resources);

		assertTrue(requiredGroup.isPresent());

		Mockito.verify(mapper1, Mockito.times(1)).meetRequirements(Mockito.any());
		Mockito.verify(mapper2, Mockito.times(1)).meetRequirements(Mockito.any());

		assertEquals(asg2Name, requiredGroup.get().autoScalingGroupName());

	}

}