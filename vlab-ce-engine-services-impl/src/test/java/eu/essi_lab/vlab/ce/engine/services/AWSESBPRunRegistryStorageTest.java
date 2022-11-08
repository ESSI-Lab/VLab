package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.ESQueryBPRuns;
import eu.essi_lab.vlab.core.datamodel.QueueMessage;
import eu.essi_lab.vlab.core.datamodel.QueueMessageHandler;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import io.kubemq.sdk.queue.Transaction;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class AWSESBPRunRegistryStorageTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();
	private AWSESBPRunRegistryStorage registryStorage;

	@Test
	public void test() throws BPException {

		expectedException.expect(BPException.class);

		String requesterEmail = "requesterEmail@mail.com";

		String runid = "runid";

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn("owner@mail.com").when(run).getOwner();

		Mockito.doReturn(new ArrayList<>()).when(run).getSharedWith();

		Mockito.doReturn(false).when(run).isPublicRun();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		registryStorage.get(runid, requesterEmail);

	}

	@Test
	public void test3() throws BPException {

		String requesterEmail = "requesterEmail@mail.com";

		String runid = "runid";

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn("owner@mail.com").when(run).getOwner();

		List<String> list = new ArrayList<>();

		list.add(requesterEmail);

		Mockito.doReturn(list).when(run).getSharedWith();

		Mockito.doReturn(false).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		BPRun found = registryStorage.get(runid, requesterEmail);

		assertEquals(runid, found.getRunid());

	}

	@Test
	public void test4() throws BPException {

		String ownerEmail = "owner@mail.com";

		String runid = "runid";

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(ownerEmail).when(run).getOwner();

		List<String> list = new ArrayList<>();

		Mockito.doReturn(list).when(run).getSharedWith();

		Mockito.doReturn(false).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		BPRun found = registryStorage.get(runid, ownerEmail);

		assertEquals(runid, found.getRunid());

	}

	@Test
	public void testUpdateMyRunOk() throws BPException {

		String ownerEmail = "owner@mail.com";

		String runid = "runid";

		BPRun newrun = Mockito.mock(BPRun.class);

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(ownerEmail).when(run).getOwner();

		List<String> list = new ArrayList<>();

		Mockito.doReturn(list).when(run).getSharedWith();

		Mockito.doReturn(false).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).storeToES(Mockito.any());

		boolean updated = registryStorage.udpdateRun(newrun, ownerEmail);

		assertTrue(updated);

		Mockito.verify(registryStorage, Mockito.times(1)).storeToES(Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(1)).readFromES(Mockito.any());

	}

	@Test
	public void testUpdateNotMyRunFail() throws BPException {

		String ownerEmail = "owner@mail.com";

		String runid = "runid";

		BPRun newrun = Mockito.mock(BPRun.class);

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(ownerEmail).when(run).getOwner();

		List<String> list = new ArrayList<>();

		Mockito.doReturn(list).when(run).getSharedWith();

		Mockito.doReturn(false).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).storeToES(Mockito.any());
		try {

			registryStorage.udpdateRun(newrun, ownerEmail + "m");

			assertFalse("BPException expected trying to update a non-owned run", true);

		} catch (BPException e) {

			assertEquals((Integer) BPException.ERROR_CODES.NOT_AUTHORIZED.getCode(), e.getErroCode());

		}

		Mockito.verify(registryStorage, Mockito.times(0)).storeToES(Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(1)).readFromES(Mockito.any());

	}

	@Test
	public void testUpdateMyRunFailStoreToES() throws BPException {

		String ownerEmail = "owner@mail.com";

		String runid = "runid";

		BPRun newrun = Mockito.mock(BPRun.class);

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(ownerEmail).when(run).getOwner();

		List<String> list = new ArrayList<>();

		Mockito.doReturn(list).when(run).getSharedWith();

		Mockito.doReturn(false).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		Mockito.doReturn(false).when(registryStorage).storeToES(Mockito.any());

		boolean updated = registryStorage.udpdateRun(newrun, ownerEmail);

		assertFalse("False result expected when store to es fails", updated);

		Mockito.verify(registryStorage, Mockito.times(1)).storeToES(Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(1)).readFromES(Mockito.any());

	}

	@Before
	public void before() throws BPException {

		String es_accessKey = "accessKey";
		String es_secretKey = "secretKey";
		String bucketName = "secretKey";
		String sqsName = "sqsName";
		String esUrl = "esUrl";
		String es_region = "region";
		String sqs_accessKey = "acc";
		String sqs_secretKey = "sec";
		String sqs_region = "region";

		environmentVariables.set(BPStaticConfigurationParameters.AWS_SQS_BP_RUN_QUEQUE_NAME.getParameter().getKey(), sqsName);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_SQS_ACCESS_KEY.getParameter().getKey(), sqs_accessKey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_SQS_SECRET_KEY.getParameter().getKey(), sqs_secretKey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_SQS_REGION.getParameter().getKey(), sqs_region);

		environmentVariables.set(BPStaticConfigurationParameters.STORAGE_ES_USER.getParameter().getKey(), es_accessKey);
		environmentVariables.set(BPStaticConfigurationParameters.STORAGE_ES_PWD.getParameter().getKey(), es_secretKey);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_ES_REGION.getParameter().getKey(), es_region);
		environmentVariables.set(BPStaticConfigurationParameters.AWS_S3_BP_RUN_BUCKET_NAME.getParameter().getKey(), bucketName);
		environmentVariables.set(BPStaticConfigurationParameters.STORAGE_ESURL.getParameter().getKey(), esUrl);

		SQSClient client = new SQSClient();

		Map<ConfigurationParameter, String> map = Mockito.spy(new HashMap<>());

		client.configurationParameters().forEach(configurationParameter -> map.put(configurationParameter,
				ConfigurationLoader.loadConfigurationParameter(configurationParameter)));

		client.configure(map);

		registryStorage = Mockito.spy(new AWSESBPRunRegistryStorage());

		registryStorage.setQueueClient(client);

		registryStorage.configurationParameters().forEach(configurationParameter -> map.put(configurationParameter,
				ConfigurationLoader.loadConfigurationParameter(configurationParameter)));

		registryStorage.configure(map);

		assertEquals(es_secretKey, registryStorage.getEsClient().getSubmitter().getPwd());
		assertEquals(es_accessKey, registryStorage.getEsClient().getSubmitter().getUser());

	}

	@Test
	public void test2() throws BPException {

		String requesterEmail = "requesterEmail@mail.com";

		String runid = "runid";

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn("owner@mail.com").when(run).getOwner();

		Mockito.doReturn(new ArrayList<>()).when(run).getSharedWith();

		Mockito.doReturn(true).when(run).isPublicRun();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		Mockito.doReturn(runid).when(run).getRunid();

		BPRun found = registryStorage.get(runid, requesterEmail);

		assertEquals(runid, found.getRunid());

	}

	@Test
	public void testQueue() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(run).getOwner();

		Mockito.doReturn(true).when(registryStorage).storeToES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).toQueue(Mockito.any());

		assertTrue(registryStorage.queue(run));

	}

	@Test
	public void testQueueFailStore() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(run).getOwner();

		Mockito.doReturn(false).when(registryStorage).storeToES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).toQueue(Mockito.any());

		assertFalse(registryStorage.queue(run));

		Mockito.verify(registryStorage, Mockito.times(0)).remove(Mockito.any(), Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(0)).doRemove(Mockito.any());

	}

	@Test
	public void testQueueFailToqueue() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(run).getOwner();

		Mockito.doReturn(true).when(registryStorage).storeToES(Mockito.any());

		Mockito.doReturn(false).when(registryStorage).toQueue(Mockito.any());

		assertFalse(registryStorage.queue(run));

		Mockito.verify(registryStorage, Mockito.times(0)).remove(Mockito.any(), Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(0)).doRemove(Mockito.any());
	}

	@Test
	public void testQueueExceptionToqueue() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(run).getOwner();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).storeToES(Mockito.any());

		Mockito.doThrow(BPException.class).when(registryStorage).toQueue(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).doRemove(Mockito.any());

		assertFalse(registryStorage.queue(run));

		Mockito.verify(registryStorage, Mockito.times(1)).remove(Mockito.any(), Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(1)).doRemove(Mockito.any());
	}

	@Test
	public void testQueueExceptionToqueueRemoveFail() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(run).getOwner();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).storeToES(Mockito.any());

		Mockito.doThrow(BPException.class).when(registryStorage).toQueue(Mockito.any());

		Mockito.doReturn(false).when(registryStorage).doRemove(Mockito.any());

		assertFalse(registryStorage.queue(run));

		Mockito.verify(registryStorage, Mockito.times(1)).remove(Mockito.any(), Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(1)).doRemove(Mockito.any());
	}

	@Test
	public void testQueueExceptionToqueueRemoveException() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(run).getOwner();

		Mockito.doReturn(run).when(registryStorage).readFromES(Mockito.any());
		Mockito.doReturn(true).when(registryStorage).storeToES(Mockito.any());

		Mockito.doThrow(BPException.class).when(registryStorage).toQueue(Mockito.any());

		Mockito.doThrow(BPException.class).when(registryStorage).doRemove(Mockito.any());

		assertFalse(registryStorage.queue(run));

		Mockito.verify(registryStorage, Mockito.times(1)).remove(Mockito.any(), Mockito.any());

		Mockito.verify(registryStorage, Mockito.times(1)).doRemove(Mockito.any());

	}

	@Test
	public void testRemoveUnauthorized() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		BPRun readRun = Mockito.mock(BPRun.class);
		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(run).getOwner();

		Mockito.doReturn(readRun).when(registryStorage).readFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).doRemove(Mockito.any());

		String requestingEmail = "requesting@mail.com";

		try {

			registryStorage.remove(run, requestingEmail);

			assertTrue("Expected exception", false);

		} catch (BPException e) {

			assertEquals((Integer) BPException.ERROR_CODES.NOT_AUTHORIZED.getCode(), e.getErroCode());

			Mockito.verify(registryStorage, Mockito.times(0)).doRemove(Mockito.any());

			return;

		}

	}

	@Test
	public void testRemoveAuthorized() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(true).when(registryStorage).doRemove(Mockito.any());

		String requestingEmail = "owner@mail.com";

		BPRun readRun = Mockito.mock(BPRun.class);

		String owner = "owner@mail.com";
		Mockito.doReturn(owner).when(readRun).getOwner();

		Mockito.doReturn(readRun).when(registryStorage).readFromES(Mockito.any());

		assertTrue(registryStorage.remove(run, requestingEmail));

		Mockito.verify(registryStorage, Mockito.times(1)).doRemove(Mockito.any());

	}

	@Test
	public void testNext() throws BPException, IOException {

		BPRun run = Mockito.mock(BPRun.class);
		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		QueueMessage message = Mockito.mock(QueueMessage.class);

		Mockito.doReturn(message).when(registryStorage).fromQueue();

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		String body = IOUtils.toString(stream);

		Mockito.doReturn(body).when(message).getBody();

		Mockito.doReturn(true).when(registryStorage).storeReceipt(Mockito.any(), Mockito.any());

		Optional<BPRun> nextRun = registryStorage.nextQuequedBPRun();

		assertTrue(nextRun.isPresent());

		assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", nextRun.get().getRunid());

	}

	@Test
	public void testNextNull() throws BPException, IOException {

		BPRun run = Mockito.mock(BPRun.class);
		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		Mockito.doReturn(null).when(registryStorage).fromQueue();

		Optional<BPRun> nextRun = registryStorage.nextQuequedBPRun();

		assertFalse(nextRun.isPresent());

	}

	@Test
	public void testExtendVisibilityTimeout() throws BPException, IOException {

		BPRun run = Mockito.mock(BPRun.class);
		Integer seconds = 3;

		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		InputStream receiptStream = new ByteArrayInputStream("{\"sqsreceipt\": \"pippo\"}".getBytes());

		Mockito.doReturn(receiptStream).when(registryStorage).readStreamFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).doExtendVisibilityTimeout(Mockito.any(), Mockito.eq(seconds));

		registryStorage.extendVisibilityTimeout(run, seconds);

		Mockito.verify(registryStorage, Mockito.times(1)).doExtendVisibilityTimeout(Mockito.any(), Mockito.eq(seconds));

	}

	@Test
	public void testSearch() throws BPException {

		BPUser user = Mockito.mock(BPUser.class);
		String email = "email@email.com";
		Mockito.doReturn(email).when(user).getEmail();

		Integer start = 3;
		Integer count = 30;

		String wfid = "wfid";

		String searchtext = "searchtext";

		BPRuns runs = Mockito.mock(BPRuns.class);
		Integer total = 4;

		Mockito.doReturn(total).when(runs).getTotal();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPUser u = (BPUser) invocation.getArguments()[0];

				ESQueryBPRuns qu = (ESQueryBPRuns) invocation.getArguments()[1];

				if (!email.equalsIgnoreCase(u.getEmail()))
					throw new Exception("Bad user");

				if (start - qu.getStart() != 0)
					throw new Exception("Bad start");

				if (count - qu.getCount() != 0)
					throw new Exception("Bad count");

				if (!searchtext.equalsIgnoreCase(qu.getText()))
					throw new Exception("Bad search text");

				if (!wfid.equalsIgnoreCase(qu.getWfid()))
					throw new Exception("Bad wfid");

				return runs;
			}
		}).when(registryStorage).doSearch(Mockito.any(), Mockito.any());

		BPRuns result = registryStorage.search(user, searchtext, start, count, wfid);

		Assert.assertEquals(total, result.getTotal());

	}

	@Test
	public void testExtendVisibilityTimeout2() throws BPException, IOException {

		expectedException.expect(
				new BPExceptionMatcher("IOException reading receipt of run null", BPException.ERROR_CODES.RESOURCE_NOT_FOUND));

		BPRun run = Mockito.mock(BPRun.class);
		Integer seconds = 3;

		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		Mockito.doThrow(IOException.class).when(registryStorage).readReceipt(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).doExtendVisibilityTimeout(Mockito.any(), Mockito.eq(seconds));

		registryStorage.extendVisibilityTimeout(run, seconds);

	}

	@Test
	public void testExtendVisibilityTimeout3() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Test ex", BPException.ERROR_CODES.API_CALL_ERROR));

		BPRun run = Mockito.mock(BPRun.class);
		Integer seconds = 3;

		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		Mockito.doThrow(new BPException("Test ex", BPException.ERROR_CODES.API_CALL_ERROR)).when(registryStorage).readStreamFromES(
				Mockito.any());

		Mockito.doReturn(true).when(registryStorage).doExtendVisibilityTimeout(Mockito.any(), Mockito.eq(seconds));

		registryStorage.extendVisibilityTimeout(run, seconds);

	}

	@Test
	public void testMoveToTriggered() throws BPException, IOException {

		BPRun run = Mockito.mock(BPRun.class);
		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		InputStream receiptStream = new ByteArrayInputStream("{\"sqsreceipt\": \"pippo\"}".getBytes());

		Mockito.doReturn(receiptStream).when(registryStorage).readStreamFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).doRemove(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).deleteMessage(Mockito.any());

		assertTrue(registryStorage.moveToTriggered(run));

	}

	@Test
	public void testMoveToTriggered2() throws BPException, IOException {

		BPRun run = Mockito.mock(BPRun.class);
		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		InputStream receiptStream = new ByteArrayInputStream("{\"sqsreceipt\": \"pippo\"}".getBytes());

		Mockito.doReturn(receiptStream).when(registryStorage).readStreamFromES(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).deleteMessage(Mockito.any());

		Mockito.doThrow(BPException.class).when(registryStorage).doRemove(Mockito.any());

		assertFalse(registryStorage.moveToTriggered(run));

	}

	@Test
	public void testMoveToTriggered3() throws BPException, IOException {

		BPRun run = Mockito.mock(BPRun.class);
		String owner = "owner@mail.com";

		Mockito.doReturn(owner).when(run).getOwner();

		String runid = "runid";

		Mockito.doReturn(runid).when(run).getOwner();

		Transaction tran = Mockito.mock(Transaction.class);

		Optional<Object> local = Optional.of(tran);

		Mockito.doReturn(local).when(registryStorage).readReceiptFromLocal(Mockito.any());

		Mockito.doReturn(true).when(registryStorage).doRemove(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				QueueMessageHandler handler = (QueueMessageHandler) invocationOnMock.getArguments()[0];

				if (!handler.getReceiptHandler().isPresent())
					throw new Exception("Expected transation object");

				return true;
			}
		}).when(registryStorage).deleteMessage(Mockito.any());

		assertTrue(registryStorage.moveToTriggered(run));

		Mockito.verify(registryStorage, Mockito.times(0)).readStreamFromES(Mockito.any());
		Mockito.verify(registryStorage, Mockito.times(0)).doRemove(Mockito.any());
		Mockito.verify(registryStorage, Mockito.times(1)).reemoveReceiptFromLocal(Mockito.any());

	}

	@Test
	public void testReadFromES() throws BPException {

		String key = "key";

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String k = (String) invocation.getArguments()[0];

				if (!k.equalsIgnoreCase(key))
					throw new Exception("Bad key: " + k);

				return stream;
			}
		}).when(registryStorage).readStreamFromES(Mockito.anyString());

		BPRun run = registryStorage.readFromES(key);

		assertEquals(3, run.getInputs().size());

		assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", run.getRunid());

		assertFalse(run.isPublicRun());

	}

	@Test
	public void testShareWithNotAuth() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Tried to perform an unauthorized action on run ", BPException.ERROR_CODES.NOT_AUTHORIZED));

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "requester@mail.com";
		String shareTargetEmail = "sharewith@mail.com";

		registryStorage.shareWith(run, requesterEmail, shareTargetEmail);

	}

	@Test
	public void testShareWith() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";
		String shareTargetEmail = "sharewith@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (!r.getSharedWith().contains(shareTargetEmail))
					throw new Exception("Missing new share");

				return false;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertFalse(registryStorage.shareWith(run, requesterEmail, shareTargetEmail));

	}

	@Test
	public void testShareWith2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";
		String shareTargetEmail = "sharewith@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (!r.getSharedWith().contains(shareTargetEmail))
					throw new Exception("Missing new share");

				return true;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertTrue(registryStorage.shareWith(run, requesterEmail, shareTargetEmail));

	}

	@Test
	public void testrevokeShareNotAuth() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Tried to perform an unauthorized action on run ", BPException.ERROR_CODES.NOT_AUTHORIZED));

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "requester@mail.com";
		String shareTargetEmail = "sharewith@mail.com";

		registryStorage.revokeShare(run, requesterEmail, shareTargetEmail);

	}

	@Test
	public void testrevokeShare() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";
		String shareTargetEmail = "sharewith@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (r.getSharedWith().contains(shareTargetEmail))
					throw new Exception("Share was not removed");

				return false;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertFalse(registryStorage.revokeShare(run, requesterEmail, shareTargetEmail));

	}

	@Test
	public void testrevokeShare2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";
		String shareTargetEmail = "sharewith@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (r.getSharedWith().contains(shareTargetEmail))
					throw new Exception("Share was not removed");

				return true;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertTrue(registryStorage.revokeShare(run, requesterEmail, shareTargetEmail));

	}

	@Test
	public void testmakePublicNotAuth() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Tried to perform an unauthorized action on run ", BPException.ERROR_CODES.NOT_AUTHORIZED));

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "requester@mail.com";

		registryStorage.makePublic(run, requesterEmail);

	}

	@Test
	public void testrmakePublic() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (!r.isPublicRun())
					throw new Exception("Run not public");

				return false;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertFalse(registryStorage.makePublic(run, requesterEmail));

	}

	@Test
	public void testmakePublic2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (!r.isPublicRun())
					throw new Exception("Run not public");

				return true;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertTrue(registryStorage.makePublic(run, requesterEmail));

	}

	@Test
	public void testrevokePublicNotAuth() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Tried to perform an unauthorized action on run ", BPException.ERROR_CODES.NOT_AUTHORIZED));

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "requester@mail.com";

		registryStorage.revokePublic(run, requesterEmail);

	}

	@Test
	public void testrevokePublic() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (r.isPublicRun())
					throw new Exception("Run public");

				return false;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertFalse(registryStorage.revokePublic(run, requesterEmail));

	}

	@Test
	public void testrevokePublic2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		BPRun run = new JSONDeserializer().deserialize(stream, BPRun.class);

		String requesterEmail = "owner@mail.com";

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (r.isPublicRun())
					throw new Exception("Run public");

				return true;
			}
		}).when(registryStorage).storeToES(Mockito.any());

		assertTrue(registryStorage.revokePublic(run, requesterEmail));

	}

	@Test
	public void testStoreReceipt() throws BPException {
		String receiptid = "receiptid";

		QueueMessageHandler handler = Mockito.mock(QueueMessageHandler.class);

		String handleid = "handleid";

		Optional<String> receiptOptTextId = Optional.of(handleid);

		Mockito.doReturn(receiptOptTextId).when(handler).getReceiptHandleId();

		Mockito.doReturn(Boolean.TRUE).when(registryStorage).storeReceiptHanlderId(Mockito.any(), Mockito.any());

		assertTrue(registryStorage.storeReceipt(receiptid, handler));

		Mockito.verify(registryStorage, Mockito.times(0)).storeReceiptHanlderObject(Mockito.any(), Mockito.any());
	}

	@Test
	public void testStoreReceipt2() throws BPException {
		String receiptid = "receiptid";

		QueueMessageHandler handler = Mockito.mock(QueueMessageHandler.class);

		Optional<String> receiptOptTextId = Optional.empty();

		Mockito.doReturn(receiptOptTextId).when(handler).getReceiptHandleId();

		Transaction tran = Mockito.mock(Transaction.class);

		Optional<Object> receiptOptObject = Optional.of(tran);

		Mockito.doReturn(receiptOptObject).when(handler).getReceiptHandler();

		assertTrue(registryStorage.storeReceipt(receiptid, handler));

		Mockito.verify(registryStorage, Mockito.times(0)).storeReceiptHanlderId(Mockito.any(), Mockito.any());
	}

	@Test
	public void testStoreReceipt3() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Can't find receipt handler for receiptid",
				BPException.ERROR_CODES.NO_QUEUE_MESSAGE_RECIPT_HANDLER));
		String receiptid = "receiptid";

		QueueMessageHandler handler = Mockito.mock(QueueMessageHandler.class);

		Optional<String> receiptOptTextId = Optional.empty();

		Mockito.doReturn(receiptOptTextId).when(handler).getReceiptHandleId();

		Optional<Object> receiptOptObject = Optional.empty();

		Mockito.doReturn(receiptOptObject).when(handler).getReceiptHandler();

		assertTrue(registryStorage.storeReceipt(receiptid, handler));

	}

}