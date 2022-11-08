package eu.essi_lab.vlab.ce.controller.services.utils;

import eu.essi_lab.vlab.core.configuration.BPECSComputeInfrastructure;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPKubernetesComputeInfrastructure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Mattia Santoro
 */
public class ComputeConfLoader {

	static final String KUBE_COMPUTE_INFRA = "kubernetes";

	private ComputeConfLoader() {
	}

	/**
	 * Returns the {@link BPComputeInfrastructure} from configuration.
	 *
	 * @return
	 */
	public static BPComputeInfrastructure getBPComputeInfrastructure() {

		BPComputeInfrastructure infrastructure = null;

		String infraType = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_TYPE.getParameter());

		if (infraType != null && !"".equals(infraType) && infraType.contains(KUBE_COMPUTE_INFRA)) {

			BPKubernetesComputeInfrastructure kubei = new BPKubernetesComputeInfrastructure();

			kubei.setServerUrl(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.KUBERNETES_API_SERVER_URL.getParameter()));
			kubei.setToken(
					ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.KUBERNETES_AUTH_TOKEN.getParameter()));
			kubei.setControllerNodeSelector(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.KUBERNETES_CONTROLLER_SELECTOR.getParameter()));
			kubei.setExecutorNodeSelector(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.KUBERNETES_EXECUTOR_SELECTOR.getParameter()));

			kubei.setControllerNodeTolerations(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.KUBERNETES_CONTROLLER_TOLERATIONS.getParameter()));
			kubei.setExecutorNodeTolerations(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.KUBERNETES_EXECUTOR_TOLERATIONS.getParameter()));

			kubei.setVlabPv(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.KUBERNETES_PERSISTENT_VOLUME.getParameter()));
			kubei.setVlabPvClaim(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.KUBERNETES_PERSISTENT_VOLUME_CLAIM.getParameter()));

			infrastructure = kubei;

		} else {

			BPECSComputeInfrastructure ecsi = new BPECSComputeInfrastructure();

			ecsi.setCoreCluster(
					ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_ECS_CLUSTER_NAME.getParameter()));

			ecsi.setDeployECSAccessKey(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_DEPLOY_ECS_ACCESS_KEY.getParameter()));
			ecsi.setDeployECSSecretKey(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_DEPLOY_ECS_SECRET_KEY.getParameter()));
			ecsi.setDeployECSRegion(
					ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_DEPLOY_ECS_REGION.getParameter()));

			ecsi.setDeployLogPrefix(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_DEPLOY_ECS_LOG_PREFIX.getParameter()));

			ecsi.setDeployLogGroup(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_DEPLOY_ECS_LOG_GROUP.getParameter()));

			ecsi.setModelCluster(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_EXECUTE_MODEL_CLUSTER_NAME.getParameter()));

			ecsi.setExecuteECSRegion(
					ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_EXECUTE_ECS_REGION.getParameter()));

			ecsi.setExecuteECSAccessKey(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_EXECUTE_ECS_ACCESS_KEY.getParameter()));

			ecsi.setExecuteECSSecretKey(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_EXECUTE_ECS_SECRET_KEY.getParameter()));

			List<String> groups = new ArrayList<>();
			String g = ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_AUTO_SCALING_GROUPS.getParameter());

			if (null != g && !"".equalsIgnoreCase(g))
				groups = Arrays.asList(g.split(","));

			ecsi.setModelExecutionAutoScalingGroups(groups);

			ecsi.setAsgAccessKey(
					ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_ASG_ACCESS_KEY.getParameter()));
			ecsi.setAsgSecretKey(
					ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_ASG_SECRET_KEY.getParameter()));

			ecsi.setModelExecutionLogGroup(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_GROUP.getParameter()));

			ecsi.setModelExecutionLogPrefix(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_PREFIX.getParameter()));

			ecsi.setModelTaskContainerName(ConfigurationLoader.loadConfigurationParameter(
					BPStaticConfigurationParameters.AWS_ECS_TASK_CONTAINER_NAME.getParameter()));

			ecsi.setEfsFolder(
					ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_ECS_EFS_FOLDER.getParameter()));

			if (null == infraType || "".equalsIgnoreCase(infraType))
				infraType = "ecs";

			infrastructure = ecsi;
		}

		String label = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_LABEL.getParameter());

		infrastructure.setType(infraType);
		infrastructure.setLabel(label);

		infrastructure.setS3AccessKey(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter()));
		infrastructure.setS3SecretKey(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter()));
		infrastructure.setS3BucketRegion(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter()));
		infrastructure.setS3ServiceUrl(Optional.ofNullable(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter())));

		infrastructure.setAwsCliImage(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_CLI_IMAGE.getParameter()));
		infrastructure.setPrepareImage(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.PREPEARE_IMAGE.getParameter()));

		infrastructure.setId(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_ID.getParameter()));
		return infrastructure;
	}

}
