package eu.essi_lab.vlab.ce.controller.services.utils;

import eu.essi_lab.vlab.core.configuration.BPECSComputeInfrastructure;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPKubernetesComputeInfrastructure;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

/**
 * @author Mattia Santoro
 */
public class ComputeConfLoaderTest {

	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();


	private String testInfraType = "kubernetes";
	private String originalTestType;

	@Test
	public void test() {

		Assert.assertEquals("kubernetes", ComputeConfLoader.KUBE_COMPUTE_INFRA);

	}

	@Before
	public void before() {

		originalTestType = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_TYPE.getParameter());

		environmentVariables.set(BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_TYPE.getParameter().getKey(), testInfraType);

	}

	@After
	public void after() {

		environmentVariables.set(BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_TYPE.getParameter().getKey(), originalTestType);
	}

	@Test
	public void test2() {

		BPComputeInfrastructure infra = ComputeConfLoader.getBPComputeInfrastructure();

		Assert.assertTrue(infra instanceof BPKubernetesComputeInfrastructure);

	}

	@Test
	public void test3() {

		environmentVariables.set(BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_TYPE.getParameter().getKey(), "aws");

		BPComputeInfrastructure infra = ComputeConfLoader.getBPComputeInfrastructure();

		Assert.assertTrue(infra instanceof BPECSComputeInfrastructure);

	}
}