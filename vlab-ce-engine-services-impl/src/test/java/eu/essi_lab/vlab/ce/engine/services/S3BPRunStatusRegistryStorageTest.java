package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class S3BPRunStatusRegistryStorageTest {

	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();

	private String testaccessKey = "testaccessKey";
	private String testsecretKey = "testsecretKey";
	private String testbprunbucket = "testbprunbucket";
	private String testbucketregion = "testregion";

	@Test
	public void testgetBPRunStatusRegistryStorage() throws BPException {

		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter().getKey(), testaccessKey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter().getKey(), testsecretKey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_BP_RUN_BUCKET_NAME.getParameter().getKey(), testbprunbucket);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter().getKey(), testbucketregion);

		S3BPRunStatusRegistryStorage storage = Mockito.spy(new S3BPRunStatusRegistryStorage());
		S3BucketClient client = Mockito.mock(S3BucketClient.class);
		storage.setS3BucketClient(client);
		Mockito.doNothing().when(storage).setS3BucketClient(Mockito.any());

		Map<ConfigurationParameter, String> map = Mockito.spy(new HashMap<>());

		storage.configurationParameters().forEach(configurationParameter -> map.put(configurationParameter,
				ConfigurationLoader.loadConfigurationParameter(configurationParameter)));

		assertEquals(4, map.size());
		storage.configure(map);

		assertNotNull(storage);

		Mockito.verify(client, Mockito.times(1)).bucketExists(Mockito.booleanThat(new ArgumentMatcher<Boolean>() {
			@Override
			public boolean matches(Boolean aBoolean) {
				return Boolean.FALSE.equals(aBoolean);
			}
		}));

		Mockito.verify(map, Mockito.times(4)).get(Mockito.any());

	}

	@Test
	public void test() throws BPException {
		S3BucketClient client = Mockito.mock(S3BucketClient.class);

		S3BPRunStatusRegistryStorage storage = new S3BPRunStatusRegistryStorage();
		storage.setS3BucketClient(client);

		BPRunStatus status = new BPRunStatus();

		String statusid = "statusid";
		status.setRunid(statusid);

		WebStorageObject object = Mockito.mock(WebStorageObject.class);

		Mockito.doReturn("BPStatusDir/BPStatus_" + statusid).when(object).getKey();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String k = (String) invocation.getArguments()[1];

				if (!("BPStatusDir/BPStatus_" + statusid).equalsIgnoreCase(k))
					throw new Exception("Bad id " + k);

				return object;
			}
		}).when(client).upload((InputStream) Mockito.any(), Mockito.anyString());

		assertTrue(storage.store(status));
	}

	@Test
	public void test2() throws BPException {
		S3BucketClient client = Mockito.mock(S3BucketClient.class);

		S3BPRunStatusRegistryStorage storage = new S3BPRunStatusRegistryStorage();
		storage.setS3BucketClient(client);

		BPRunStatus status = new BPRunStatus();

		String statusid = "statusid";
		status.setRunid(statusid);

		Mockito.doThrow(BPException.class).when(client).upload((InputStream) Mockito.any(), Mockito.anyString());

		assertFalse(storage.store(status));
	}

	@Test
	public void test3() throws BPException {
		S3BucketClient client = Mockito.mock(S3BucketClient.class);

		S3BPRunStatusRegistryStorage storage = new S3BPRunStatusRegistryStorage();
		storage.setS3BucketClient(client);

		BPRunStatus status = new BPRunStatus();

		String statusid = "statusid";
		status.setRunid(statusid);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String k = (String) invocation.getArguments()[0];

				if (!("BPStatusDir/BPStatus_" + statusid).equalsIgnoreCase(k))
					throw new Exception("Bad id " + k);

				return new ByteArrayInputStream(new JSONSerializer().serialize(status).getBytes());
			}
		}).when(client).read(Mockito.anyString());

		BPRunStatus s = storage.get(statusid);

		assertEquals(statusid, s.getRunid());
	}

	@Test
	public void test4() throws BPException {
		S3BucketClient client = Mockito.mock(S3BucketClient.class);

		S3BPRunStatusRegistryStorage storage = new S3BPRunStatusRegistryStorage();
		storage.setS3BucketClient(client);

		String statusid = "statusid";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String k = (String) invocation.getArguments()[0];

				if (!("BPStatusDir/BPStatus_" + statusid).equalsIgnoreCase(k))
					throw new Exception("Bad id " + k);

				return true;
			}
		}).when(client).remove(Mockito.anyString());

		assertTrue(storage.remove(statusid));

	}

}