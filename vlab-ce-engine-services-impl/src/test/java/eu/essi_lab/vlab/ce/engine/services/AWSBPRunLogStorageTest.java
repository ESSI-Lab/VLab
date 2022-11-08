package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLog;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DataAlreadyAcceptedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.RejectedLogEventsInfo;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;

/**
 * @author Mattia Santoro
 */
public class AWSBPRunLogStorageTest {

	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private String testLogAccesskey = "testLogAccesskey";
	private String testLogSecretkey = "testLogSecretkey";
	private String testRegion = "us-east-1";
	private String testModelGroup = "tesrgroup";
	private String testPrefix = "testPrefix";
	private String testCName = "testCName";

	private String originalModelGroup;
	private String originalPrefix;
	private String originalTaskCName;
	private String originalRegion;
	private String originalLogSecretkey;
	private String originalLogAccesskey;

	@Before
	public void before() {

		originalModelGroup = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_GROUP.getParameter());
		originalLogAccesskey = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_LOG_ACCESS_KEY.getParameter());
		originalLogSecretkey = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_LOG_SECRET_KEY.getParameter());
		originalRegion = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_LOG_REGION.getParameter());
		originalTaskCName = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_ECS_TASK_CONTAINER_NAME.getParameter());
		originalPrefix = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_PREFIX.getParameter());

		environmentVariables.set(BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_GROUP.getParameter().getKey(), testModelGroup);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_LOG_ACCESS_KEY.getParameter().getKey(), testLogAccesskey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_LOG_SECRET_KEY.getParameter().getKey(), testLogSecretkey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_LOG_REGION.getParameter().getKey(), testRegion);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_PREFIX.getParameter().getKey(), testPrefix);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_ECS_TASK_CONTAINER_NAME.getParameter().getKey(), testCName);

	}

	@After
	public void after() {

		environmentVariables.set(BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_GROUP.getParameter().getKey(), originalModelGroup);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_LOG_ACCESS_KEY.getParameter().getKey(), originalLogAccesskey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_LOG_SECRET_KEY.getParameter().getKey(), originalLogSecretkey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_LOG_REGION.getParameter().getKey(), originalRegion);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_PREFIX.getParameter().getKey(), originalPrefix);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_ECS_TASK_CONTAINER_NAME.getParameter().getKey(), originalTaskCName);
	}

	private AWSBPRunLogStorage initAWSBPRunLogStorage() throws BPException {
		AWSBPRunLogStorage storage = Mockito.spy(new AWSBPRunLogStorage());

		Map<ConfigurationParameter, String> map = Mockito.spy(new HashMap<>());

		storage.configurationParameters().forEach(configurationParameter -> map.put(configurationParameter,
				ConfigurationLoader.loadConfigurationParameter(configurationParameter)));

		storage.configure(map);

		return storage;
	}

	@Test
	public void test() throws BPException {

		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "taskid";

		Assert.assertEquals(testPrefix + "/" + testCName + "/" + teskid, storage.getLogStreamName(teskid));
	}

	@Test
	public void test2() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "taskid";

		GetLogEventsResponse awsresponse = Mockito.mock(GetLogEventsResponse.class);

		List<OutputLogEvent> emptyEvents = new ArrayList<>();

		Mockito.doReturn(emptyEvents).when(awsresponse).events();

		String respNextForwardToken = "respNextForwardToken";
		String respNextBackwardToken = "respNextBackwardToken";
		Mockito.doReturn(respNextForwardToken).when(awsresponse).nextForwardToken();
		Mockito.doReturn(respNextBackwardToken).when(awsresponse).nextBackwardToken();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GetLogEventsRequest request = (GetLogEventsRequest) invocationOnMock.getArguments()[0];

				Integer limit = request.limit();

				if (100 - limit != 0)
					throw new Exception("Bad limit");

				String nexttoken = request.nextToken();

				if (nexttoken != null)
					throw new Exception("Bad next toen");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + teskid).equals(stream))
					throw new Exception("Bad stream name");

				if (request.startFromHead())
					throw new Exception("Bad head");

				return awsresponse;
			}
		}).when(storage).doGetLogEvents(Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, null);

		Assert.assertEquals(respNextForwardToken, response.getNextForwardToken());
		Assert.assertEquals(respNextBackwardToken, response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
	}

	@Test
	public void test_ex() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();

		String teskid = "taskid";

		Mockito.doThrow(ServiceUnavailableException.class).when(client).getLogEvents((GetLogEventsRequest) Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, null);

		Assert.assertNull(response.getNextForwardToken());
		Assert.assertNull(response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
		Mockito.verify(client, Mockito.times(1)).getLogEvents((GetLogEventsRequest) Mockito.any());
	}

	@Test
	public void test_ex_2() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();

		String teskid = "taskid";

		Mockito.doThrow(ResourceNotFoundException.class).when(client).getLogEvents((GetLogEventsRequest) Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, null);

		Assert.assertNull(response.getNextForwardToken());
		Assert.assertNull(response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
		Mockito.verify(client, Mockito.times(1)).getLogEvents((GetLogEventsRequest) Mockito.any());
	}

	@Test
	public void test_ex_3() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();

		String teskid = "taskid";

		Mockito.doThrow(InvalidParameterException.class).when(client).getLogEvents((GetLogEventsRequest) Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, null);

		Assert.assertNull(response.getNextForwardToken());
		Assert.assertNull(response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
		Mockito.verify(client, Mockito.times(1)).getLogEvents((GetLogEventsRequest) Mockito.any());
	}

	@Test
	public void test2_1() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "cluster/taskid";

		GetLogEventsResponse awsresponse = Mockito.mock(GetLogEventsResponse.class);

		List<OutputLogEvent> emptyEvents = new ArrayList<>();

		Mockito.doReturn(emptyEvents).when(awsresponse).events();

		String respNextForwardToken = "respNextForwardToken";
		String respNextBackwardToken = "respNextBackwardToken";
		Mockito.doReturn(respNextForwardToken).when(awsresponse).nextForwardToken();
		Mockito.doReturn(respNextBackwardToken).when(awsresponse).nextBackwardToken();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GetLogEventsRequest request = (GetLogEventsRequest) invocationOnMock.getArguments()[0];

				Integer limit = request.limit();

				if (100 - limit != 0)
					throw new Exception("Bad limit");

				String nexttoken = request.nextToken();

				if (nexttoken != null)
					throw new Exception("Bad next toen");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();
				if (!(testPrefix + "/" + testCName + "/taskid").equals(stream))
					throw new Exception("Bad stream name " + stream);

				if (request.startFromHead())
					throw new Exception("Bad head");

				return awsresponse;
			}
		}).when(storage).doGetLogEvents(Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, null);

		Assert.assertEquals(respNextForwardToken, response.getNextForwardToken());
		Assert.assertEquals(respNextBackwardToken, response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
	}

	@Test
	public void test2_2() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "cluster/";

		GetLogEventsResponse awsresponse = Mockito.mock(GetLogEventsResponse.class);

		List<OutputLogEvent> emptyEvents = new ArrayList<>();

		Mockito.doReturn(emptyEvents).when(awsresponse).events();

		String respNextForwardToken = "respNextForwardToken";
		String respNextBackwardToken = "respNextBackwardToken";
		Mockito.doReturn(respNextForwardToken).when(awsresponse).nextForwardToken();
		Mockito.doReturn(respNextBackwardToken).when(awsresponse).nextBackwardToken();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GetLogEventsRequest request = (GetLogEventsRequest) invocationOnMock.getArguments()[0];

				Integer limit = request.limit();

				if (100 - limit != 0)
					throw new Exception("Bad limit");

				String nexttoken = request.nextToken();

				if (nexttoken != null)
					throw new Exception("Bad next toen");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + teskid).equals(stream))
					throw new Exception("Bad stream name");

				if (request.startFromHead())
					throw new Exception("Bad head");

				return awsresponse;
			}
		}).when(storage).doGetLogEvents(Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, null);

		Assert.assertEquals(respNextForwardToken, response.getNextForwardToken());
		Assert.assertEquals(respNextBackwardToken, response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
	}

	@Test
	public void test3() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "taskid";

		GetLogEventsResponse awsresponse = Mockito.mock(GetLogEventsResponse.class);

		List<OutputLogEvent> emptyEvents = new ArrayList<>();

		Mockito.doReturn(emptyEvents).when(awsresponse).events();

		String respNextForwardToken = "respNextForwardToken";
		String respNextBackwardToken = "respNextBackwardToken";
		Mockito.doReturn(respNextForwardToken).when(awsresponse).nextForwardToken();
		Mockito.doReturn(respNextBackwardToken).when(awsresponse).nextBackwardToken();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GetLogEventsRequest request = (GetLogEventsRequest) invocationOnMock.getArguments()[0];

				Integer limit = request.limit();

				if (100 - limit != 0)
					throw new Exception("Bad limit");

				String nexttoken = request.nextToken();

				if (nexttoken != null)
					throw new Exception("Bad next toen");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + teskid).equals(stream))
					throw new Exception("Bad stream name");

				if (request.startFromHead())
					throw new Exception("Bad head");

				return awsresponse;
			}
		}).when(storage).doGetLogEvents(Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, "");

		Assert.assertEquals(respNextForwardToken, response.getNextForwardToken());
		Assert.assertEquals(respNextBackwardToken, response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());
		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
	}

	@Test
	public void test4() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "taskid";

		GetLogEventsResponse awsresponse = Mockito.mock(GetLogEventsResponse.class);

		List<OutputLogEvent> emptyEvents = new ArrayList<>();

		Mockito.doReturn(emptyEvents).when(awsresponse).events();

		String respNextForwardToken = "respNextForwardToken";
		String respNextBackwardToken = "respNextBackwardToken";
		Mockito.doReturn(respNextForwardToken).when(awsresponse).nextForwardToken();
		Mockito.doReturn(respNextBackwardToken).when(awsresponse).nextBackwardToken();

		String next = "next";
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GetLogEventsRequest request = (GetLogEventsRequest) invocationOnMock.getArguments()[0];

				Integer limit = request.limit();

				if (100 - limit != 0)
					throw new Exception("Bad limit");

				String nexttoken = request.nextToken();

				if (!next.equals(nexttoken))
					throw new Exception("Bad next toen");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + teskid).equals(stream))
					throw new Exception("Bad stream name");

				if (request.startFromHead())
					throw new Exception("Bad head");

				return awsresponse;
			}
		}).when(storage).doGetLogEvents(Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, false, next);

		Assert.assertEquals(respNextForwardToken, response.getNextForwardToken());
		Assert.assertEquals(respNextBackwardToken, response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());
		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
	}

	@Test
	public void test5() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "taskid";

		GetLogEventsResponse awsresponse = Mockito.mock(GetLogEventsResponse.class);

		List<OutputLogEvent> emptyEvents = new ArrayList<>();

		Mockito.doReturn(emptyEvents).when(awsresponse).events();

		String respNextForwardToken = "respNextForwardToken";
		String respNextBackwardToken = "respNextBackwardToken";
		Mockito.doReturn(respNextForwardToken).when(awsresponse).nextForwardToken();
		Mockito.doReturn(respNextBackwardToken).when(awsresponse).nextBackwardToken();

		String next = "next";
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GetLogEventsRequest request = (GetLogEventsRequest) invocationOnMock.getArguments()[0];

				Integer limit = request.limit();

				if (100 - limit != 0)
					throw new Exception("Bad limit");

				String nexttoken = request.nextToken();

				if (!next.equals(nexttoken))
					throw new Exception("Bad next toen");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + teskid).equals(stream))
					throw new Exception("Bad stream name");

				if (!request.startFromHead())
					throw new Exception("Bad head");

				return awsresponse;
			}
		}).when(storage).doGetLogEvents(Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, true, next);

		Assert.assertEquals(respNextForwardToken, response.getNextForwardToken());
		Assert.assertEquals(respNextBackwardToken, response.getNextBackwardToken());
		Assert.assertEquals(1, response.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", response.getEvents().get(0).getMessage());
		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
	}

	@Test
	public void test6() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();

		String teskid = "taskid";

		GetLogEventsResponse awsresponse = Mockito.mock(GetLogEventsResponse.class);

		Long it1 = 130948L;
		Long ts1 = 130938L;
		String m1 = "message 1";

		List<OutputLogEvent> awsevents = new ArrayList<>();

		OutputLogEvent e1 = Mockito.mock(OutputLogEvent.class);
		Mockito.doReturn(it1).when(e1).ingestionTime();
		Mockito.doReturn(ts1).when(e1).timestamp();
		Mockito.doReturn(m1).when(e1).message();

		awsevents.add(e1);

		Long it2 = 120948L;
		Long ts2 = 134938L;
		String m2 = "message 2";
		OutputLogEvent e2 = Mockito.mock(OutputLogEvent.class);
		Mockito.doReturn(it2).when(e2).ingestionTime();
		Mockito.doReturn(ts2).when(e2).timestamp();
		Mockito.doReturn(m2).when(e2).message();

		awsevents.add(e2);

		Mockito.doReturn(awsevents).when(awsresponse).events();

		String respNextForwardToken = "respNextForwardToken";
		String respNextBackwardToken = "respNextBackwardToken";
		Mockito.doReturn(respNextForwardToken).when(awsresponse).nextForwardToken();
		Mockito.doReturn(respNextBackwardToken).when(awsresponse).nextBackwardToken();

		String next = "next";
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GetLogEventsRequest request = (GetLogEventsRequest) invocationOnMock.getArguments()[0];

				Integer limit = request.limit();

				if (100 - limit != 0)
					throw new Exception("Bad limit");

				String nexttoken = request.nextToken();

				if (!next.equals(nexttoken))
					throw new Exception("Bad next toen");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + teskid).equals(stream))
					throw new Exception("Bad stream name");

				if (!request.startFromHead())
					throw new Exception("Bad head");

				return awsresponse;
			}
		}).when(storage).doGetLogEvents(Mockito.any());

		LogMessagesResponse response = storage.readLog(teskid, true, next);

		Assert.assertEquals(respNextForwardToken, response.getNextForwardToken());
		Assert.assertEquals(respNextBackwardToken, response.getNextBackwardToken());
		Assert.assertEquals(2, response.getEvents().size());

		Assert.assertEquals(m1, response.getEvents().get(0).getMessage());
		Assert.assertEquals(it1, response.getEvents().get(0).getIngestionTime());
		Assert.assertEquals(ts1, response.getEvents().get(0).getTimestamp());

		Assert.assertEquals(m2, response.getEvents().get(1).getMessage());
		Assert.assertEquals(it2, response.getEvents().get(1).getIngestionTime());
		Assert.assertEquals(ts2, response.getEvents().get(1).getTimestamp());
		Mockito.verify(storage, Mockito.times(1)).doGetLogEvents(Mockito.any());
	}

	@Test
	public void test7() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);

		Long nano2 = 114L;
		Long milli2 = 1634303408000L;
		BPLog log2 = new BPLog(milli2, nano2);
		String msg2 = "Second test message";
		log2.setMessage(msg2);

		BPLogChunk chunck = new BPLogChunk();
		chunck.getSet().add(log2);
		chunck.getSet().add(log);

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				PutLogEventsRequest request = (PutLogEventsRequest) invocationOnMock.getArguments()[0];

				List<InputLogEvent> awsEvents = request.logEvents();

				if (!msg.equalsIgnoreCase(awsEvents.get(0).message()))
					throw new Exception("Bad first log message");

				if (milli + nano - awsEvents.get(0).timestamp() != 0)
					throw new Exception("Bad first log time stamp");

				if (!msg2.equalsIgnoreCase(awsEvents.get(1).message()))
					throw new Exception("Bad second log message");

				if (milli2 + nano2 - awsEvents.get(1).timestamp() != 0)
					throw new Exception("Bad second log time stamp");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + taskid).equals(stream))
					throw new Exception("Bad stream name");

				return awsresponse;
			}
		}).when(client).putLogEvents((PutLogEventsRequest) Mockito.any());

		storage.writeLog(runid, taskid, chunck);

		Mockito.verify(storage, Mockito.times(1)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(1)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test7_1() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "";
		log.setMessage(msg);

		Long nano2 = 114L;
		Long milli2 = 1634303408000L;
		BPLog log2 = new BPLog(milli2, nano2);
		String msg2 = "Second test message";
		log2.setMessage(msg2);

		BPLogChunk chunck = new BPLogChunk();
		chunck.getSet().add(log2);
		chunck.getSet().add(log);

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				PutLogEventsRequest request = (PutLogEventsRequest) invocationOnMock.getArguments()[0];

				List<InputLogEvent> awsEvents = request.logEvents();

				if (awsEvents.size() > 1)
					throw new Exception("Too many messages");

				if (!msg2.equalsIgnoreCase(awsEvents.get(0).message()))
					throw new Exception("Bad second log message");

				if (milli2 + nano2 - awsEvents.get(0).timestamp() != 0)
					throw new Exception("Bad second log time stamp");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + taskid).equals(stream))
					throw new Exception("Bad stream name");

				return awsresponse;
			}
		}).when(client).putLogEvents((PutLogEventsRequest) Mockito.any());

		storage.writeLog(runid, taskid, chunck);

		Mockito.verify(storage, Mockito.times(1)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(1)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test8() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);

		Long nano2 = 114L;
		Long milli2 = 1634303408000L;
		BPLog log2 = new BPLog(milli2, nano2);
		String msg2 = "Second test message";
		log2.setMessage(msg2);

		Long nano3 = 112L;
		Long milli3 = 1634303409000L;
		BPLog log3 = new BPLog(milli3, nano3);
		String msg3 = "Test 3 message";
		log3.setMessage(msg3);

		Long nano4 = 119L;
		Long milli4 = 1634303409000L;
		BPLog log4 = new BPLog(milli4, nano4);
		String msg4 = "Second 4 test message";
		log4.setMessage(msg4);

		BPLogChunk chunck = new BPLogChunk();
		chunck.getSet().add(log2);
		chunck.getSet().add(log);

		BPLogChunk chunck2 = new BPLogChunk();
		chunck2.getSet().add(log4);
		chunck2.getSet().add(log3);

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		String expectedToken = "sdfghjklòàùtyuiopè";

		Mockito.doReturn(expectedToken).when(awsresponse).nextSequenceToken();

		PutLogEventsResponse awsresponse2 = Mockito.mock(PutLogEventsResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				PutLogEventsRequest request = (PutLogEventsRequest) invocationOnMock.getArguments()[0];

				List<InputLogEvent> awsEvents = request.logEvents();

				if (!msg.equalsIgnoreCase(awsEvents.get(0).message()))
					throw new Exception("Bad first log message");

				if (milli + nano - awsEvents.get(0).timestamp() != 0)
					throw new Exception("Bad first log time stamp");

				if (!msg2.equalsIgnoreCase(awsEvents.get(1).message()))
					throw new Exception("Bad second log message");

				if (milli2 + nano2 - awsEvents.get(1).timestamp() != 0)
					throw new Exception("Bad second log time stamp");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + taskid).equals(stream))
					throw new Exception("Bad stream name");

				return awsresponse;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				PutLogEventsRequest request = (PutLogEventsRequest) invocationOnMock.getArguments()[0];

				List<InputLogEvent> awsEvents = request.logEvents();

				String token = request.sequenceToken();

				if (!expectedToken.equalsIgnoreCase(token))
					throw new Exception("Bad token " + token);

				if (!msg3.equalsIgnoreCase(awsEvents.get(0).message()))
					throw new Exception("Bad first log message in second chunck");

				if (milli3 + nano3 - awsEvents.get(0).timestamp() != 0)
					throw new Exception("Bad first log time stamp in second chunck");

				if (!msg4.equalsIgnoreCase(awsEvents.get(1).message()))
					throw new Exception("Bad second log message in second chunck");

				if (milli4 + nano4 - awsEvents.get(1).timestamp() != 0)
					throw new Exception("Bad second log time stamp in second chunck");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + taskid).equals(stream))
					throw new Exception("Bad stream name");

				return awsresponse2;
			}
		}).when(client).putLogEvents((PutLogEventsRequest) Mockito.any());

		Assert.assertEquals((Integer) 2, storage.writeLog(runid, taskid, chunck));
		Assert.assertEquals((Integer) 2, storage.writeLog(runid, taskid, chunck2));

		Mockito.verify(storage, Mockito.times(2)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(2)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test8_1() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);

		Long nano2 = 114L;
		Long milli2 = 1634303408000L;
		BPLog log2 = new BPLog(milli2, nano2);
		String msg2 = "Second test message";
		log2.setMessage(msg2);

		Long nano3 = 113L;
		Long milli3 = 1634303408000L;
		BPLog log3 = new BPLog(milli3, nano3);
		String msg3 = "Test 3 message";
		log3.setMessage(msg3);

		Long nano4 = 119L;
		Long milli4 = 1634303409000L;
		BPLog log4 = new BPLog(milli4, nano4);
		String msg4 = "Second 4 test message";
		log4.setMessage(msg4);

		BPLogChunk chunck = new BPLogChunk();
		chunck.getSet().add(log2);
		chunck.getSet().add(log);

		BPLogChunk chunck2 = new BPLogChunk();
		chunck2.getSet().add(log4);
		chunck2.getSet().add(log3);

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		String expectedToken = "sdfghjklòàùtyuiopè";

		Mockito.doReturn(expectedToken).when(awsresponse).nextSequenceToken();

		PutLogEventsResponse awsresponse2 = Mockito.mock(PutLogEventsResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				PutLogEventsRequest request = (PutLogEventsRequest) invocationOnMock.getArguments()[0];

				List<InputLogEvent> awsEvents = request.logEvents();

				if (!msg.equalsIgnoreCase(awsEvents.get(0).message()))
					throw new Exception("Bad first log message");

				if (milli + nano - awsEvents.get(0).timestamp() != 0)
					throw new Exception("Bad first log time stamp");

				if (!msg2.equalsIgnoreCase(awsEvents.get(1).message()))
					throw new Exception("Bad second log message");

				if (milli2 + nano2 - awsEvents.get(1).timestamp() != 0)
					throw new Exception("Bad second log time stamp");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + taskid).equals(stream))
					throw new Exception("Bad stream name " + stream);

				return awsresponse;
			}
		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				PutLogEventsRequest request = (PutLogEventsRequest) invocationOnMock.getArguments()[0];

				List<InputLogEvent> awsEvents = request.logEvents();

				String token = request.sequenceToken();

				if (!expectedToken.equalsIgnoreCase(token))
					throw new Exception("Bad token " + token);

				if (!msg3.equalsIgnoreCase(awsEvents.get(0).message()))
					throw new Exception("Bad first log message in second chunck");

				if (milli2 + nano2 + 1 - awsEvents.get(0).timestamp() != 0)
					throw new Exception("Bad first log time stamp in second chunck");

				if (!msg4.equalsIgnoreCase(awsEvents.get(1).message()))
					throw new Exception("Bad second log message in second chunck");

				if (milli4 + nano4 - awsEvents.get(1).timestamp() != 0)
					throw new Exception("Bad second log time stamp in second chunck");

				String group = request.logGroupName();

				if (!testModelGroup.equals(group))
					throw new Exception("Bad group");

				String stream = request.logStreamName();

				if (!(testPrefix + "/" + testCName + "/" + taskid).equals(stream))
					throw new Exception("Bad stream name");

				return awsresponse2;
			}
		}).when(client).putLogEvents((PutLogEventsRequest) Mockito.any());

		Assert.assertEquals((Integer) 2, storage.writeLog(runid, taskid, chunck));
		Assert.assertEquals((Integer) 2, storage.writeLog(runid, taskid, chunck2));

		Mockito.verify(storage, Mockito.times(2)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(2)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test9() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);

		Integer numberoflogs = 4009;

		BPLogChunk chunck = new BPLogChunk();

		for (int i = 0; i < numberoflogs; i++) {

			chunck.getSet().add(log);
			log = new BPLog(milli + (i * 1000), nano);
			msg = "Test message " + i;
			log.setMessage(msg);

		}

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		String expectedToken = "sdfghjklòàùtyuiopè";

		Mockito.doReturn(expectedToken).when(awsresponse).nextSequenceToken();

		Mockito.doReturn(awsresponse).when(client).putLogEvents((PutLogEventsRequest) Mockito.any());
		Assert.assertEquals((Integer) numberoflogs, storage.writeLog(runid, taskid, chunck));

		Mockito.verify(storage, Mockito.times(3)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(3)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test10() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);

		Integer numberoflogs = 4009;

		BPLogChunk chunck = new BPLogChunk();

		for (int i = 0; i < numberoflogs; i++) {

			chunck.getSet().add(log);
			log = new BPLog(milli + (i * 1000), nano);
			msg = "Test message " + i;
			log.setMessage(msg);

		}

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		String expectedToken = "sdfghjklòàùtyuiopè";

		Mockito.doReturn(expectedToken).when(awsresponse).nextSequenceToken();

		RejectedLogEventsInfo rej = Mockito.mock(RejectedLogEventsInfo.class);

		Integer old = 2;
		Integer nnew = 1;
		Integer expired = 9;

		Mockito.doReturn(old).when(rej).tooOldLogEventEndIndex();
		Mockito.doReturn(nnew).when(rej).tooNewLogEventStartIndex();
		Mockito.doReturn(expired).when(rej).expiredLogEventEndIndex();

		Mockito.doReturn(rej).when(awsresponse).rejectedLogEventsInfo();

		Mockito.doReturn(awsresponse).when(client).putLogEvents((PutLogEventsRequest) Mockito.any());
		Integer expectedwritten = numberoflogs - (3 * (old + nnew + expired));
		Assert.assertEquals((Integer) expectedwritten, storage.writeLog(runid, taskid, chunck));

		Mockito.verify(storage, Mockito.times(3)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(3)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test11() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);

		Integer numberoflogs = 4009;

		BPLogChunk chunck = new BPLogChunk();

		for (int i = 0; i < numberoflogs; i++) {

			chunck.getSet().add(log);
			log = new BPLog(milli + (i * 1000), nano);
			msg = "Test message " + i;
			log.setMessage(msg);

		}

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		String expectedToken = "sdfghjklòàùtyuiopè";

		Mockito.doReturn(expectedToken).when(awsresponse).nextSequenceToken();

		RejectedLogEventsInfo rej = Mockito.mock(RejectedLogEventsInfo.class);

		Integer old = 2;
		Integer nnew = 1;
		Integer expired = 9;

		Mockito.doReturn(old).when(rej).tooOldLogEventEndIndex();
		Mockito.doReturn(nnew).when(rej).tooNewLogEventStartIndex();
		Mockito.doReturn(expired).when(rej).expiredLogEventEndIndex();

		Mockito.doReturn(rej).when(awsresponse).rejectedLogEventsInfo();

		Mockito.doReturn(awsresponse).doThrow(DataAlreadyAcceptedException.class).when(client).putLogEvents(
				(PutLogEventsRequest) Mockito.any());

		Integer expectedwritten = 2000 - (old + nnew + expired);
		Assert.assertEquals((Integer) expectedwritten, storage.writeLog(runid, taskid, chunck));

		Mockito.verify(storage, Mockito.times(3)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(3)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test12() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);

		Integer numberoflogs = 4019;

		BPLogChunk chunck = new BPLogChunk();

		for (int i = 0; i < numberoflogs; i++) {

			chunck.getSet().add(log);
			log = new BPLog(milli + (i * 1000), nano);
			msg = "Test message " + i;
			log.setMessage(msg);

		}

		PutLogEventsResponse awsresponse = Mockito.mock(PutLogEventsResponse.class);

		String expectedToken = "sdfghjklòàùtyuiopè";

		Mockito.doReturn(expectedToken).when(awsresponse).nextSequenceToken();

		RejectedLogEventsInfo rej = Mockito.mock(RejectedLogEventsInfo.class);

		Integer old = 2;
		Integer nnew = 1;
		Integer expired = 9;

		Mockito.doReturn(old).when(rej).tooOldLogEventEndIndex();
		Mockito.doReturn(nnew).when(rej).tooNewLogEventStartIndex();
		Mockito.doReturn(expired).when(rej).expiredLogEventEndIndex();

		Mockito.doReturn(rej).when(awsresponse).rejectedLogEventsInfo();

		Mockito.doThrow(DataAlreadyAcceptedException.class).doReturn(awsresponse).when(client).putLogEvents(
				(PutLogEventsRequest) Mockito.any());

		Integer expectedwritten = 2000 + 19 - (2 * (old + nnew + expired));
		Assert.assertEquals((Integer) expectedwritten, storage.writeLog(runid, taskid, chunck));

		Mockito.verify(storage, Mockito.times(3)).doWriteLog(Mockito.any(), Mockito.any());
		Mockito.verify(client, Mockito.times(3)).putLogEvents((PutLogEventsRequest) Mockito.any());
	}

	@Test
	public void test14() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Mockito.doReturn(Mockito.mock(CreateLogStreamResponse.class)).when(client).createLogStream((CreateLogStreamRequest) Mockito.any());

		Assert.assertTrue(storage.createLog(runid, taskid));

		Mockito.verify(storage, Mockito.times(1)).doCreateLogStream(Mockito.any());
		Mockito.verify(client, Mockito.times(1)).createLogStream((CreateLogStreamRequest) Mockito.any());
	}

	@Test
	public void test15() throws BPException {
		AWSBPRunLogStorage storage = initAWSBPRunLogStorage();
		CloudWatchLogsClient client = Mockito.mock(CloudWatchLogsClient.class);

		Mockito.doReturn(client).when(storage).getClient();
		String taskid = "taskid";
		String runid = "runid";

		Mockito.doThrow(InvalidParameterException.class).when(client).createLogStream((CreateLogStreamRequest) Mockito.any());

		Assert.assertFalse(storage.createLog(runid, taskid));

		Mockito.verify(storage, Mockito.times(1)).doCreateLogStream(Mockito.any());
		Mockito.verify(client, Mockito.times(1)).createLogStream((CreateLogStreamRequest) Mockito.any());

	}

}