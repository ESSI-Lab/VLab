package eu.essi_lab.vlab.core.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

/**
 * @author Mattia Santoro
 */
public class KubernetesConfLoaderTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	private File confFile;

	private Logger logger = LogManager.getLogger(AWSLoaderTest.class);
	private String token;
	private String serverurl;
	private String kubecon;
	private String kubeex;

	@Before
	public void write() throws IOException {

		token = System.getenv("eu.essi_lab.vlab.kubernetes.token");
		serverurl = System.getenv("eu.essi_lab.vlab.aws.serverurl");
		kubecon = System.getenv("eu.essi_lab.vlab.aws.controller");
		kubeex = System.getenv("eu.essi_lab.vlab.aws.executor");

		URL url = this.getClass().getClassLoader().getResource(".");

		logger.info("URL: {}", url);

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("conftest/vlab.properties");

		String path = url.getPath();

		logger.info("Path: {}", path);

		confFile = new File(path + "vlab.properties");

		logger.info("File: {}", confFile);

		String absPath = confFile.getAbsolutePath();

		logger.info("Creating: {}", absPath);

		confFile.createNewFile();
		stream.transferTo(new FileOutputStream(confFile));


	}

	@After
	public void delete() throws IOException {

		environmentVariables.set("eu.essi_lab.vlab.kubernetes.token", token);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.serverurl", serverurl);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.executor", kubeex);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.controller", kubecon);

		confFile.delete();

	}

	@Test
	public void loadFromFile() {

		environmentVariables.set("eu.essi_lab.vlab.kubernetes.token", null);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.serverurl", null);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.executor", null);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.controller", null);

		ConfigurationLoader.setConfFilePath(confFile.getAbsolutePath());

		assertTrue(ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.KUBERNETES_AUTH_TOKEN.getParameter())
				.equalsIgnoreCase("testtoken"));

		assertTrue(ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.KUBERNETES_API_SERVER_URL.getParameter())
				.equalsIgnoreCase("yesyurl"));

		assertTrue(ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.KUBERNETES_CONTROLLER_SELECTOR.getParameter()).equalsIgnoreCase("yesycon"));

		assertTrue(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.KUBERNETES_EXECUTOR_SELECTOR.getParameter())
						.equalsIgnoreCase("yesex"));

	}
}