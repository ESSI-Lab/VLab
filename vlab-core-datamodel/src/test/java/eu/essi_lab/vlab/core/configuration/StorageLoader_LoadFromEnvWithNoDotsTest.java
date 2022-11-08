package eu.essi_lab.vlab.core.configuration;

import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

/**
 * @author Mattia Santoro
 */
public class StorageLoader_LoadFromEnvWithNoDotsTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	private String conf = "eu.essi_lab.vlab.storage.es.url";

	private String confNoDots = "euessi_labvlabstorageesurl";

	private String existing;
	private String existingNoDots;
	private String expected = "http://example.com";

	@Before
	public void write() throws IOException {

		existing = System.getenv(conf);

		existingNoDots = System.getenv(confNoDots);

		environmentVariables.set(conf, null);
		environmentVariables.set(confNoDots, expected);

	}

	@After
	public void resetEnvVars() {
		environmentVariables.set(conf, existing);
		environmentVariables.set(confNoDots, existingNoDots);
	}

	@Test
	public void loadFromEnvWithNoDots() {

		assertEquals(expected, ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.STORAGE_ESURL.getParameter()));

	}

}