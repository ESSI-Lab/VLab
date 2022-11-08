package eu.essi_lab.vlab.core.configuration;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

/**
 * @author Mattia Santoro
 */
public class ConfigurationLoaderTest {

	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Test
	public void test() {

		String original = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter());

		String testsecret = "testsecret";
		environmentVariables.set("euessi_labvlabawss3secretKey", testsecret);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter().getKey(), null);

		Assert.assertEquals(testsecret,
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter()));

		environmentVariables.set("euessi_labvlabawss3secretKey", null);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter().getKey(), original);

	}

	@Test
	public void test2() {

		String original = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter());

		String testsecret = "testsecret";
		String testsecret2 = "testsecret2";
		environmentVariables.set("euessi_labvlabawss3secretKey", testsecret);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter().getKey(), testsecret2);

		Assert.assertEquals(testsecret2,
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter()));

		environmentVariables.set("euessi_labvlabawss3secretKey", null);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter().getKey(), original);

	}

}