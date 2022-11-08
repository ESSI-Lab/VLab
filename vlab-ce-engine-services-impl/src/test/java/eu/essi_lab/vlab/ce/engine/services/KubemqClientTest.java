package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.QueueMessage;
import eu.essi_lab.vlab.core.datamodel.QueueMessageHandler;
import io.kubemq.sdk.basic.ServerAddressNotSuppliedException;
import io.kubemq.sdk.grpc.Kubemq;
import io.kubemq.sdk.queue.AckAllMessagesResponse;
import io.kubemq.sdk.queue.Message;
import io.kubemq.sdk.queue.Queue;
import io.kubemq.sdk.queue.SendMessageResult;
import io.kubemq.sdk.queue.Transaction;
import io.kubemq.sdk.queue.TransactionMessagesResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.net.ssl.SSLException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class KubemqClientTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void test1() throws ServerAddressNotSuppliedException, SSLException {

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = new KubemqClient(queue, url);

		client.ackAll();
		AckAllMessagesResponse r = Mockito.mock(AckAllMessagesResponse.class);
		Mockito.doReturn(r).when(queue).AckAllQueueMessages();
		Mockito.verify(queue, Mockito.times(1)).AckAllQueueMessages();

	}

	@Test
	public void test2() throws ServerAddressNotSuppliedException, SSLException, BPException {

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = new KubemqClient(queue, url);

		String body = "body";

		SendMessageResult resultok = Mockito.mock(SendMessageResult.class);

		Mockito.doReturn(Boolean.FALSE).when(resultok).getIsError();

		String mid = "mid";

		Mockito.doReturn(mid).when(resultok).getMessageID();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				Message m = (Message) invocationOnMock.getArguments()[0];

				if (!body.equals(IOUtils.toString(new ByteArrayInputStream(m.getBody()))))
					throw new Exception("Bad message body");

				return resultok;
			}
		}).when(queue).SendQueueMessage(Mockito.any());

		Assert.assertEquals(mid, client.add(body));

	}

	@Test
	public void test3() throws ServerAddressNotSuppliedException, SSLException, BPException {
		String err = "Error from kubemq";
		expectedException.expect(new BPExceptionMatcher(err, BPException.ERROR_CODES.QUEUE_SEND_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = new KubemqClient(queue, url);

		String body = "body";

		SendMessageResult resultfail = Mockito.mock(SendMessageResult.class);

		Mockito.doReturn(Boolean.TRUE).when(resultfail).getIsError();

		Mockito.doReturn(err).when(resultfail).getError();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				Message m = (Message) invocationOnMock.getArguments()[0];

				if (!body.equals(IOUtils.toString(new ByteArrayInputStream(m.getBody()), "UTF-8")))
					throw new Exception("Bad message body");

				return resultfail;
			}
		}).when(queue).SendQueueMessage(Mockito.any());

		client.add(body);

	}

	@Test
	public void test4() throws ServerAddressNotSuppliedException, SSLException, BPException {

		expectedException.expect(new BPExceptionMatcher("Exception sending message to", BPException.ERROR_CODES.QUEUE_SEND_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = new KubemqClient(queue, url);

		String body = "body";

		Mockito.doThrow(SSLException.class).when(queue).SendQueueMessage(Mockito.any());

		client.add(body);

	}

	@Test
	public void test5() throws ServerAddressNotSuppliedException, SSLException, BPException {

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (70 != (Integer) invocationOnMock.getArguments()[0])
					throw new Exception("Bad default time out");

				return null;
			}
		}).when(client).getNextMessage(Mockito.anyInt());

		client.getNextMessage();

	}

	@Test
	public void test6() throws ServerAddressNotSuppliedException, IOException, BPException {

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		TransactionMessagesResponse receiveok = Mockito.mock(TransactionMessagesResponse.class);

		Mockito.doReturn(receiveok).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		Mockito.doReturn(Boolean.FALSE).when(receiveok).getIsError();

		Message message = Mockito.mock(Message.class);

		Mockito.doReturn("received".getBytes()).when(message).getBody();

		Mockito.doReturn(message).when(receiveok).getMessage();

		QueueMessage next = client.getNextMessage(10);

		Assert.assertEquals("received", next.getBody());

		Assert.assertTrue(next.getHandler().getReceiptHandler().get() instanceof Transaction);

	}

	@Test
	public void test7() throws ServerAddressNotSuppliedException, IOException, BPException {

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		TransactionMessagesResponse receiveok = Mockito.mock(TransactionMessagesResponse.class);

		Mockito.doReturn(receiveok).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		Mockito.doReturn(Boolean.TRUE).when(receiveok).getIsError();

		Mockito.doReturn("No Active queue message, visibility expired:false").when(receiveok).getError();

		QueueMessage next = client.getNextMessage(10);

		Assert.assertNull(next);

	}

	@Test
	public void test8() throws ServerAddressNotSuppliedException, IOException, BPException {

		String realerr = "Real error";

		expectedException.expect(new BPExceptionMatcher(realerr, BPException.ERROR_CODES.QUEUE_RECEIVE_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		TransactionMessagesResponse receivefail = Mockito.mock(TransactionMessagesResponse.class);

		Mockito.doReturn(receivefail).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		Mockito.doReturn(Boolean.TRUE).when(receivefail).getIsError();

		Mockito.doReturn(realerr).when(receivefail).getError();

		client.getNextMessage(10);

	}

	@Test
	public void test9() throws ServerAddressNotSuppliedException, IOException, BPException {

		expectedException.expect(
				new BPExceptionMatcher("Exception reading message from kubemq", BPException.ERROR_CODES.QUEUE_RECEIVE_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		Mockito.doThrow(IOException.class).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		client.getNextMessage(10);

	}

	@Test
	public void test10() throws ServerAddressNotSuppliedException, IOException, BPException {

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);
		Kubemq.QueueMessage qm = Mockito.mock(Kubemq.QueueMessage.class);
		String messageid = "messageid";
		Mockito.doReturn(messageid).when(qm).getMessageID();
		Mockito.doReturn(qm).when(tran).getCurrentHandledMessage();

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		Mockito.doThrow(IOException.class).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		TransactionMessagesResponse receiveok = Mockito.mock(TransactionMessagesResponse.class);

		Mockito.doReturn(receiveok).when(tran).AckMessage();
		Assert.assertTrue(client.deleteMessage(receipt));

	}

	@Test
	public void test10_1() throws ServerAddressNotSuppliedException, IOException, BPException {

		String err = "error from server";

		expectedException.expect(new BPExceptionMatcher(err, BPException.ERROR_CODES.QUEUE_DELETE_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);
		Kubemq.QueueMessage qm = Mockito.mock(Kubemq.QueueMessage.class);
		String messageid = "messageid";
		Mockito.doReturn(messageid).when(qm).getMessageID();
		Mockito.doReturn(qm).when(tran).getCurrentHandledMessage();

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		Mockito.doThrow(IOException.class).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		TransactionMessagesResponse receivefail = Mockito.mock(TransactionMessagesResponse.class);

		Mockito.doReturn(Boolean.TRUE).when(receivefail).getIsError();

		Mockito.doReturn(err).when(receivefail).getError();
		Mockito.doReturn(receivefail).when(tran).AckMessage();

		client.deleteMessage(receipt);

	}

	@Test
	public void test11() throws ServerAddressNotSuppliedException, IOException, BPException {

		expectedException.expect(new BPExceptionMatcher("Error deleting message ", BPException.ERROR_CODES.QUEUE_DELETE_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);
		Kubemq.QueueMessage qm = Mockito.mock(Kubemq.QueueMessage.class);
		String messageid = "messageid";
		Mockito.doReturn(messageid).when(qm).getMessageID();
		Mockito.doReturn(qm).when(tran).getCurrentHandledMessage();

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		Mockito.doThrow(IOException.class).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		Mockito.doThrow(IOException.class).when(tran).AckMessage();
		Assert.assertTrue(client.deleteMessage(receipt));

	}

	@Test
	public void test12() throws ServerAddressNotSuppliedException, IOException, BPException {

		expectedException.expect(new BPExceptionMatcher("Receipt handler found is of wrong type ",
				BPException.ERROR_CODES.BAD_QUEUE_MESSAGE_RECIPT_HANDLER));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Object tran = Mockito.mock(Object.class);

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		client.deleteMessage(receipt);

	}

	@Test
	public void test14() throws ServerAddressNotSuppliedException, IOException, BPException {

		expectedException.expect(new BPExceptionMatcher("No receipt transaction found for message delete",
				BPException.ERROR_CODES.BAD_QUEUE_MESSAGE_RECIPT_HANDLER));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));

		QueueMessageHandler receipt = new QueueMessageHandler();

		client.deleteMessage(receipt);

	}

	@Test
	public void test15() throws ServerAddressNotSuppliedException, IOException, BPException {

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);
		Kubemq.QueueMessage qm = Mockito.mock(Kubemq.QueueMessage.class);
		String messageid = "messageid";
		Mockito.doReturn(messageid).when(qm).getMessageID();
		Mockito.doReturn(qm).when(tran).getCurrentHandledMessage();

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		Mockito.doThrow(IOException.class).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		TransactionMessagesResponse receiveok = Mockito.mock(TransactionMessagesResponse.class);
		Integer seconds = 3;
		Mockito.doReturn(receiveok).when(client).doExtendVisibility(Mockito.any(), Mockito.eq(seconds));

		Assert.assertTrue(client.extendVisibilityTimeout(receipt, seconds));

	}

	@Test
	public void test15_1() throws ServerAddressNotSuppliedException, IOException, BPException {

		String err = "error from server";

		expectedException.expect(new BPExceptionMatcher(err, BPException.ERROR_CODES.QUEUE_EXTEND_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);
		Kubemq.QueueMessage qm = Mockito.mock(Kubemq.QueueMessage.class);
		String messageid = "messageid";
		Mockito.doReturn(messageid).when(qm).getMessageID();
		Mockito.doReturn(qm).when(tran).getCurrentHandledMessage();

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		Mockito.doThrow(IOException.class).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		TransactionMessagesResponse receivefail = Mockito.mock(TransactionMessagesResponse.class);

		Mockito.doReturn(Boolean.TRUE).when(receivefail).getIsError();

		Mockito.doReturn(err).when(receivefail).getError();

		Integer seconds = 3;
		Mockito.doReturn(receivefail).when(client).doExtendVisibility(Mockito.any(), Mockito.eq(seconds));

		client.extendVisibilityTimeout(receipt, seconds);

	}

	@Test
	public void test16() throws ServerAddressNotSuppliedException, IOException, BPException {

		expectedException.expect(
				new BPExceptionMatcher("Error extending timeout of message", BPException.ERROR_CODES.QUEUE_EXTEND_MESSAGE_ERROR));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Transaction tran = Mockito.mock(Transaction.class);
		Kubemq.QueueMessage qm = Mockito.mock(Kubemq.QueueMessage.class);
		String messageid = "messageid";
		Mockito.doReturn(messageid).when(qm).getMessageID();
		Mockito.doReturn(qm).when(tran).getCurrentHandledMessage();

		Mockito.doReturn(tran).when(queue).CreateTransaction();

		Mockito.doThrow(IOException.class).when(tran).Receive(Mockito.anyInt(), Mockito.anyInt());

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		Integer seconds = 3;
		Mockito.doThrow(IOException.class).when(client).doExtendVisibility(Mockito.any(), Mockito.eq(seconds));

		client.extendVisibilityTimeout(receipt, seconds);

	}

	@Test
	public void test18() throws ServerAddressNotSuppliedException, IOException, BPException {

		expectedException.expect(new BPExceptionMatcher("Receipt handler found is of wrong type ",
				BPException.ERROR_CODES.BAD_QUEUE_MESSAGE_RECIPT_HANDLER));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));
		Object tran = Mockito.mock(Object.class);

		QueueMessageHandler receipt = new QueueMessageHandler();
		receipt.setReceiptHandler(tran);

		client.extendVisibilityTimeout(receipt, 3);

	}

	@Test
	public void test19() throws ServerAddressNotSuppliedException, IOException, BPException {

		expectedException.expect(new BPExceptionMatcher("No receipt transaction found for message visibility timeout extension",
				BPException.ERROR_CODES.BAD_QUEUE_MESSAGE_RECIPT_HANDLER));

		Queue queue = Mockito.mock(Queue.class);
		String url = "example.com";
		KubemqClient client = Mockito.spy(new KubemqClient(queue, url));

		QueueMessageHandler receipt = new QueueMessageHandler();

		client.extendVisibilityTimeout(receipt, 9);

	}

}