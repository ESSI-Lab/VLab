package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.QueueMessage;
import eu.essi_lab.vlab.core.datamodel.QueueMessageHandler;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.InvalidIdFormatException;
import software.amazon.awssdk.services.sqs.model.InvalidMessageContentsException;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.OverLimitException;
import software.amazon.awssdk.services.sqs.model.QueueDeletedRecentlyException;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.ReceiptHandleIsInvalidException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * @author Mattia Santoro
 */
public class SQSClientTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void test() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		Mockito.doReturn(null).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		sqsClient.quequeExists(false);
	}

	@Test
	public void testCreateAlreadyExists() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		Mockito.doReturn(null).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);
		sqsClient.quequeExists(true);

		Mockito.verify(client, Mockito.times(0)).createQueue((CreateQueueRequest) Mockito.any());
	}

	@Test
	public void testCreateNotExists() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		Mockito.doThrow(QueueDoesNotExistException.class).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		Mockito.doReturn(null).when(client).createQueue((CreateQueueRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		sqsClient.quequeExists(true);

		Mockito.verify(client, Mockito.times(1)).createQueue((CreateQueueRequest) Mockito.any());
	}

	@Test
	public void testCreateNotExistsException() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		Mockito.doThrow(QueueDoesNotExistException.class).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		QueueDeletedRecentlyException ex = Mockito.mock(QueueDeletedRecentlyException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).createQueue((CreateQueueRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		sqsClient.quequeExists(true);

		Mockito.verify(client, Mockito.times(1)).createQueue((CreateQueueRequest) Mockito.any());
	}

	@Test
	public void test2() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		Mockito.doThrow(QueueDoesNotExistException.class).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		sqsClient.quequeExists(false);
	}

	@Test
	public void testAdd() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		SendMessageResponse createResponse = Mockito.mock(SendMessageResponse.class);

		String msgid = "msgid";

		Mockito.doReturn(msgid).when(createResponse).messageId();

		Mockito.doReturn(createResponse).when(client).sendMessage((SendMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String message = "message";
		String r = sqsClient.add(message);

		Assert.assertEquals(msgid, r);
	}

	@Test
	public void testAddException1() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		QueueDoesNotExistException ex = Mockito.mock(QueueDoesNotExistException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).sendMessage((SendMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String message = "message";
		sqsClient.add(message);

	}

	@Test
	public void testAddException2() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());
		InvalidMessageContentsException ex = Mockito.mock(InvalidMessageContentsException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).sendMessage((SendMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String message = "message";
		sqsClient.add(message);

	}

	@Test
	public void testAddException3() throws BPException {

		expectedException.expect(BPException.class);
		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		Mockito.doThrow(UnsupportedOperationException.class).when(client).sendMessage((SendMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String message = "message";
		sqsClient.add(message);

	}

	@Test
	public void testAddException4() throws BPException {

		expectedException.expect(BPException.class);
		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		Mockito.doThrow(AwsServiceException.class).when(client).sendMessage((SendMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String message = "message";
		sqsClient.add(message);

	}

	@Test
	public void getGetMessage() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		ReceiveMessageResponse mResult = Mockito.mock(ReceiveMessageResponse.class);

		Mockito.doReturn(new ArrayList<>()).when(mResult).messages();

		Mockito.doReturn(mResult).when(client).receiveMessage((ReceiveMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		QueueMessage next = sqsClient.getNextMessage();

		Assert.assertNull(next);

	}

	@Test
	public void getGetMessage2() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		ReceiveMessageResponse mResult = Mockito.mock(ReceiveMessageResponse.class);

		List<Message> list = new ArrayList<>();

		Message message = Mockito.mock(Message.class);

		String body = "body";

		Mockito.doReturn(body).when(message).body();

		String receipt = "rec";

		Mockito.doReturn(receipt).when(message).receiptHandle();
		list.add(message);

		Mockito.doReturn(list).when(mResult).messages();

		Mockito.doReturn(mResult).when(client).receiveMessage((ReceiveMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		QueueMessage next = sqsClient.getNextMessage();

		Assert.assertNotNull(next);

		Assert.assertEquals(body, next.getBody());

		Assert.assertNotNull(next.getHandler());

		Assert.assertTrue(next.getHandler().getReceiptHandleId().isPresent());

		Assert.assertEquals(receipt, next.getHandler().getReceiptHandleId().get());

	}

	@Test
	public void getGetMessageException1() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		QueueDoesNotExistException ex = Mockito.mock(QueueDoesNotExistException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).receiveMessage((ReceiveMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		sqsClient.getNextMessage();
	}

	@Test
	public void getGetMessageException2() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		OverLimitException ex = Mockito.mock(OverLimitException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();
		Mockito.doThrow(ex).when(client).receiveMessage((ReceiveMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		sqsClient.getNextMessage();
	}

	@Test
	public void getGetMessageException3() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());
		Mockito.doThrow(AwsServiceException.class).when(client).receiveMessage((ReceiveMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		sqsClient.getNextMessage();
	}

	@Test
	public void deleteMessage() throws BPException {

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		Mockito.doReturn(null).when(client).deleteMessage((DeleteMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String receipt = "receipt";

		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		Assert.assertTrue(sqsClient.deleteMessage(handler));

	}

	@Test
	public void deleteMessageException1() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		QueueDoesNotExistException ex = Mockito.mock(QueueDoesNotExistException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).deleteMessage((DeleteMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String receipt = "receipt";
		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		sqsClient.deleteMessage(handler);

	}

	@Test
	public void deleteMessageException2() throws BPException {
		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());
		InvalidIdFormatException ex = Mockito.mock(InvalidIdFormatException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();
		Mockito.doThrow(ex).when(client).deleteMessage((DeleteMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String receipt = "receipt";

		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);
		sqsClient.deleteMessage(handler);

	}

	@Test
	public void deleteMessageException3() throws BPException {

		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());
		ReceiptHandleIsInvalidException ex = Mockito.mock(ReceiptHandleIsInvalidException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).deleteMessage((DeleteMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);
		String receipt = "receipt";

		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		sqsClient.deleteMessage(handler);

	}

	@Test
	public void deleteMessageException4() throws BPException {
		expectedException.expect(BPException.class);

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		Mockito.doThrow(AwsServiceException.class).when(client).deleteMessage((DeleteMessageRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		String receipt = "receipt";

		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		sqsClient.deleteMessage(handler);

	}

	@Test
	public void extendVis() throws BPException {
		Integer seconds = 33;
		String receipt = "receipt";

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		Mockito.doReturn(null).when(client).changeMessageVisibility((ChangeMessageVisibilityRequest) Mockito.any());
		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);
		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		Assert.assertTrue(sqsClient.extendVisibilityTimeout(handler, seconds));

		Mockito.verify(client, Mockito.times(1)).changeMessageVisibility((ChangeMessageVisibilityRequest) Mockito.any());

	}

	@Test
	public void extendVisException1() throws BPException {

		expectedException.expect(BPException.class);
		Integer seconds = 33;
		String receipt = "receipt";
		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		QueueDoesNotExistException ex = Mockito.mock(QueueDoesNotExistException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).changeMessageVisibility((ChangeMessageVisibilityRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		sqsClient.extendVisibilityTimeout(handler, seconds);

	}

	@Test
	public void extendVisException2() throws BPException {

		expectedException.expect(BPException.class);
		Integer seconds = 33;
		String receipt = "receipt";

		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		InvalidIdFormatException ex = Mockito.mock(InvalidIdFormatException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).changeMessageVisibility((ChangeMessageVisibilityRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);
		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		sqsClient.extendVisibilityTimeout(handler, seconds);

	}

	@Test
	public void extendVisException3() throws BPException {

		expectedException.expect(BPException.class);
		Integer seconds = 33;
		String receipt = "receipt";
		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		ReceiptHandleIsInvalidException ex = Mockito.mock(ReceiptHandleIsInvalidException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(client).changeMessageVisibility((ChangeMessageVisibilityRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		sqsClient.extendVisibilityTimeout(handler, seconds);

	}

	@Test
	public void extendVisException4() throws BPException {

		expectedException.expect(BPException.class);
		Integer seconds = 33;
		String receipt = "receipt";
		SqsClient client = Mockito.mock(SqsClient.class);

		GetQueueUrlResponse queueurl = Mockito.mock(GetQueueUrlResponse.class);

		Mockito.doReturn(null).when(queueurl).queueUrl();

		Mockito.doReturn(queueurl).when(client).getQueueUrl((GetQueueUrlRequest) Mockito.any());

		Mockito.doThrow(AwsServiceException.class).when(client).changeMessageVisibility((ChangeMessageVisibilityRequest) Mockito.any());

		SQSClient sqsClient = new SQSClient();
		sqsClient.setSQSClient(client);

		QueueMessageHandler handler = new QueueMessageHandler();
		handler.setReceiptHandleId(receipt);

		sqsClient.extendVisibilityTimeout(handler, seconds);

	}

}