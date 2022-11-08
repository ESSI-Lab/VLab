package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsResponse;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * @author Mattia Santoro
 */
public class AWSGroupResourcesMapper {

	private final AutoScalingClient asgClient;
	private final Ec2Client ec2Client;
	private final AutoScalingGroup group;
	private InstanceType instanceType;
	private Logger logger = LogManager.getLogger(AWSGroupResourcesMapper.class);

	public AWSGroupResourcesMapper(AutoScalingGroup awsgroup, AutoScalingClient asg, Ec2Client ec2) {

		this.group = awsgroup;

		asgClient = asg;
		ec2Client = ec2;

		LaunchConfiguration conf = findLaunchConfiguration(group.launchConfigurationName());

		logger.trace("Launch configuration of group {} is {}", group.autoScalingGroupName(), conf.launchConfigurationName());

		instanceType = InstanceType.fromValue(conf.instanceType());

		logger.trace("Instance type of {} is {}", conf.launchConfigurationName(), instanceType);

	}

	public AutoScalingGroup getAutoScalingGroup() {
		return group;
	}

	public boolean meetRequirements(VLabDockerResources resources) {

		if (resources.getMemory_mb() != null && !resources.getMemory_mb().equalsIgnoreCase("")) {

			Integer mb = Integer.valueOf(resources.getMemory_mb());

			Integer mem = memory(instanceType);

			logger.debug("Checking requirement: Requested {} MB, available {} MB", mb, mem);

			return mb < mem;

		}

		return true;

	}

	private Integer memory(InstanceType type) {

		Long memSize = ec2Client.describeInstanceTypes(DescribeInstanceTypesRequest.builder().instanceTypes(type).build()).instanceTypes()
				.get(0).memoryInfo().sizeInMiB();

		return memSize.intValue();

	}

	private LaunchConfiguration findLaunchConfiguration(String launchConfigurationName) {

		DescribeLaunchConfigurationsResponse response = asgClient.describeLaunchConfigurations(
				DescribeLaunchConfigurationsRequest.builder().launchConfigurationNames(Arrays.asList(launchConfigurationName)).build());

		return response.launchConfigurations().get(0);

	}

	public Integer getInstanceTypeSorting() {
		return memory(instanceType);
	}
}
