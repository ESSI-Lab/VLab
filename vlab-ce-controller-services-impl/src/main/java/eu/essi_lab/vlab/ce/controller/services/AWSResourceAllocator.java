package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.ce.controller.services.ecs.AWSGroupResourcesMapper;
import eu.essi_lab.vlab.controller.services.IResourceAllocator;
import eu.essi_lab.vlab.core.configuration.BPECSComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.ResourceRequestResponse;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.autoscaling.model.DescribePoliciesRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribePoliciesResponse;
import software.amazon.awssdk.services.autoscaling.model.ExecutePolicyRequest;
import software.amazon.awssdk.services.autoscaling.model.ExecutePolicyResponse;
import software.amazon.awssdk.services.autoscaling.model.ScalingPolicy;
import software.amazon.awssdk.services.ec2.Ec2Client;

/**
 * @author Mattia Santoro
 */
public class AWSResourceAllocator implements IResourceAllocator {

	private Logger logger = LogManager.getLogger(AWSResourceAllocator.class);
	private List<String> autoScalingGroups;
	private AutoScalingClient client;
	private BPECSComputeInfrastructure computeInfrastructure;
	private Ec2Client ec2Client;

	@Override
	public ResourceRequestResponse request(VLabDockerResources resources) {

		ResourceRequestResponse response = new ResourceRequestResponse();

		response.setRequestSent(false);

		logger.info("Start requesting increase");

		Optional<AutoScalingGroup> optionalAutoScalingGroup = findRequiredGroup(resources);

		if (!optionalAutoScalingGroup.isPresent()) {
			logger.info("No available group meets the requirements: Memory {} MB and CPU {} units", resources.getMemory_mb(),
					resources.getCpu_units());

			return response;
		}

		AutoScalingGroup group = optionalAutoScalingGroup.get();

		response.setGroupName(group.autoScalingGroupName());

		logger.debug("Autoscaling Group with required resources is {}", group.autoScalingGroupName());

		Optional<ScalingPolicy> optPolicy = findIncreasePolicy(group);

		if (!optPolicy.isPresent()) {

			logger.info("No available policy for increasing instances in group {}", group.autoScalingGroupName());

			return response;

		}

		ScalingPolicy policy = optPolicy.get();

		response.setUsableScalingPolicy(policy.policyName());

		execPolicy(group, policy);

		response.setRequestSent(true);

		logger.info("Completed requesting increase");

		return response;
	}

	@Override
	public Boolean supports(BPComputeInfrastructure infra) {
		return "ecs".equalsIgnoreCase(infra.getType());
	}

	@Override
	public void setInfra(BPComputeInfrastructure infra) {
		if (infra instanceof BPECSComputeInfrastructure ecsinfra) {
			computeInfrastructure = ecsinfra;

			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(computeInfrastructure.getAsgAccessKey(),
					computeInfrastructure.getAsgSecretKey());

			setClient(AutoScalingClient.builder().region(Region.of(computeInfrastructure.getExecuteECSRegion()))
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build());

			setEc2Client(Ec2Client.builder().region(Region.of(computeInfrastructure.getExecuteECSRegion()))
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build());

			setAutoScalingGroups(computeInfrastructure.getModelExecutionAutoScalingGroups());

		} else
			logger.error("Can't assign compute infrastructure {} to this allocator", infra.getClass().getSimpleName());
	}

	private void setEc2Client(Ec2Client c) {
		ec2Client = c;

	}

	public void execPolicy(AutoScalingGroup group, ScalingPolicy policy) {

		ExecutePolicyResponse response = client.executePolicy(ExecutePolicyRequest.builder().autoScalingGroupName(
				group.autoScalingGroupName()).policyName(policy.policyName()).build());

		logger.info("Result of executing policy {} on group {} is: {}", policy.policyName(), group.autoScalingGroupName(), response);

	}

	public Optional<ScalingPolicy> findIncreasePolicy(AutoScalingGroup group) {

		logger.trace("Looking for increase policy");

		DescribePoliciesResponse policyResponse = client.describePolicies(
				DescribePoliciesRequest.builder().autoScalingGroupName(group.autoScalingGroupName())

						.build());

		List<ScalingPolicy> policies = policyResponse.scalingPolicies();

		if (policies != null && !policies.isEmpty()) {

			for (ScalingPolicy policy : policies) {
				if (policy.scalingAdjustment() != null && policy.scalingAdjustment() > 0) {

					logger.trace("Found increase policy: {}", policy.policyName());

					return Optional.of(policy);
				}
			}
		} else {
			logger.trace("Group {} has no scaling policies", group.autoScalingGroupName());
		}

		return Optional.empty();
	}

	AWSGroupResourcesMapper initMapper(AutoScalingGroup group) {
		return new AWSGroupResourcesMapper(group, client, ec2Client);
	}

	public Optional<AutoScalingGroup> findRequiredGroup(VLabDockerResources resources) {

		if (autoScalingGroups.isEmpty()) {

			logger.warn("No autoscaling group configured");

			return Optional.empty();
		}

		AWSGroupResourcesMapper myMapper = null;

		DescribeAutoScalingGroupsRequest request = DescribeAutoScalingGroupsRequest.builder().autoScalingGroupNames(autoScalingGroups)
				.build();

		logger.debug("Requesting description of {}", autoScalingGroups);

		DescribeAutoScalingGroupsResponse response = client.describeAutoScalingGroups(request);

		List<AutoScalingGroup> groups = response.autoScalingGroups();

		if (groups == null)
			return Optional.empty();

		for (AutoScalingGroup group : groups) {

			logger.trace("Investingating group {}", group.autoScalingGroupName());
			AWSGroupResourcesMapper mapper = initMapper(group);

			if (mapper.meetRequirements(resources)) {

				logger.trace("Group {} matches requirements", group.autoScalingGroupName());

				if (myMapper == null || myMapper.getInstanceTypeSorting() > mapper.getInstanceTypeSorting()) {
					logger.trace("Updating group to {}", group.autoScalingGroupName());

					myMapper = mapper;
				} else {

					logger.trace("Current group {} requires less than {}", myMapper.getAutoScalingGroup().autoScalingGroupName(),
							group.autoScalingGroupName());

				}
			} else {

				logger.trace("Requirements are not met");

			}

		}

		if (myMapper == null) {

			logger.debug("No group met requirements");

			return Optional.empty();
		}

		return Optional.ofNullable(myMapper.getAutoScalingGroup());
	}

	public List<String> getAutoScalingGroups() {
		return autoScalingGroups;
	}

	public void setAutoScalingGroups(List<String> autoScalingGroups) {
		this.autoScalingGroups = autoScalingGroups;
	}

	public void setClient(AutoScalingClient client) {
		this.client = client;
	}

}
