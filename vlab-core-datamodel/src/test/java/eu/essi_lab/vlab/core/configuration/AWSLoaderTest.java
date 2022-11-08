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
public class AWSLoaderTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	private File confFile;
	private String tempbucket;
	private String accessKey;
	private String secretKey;
	private String cluster;
	private String obucket;
	private String asg;

	private Logger logger = LogManager.getLogger(AWSLoaderTest.class);

	@Before
	public void write() throws IOException {

		accessKey = System.getenv("eu.essi_lab.vlab.aws.accessKey");
		secretKey = System.getenv("eu.essi_lab.vlab.aws.secretKey");
		cluster = System.getenv("eu.essi_lab.vlab.aws.ecs.cluster.name");
		obucket = System.getenv("eu.essi_lab.vlab.storage.output.folder");
		tempbucket = System.getenv("eu.essi_lab.vlab.aws.s3.temporary.bucket");
		asg = System.getenv("eu.essi_lab.vlab.aws.asg.groups");

		URL url = this.getClass().getClassLoader().getResource(".");

		logger.info("URL: {}", url);

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("conftest/vlab.properties");

		String path = url.getPath();

		logger.info("Path: {}", path);

		confFile = new File(path + "vlab.properties");

		logger.info("File: {}", confFile);

		String absPath = confFile.getAbsolutePath();

		logger.info("Creating: {}", absPath);

		boolean created = confFile.createNewFile();
		logger.info("Created: {}", created);
		FileOutputStream fos = new FileOutputStream(confFile);
		stream.transferTo(fos);
		fos.flush();
		fos.close();

	}

	@After
	public void delete() throws IOException {

		environmentVariables.set("eu.essi_lab.vlab.aws.ecs.cluster.name", cluster);
		environmentVariables.set("eu.essi_lab.vlab.storage.output.folder", obucket);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.token", tempbucket);

		confFile.delete();

	}

	@Test
	public void loadFromFile() {

		environmentVariables.set("eu.essi_lab.vlab.aws.ecs.cluster.name", null);
		environmentVariables.set("eu.essi_lab.vlab.storage.output.folder", null);
		environmentVariables.set("eu.essi_lab.vlab.kubernetes.token", null);

		ConfigurationLoader.setConfFilePath(confFile.getAbsolutePath());

		assertTrue(ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_ECS_CLUSTER_NAME.getParameter())
				.equalsIgnoreCase("testcluster"));

		assertTrue(ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_OUTPUT_BUCKET_NAME.getParameter())
				.equalsIgnoreCase("tests3bucktoutput"));

		assertTrue(ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.KUBERNETES_AUTH_TOKEN.getParameter())
				.equalsIgnoreCase("testtoken"));

	}
}