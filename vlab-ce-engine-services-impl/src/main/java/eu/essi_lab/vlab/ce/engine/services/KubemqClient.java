package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.QueueMessage;
import eu.essi_lab.vlab.core.datamodel.QueueMessageHandler;
import eu.essi_lab.vlab.core.engine.services.IBPQueueClient;
import io.kubemq.sdk.basic.ServerAddressNotSuppliedException;
import io.kubemq.sdk.queue.Message;
import io.kubemq.sdk.queue.Queue;
import io.kubemq.sdk.queue.SendMessageResult;
import io.kubemq.sdk.queue.Transaction;
import io.kubemq.sdk.queue.TransactionMessagesResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubemqClient implements IBPQueueClient {
	private static final String KUBEMQ_SERVICE_TYPE = "kubemq";
	private Queue queue;
	private Logger logger = LogManager.getLogger(KubemqClient.class);
	private String kubemqurl;
	private static final Integer VISIBILITY_SECONDS = 70;
	private static final Integer WAIT_SECONDS = 10;

	public KubemqClient() {
	}

	@Override
	public Boolean supports(String queueServiceType) {
		return KUBEMQ_SERVICE_TYPE.equalsIgnoreCase(queueServiceType);
	}

	@Override
	public List<ConfigurationParameter> configurationParameters() {
		return Arrays.asList(BPStaticConfigurationParameters.AWS_SQS_BP_RUN_QUEQUE_NAME.getParameter(),
				BPStaticConfigurationParameters.BP_RUN_QUEQUE_KUBEMQ_CLIENTID.getParameter(),
				BPStaticConfigurationParameters.BP_RUN_QUEQUE_SERVER_URL.getParameter());
	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) throws BPException {

		String name = parameters.get(BPStaticConfigurationParameters.AWS_SQS_BP_RUN_QUEQUE_NAME.getParameter());

		String clientid = parameters.get(BPStaticConfigurationParameters.BP_RUN_QUEQUE_KUBEMQ_CLIENTID.getParameter());
		kubemqurl = parameters.get(BPStaticConfigurationParameters.BP_RUN_QUEQUE_SERVER_URL.getParameter());

		try {
			this.setKMQClient(new Queue(name, clientid, kubemqurl));

		} catch (SSLException | ServerAddressNotSuppliedException e) {

			logger.error("Error connecting to kubemq at {} with queue name {}", kubemqurl, name, e);

			String bpMsg = "Error connecting to kubemq at " + kubemqurl + " with queue name " + name + " (" + e.getMessage() + ")";

			throw new BPException(bpMsg, BPException.ERROR_CODES.AWS_SQS_INIT_ERROR);

		}

	}

	public void setKMQClient(Queue cli) {
		this.queue = cli;
	}

	public KubemqClient(String queueName, String clientid, String url) throws ServerAddressNotSuppliedException, SSLException {

		this(new Queue(queueName, clientid, url), url);

	}

	public KubemqClient(Queue q, String url) {
		kubemqurl = url;
		queue = q;
	}

	public void ackAll() throws ServerAddressNotSuppliedException, SSLException {
		queue.AckAllQueueMessages();
	}

	@Override
	public void quequeExists(Boolean createIfneeded) throws BPException {

		//TODO

	}

	@Override
	public String add(String message) throws BPException {

		String err = "";

		Message queueMessage = new Message();

		queueMessage.setBody(message.getBytes(StandardCharsets.UTF_8));

		try {
			logger.trace("Sending message to queque {}", queue.getQueueName());

			SendMessageResult result = queue.SendQueueMessage(queueMessage);

			if (Boolean.TRUE.equals(result.getIsError())) {
				err = "Error sending message to " + queue.getQueueName() + " at " + kubemqurl + " (" + result.getError() + ")";

				logger.warn(err);
			} else {

				String messageid = result.getMessageID();

				logger.trace("Sending message to queque {} success with message id {}", queue.getQueueName(), messageid);

				return messageid;
			}

		} catch (SSLException | ServerAddressNotSuppliedException e) {
			err = "Exception sending message to " + queue.getQueueName() + " at " + kubemqurl;

			logger.warn(err, e);

		}

		throw new BPException(err, BPException.ERROR_CODES.QUEUE_SEND_MESSAGE_ERROR);

	}

	@Override
	public QueueMessage getNextMessage() throws BPException {
		return getNextMessage(VISIBILITY_SECONDS);
	}

	public QueueMessage getNextMessage(Integer to) throws BPException {

		try {

			logger.info("Fetching next message from {}", queue.getQueueName());

			Transaction tran = queue.CreateTransaction();

			TransactionMessagesResponse receive = tran.Receive(to, WAIT_SECONDS);

			if (Boolean.TRUE.equals(receive.getIsError())) {

				String kubeErr = receive.getError();

				logger.trace("Transaction returned error: {}", kubeErr);

				if (isEmptyErr(kubeErr))
					return null;

				String err = "Exception reading message from kubemq " + kubeErr;

				throw new BPException(err, BPException.ERROR_CODES.QUEUE_RECEIVE_MESSAGE_ERROR);
			}

			logger.trace("Received message");

			String body = new String(new ByteArrayInputStream(receive.getMessage().getBody()).readAllBytes());

			QueueMessage queueMessage = new QueueMessage();
			queueMessage.setBody(body);

			logger.trace("Body set in QueueMessage");

			QueueMessageHandler handler = new QueueMessageHandler();
			handler.setReceiptHandler(tran);

			queueMessage.setHandler(handler);

			return queueMessage;

		} catch (ServerAddressNotSuppliedException | IOException e) {

			logger.error("Can't read message from {} at {}", queue.getQueueName(), kubemqurl, e);

			String err = "Exception reading message from kubemq " + e.getMessage();

			throw new BPException(err, BPException.ERROR_CODES.QUEUE_RECEIVE_MESSAGE_ERROR);
		}

	}

	boolean isEmptyErr(String kubeErr) {

		return "No Active queue message, visibility expired:false".equalsIgnoreCase(kubeErr)
				|| "Error 138: no new message in queue, wait time expired".equalsIgnoreCase(kubeErr);

	}

	private Transaction getHandleIdOrThrowException(QueueMessageHandler receipt, String exmsg) throws BPException {

		Optional<Object> handler = receipt.getReceiptHandler();

		if (!handler.isPresent()) {

			logger.error(exmsg);
			throw new BPException(exmsg, BPException.ERROR_CODES.BAD_QUEUE_MESSAGE_RECIPT_HANDLER);
		}

		Object o = handler.get();

		if (!(o instanceof Transaction)) {
			String exmsgInstance = "Receipt handler found is of wrong type (" + o.getClass().getName() + ")";
			logger.error(exmsgInstance);
			throw new BPException(exmsgInstance, BPException.ERROR_CODES.BAD_QUEUE_MESSAGE_RECIPT_HANDLER);

		}

		return (Transaction) o;
	}

	@Override
	public boolean deleteMessage(QueueMessageHandler receipt) throws BPException {

		Transaction tran = getHandleIdOrThrowException(receipt, "No receipt transaction found for message delete");
		try {

			logger.trace("Deleting {}", tran.getCurrentHandledMessage().getMessageID());

			TransactionMessagesResponse response = tran.AckMessage();

			if (Boolean.TRUE.equals(response.getIsError())) {

				logger.error("Error deleting message from kubemq: {}", response.getError());

				throw new BPException("Error deleting message " + response.getError(), BPException.ERROR_CODES.QUEUE_DELETE_MESSAGE_ERROR);
			}

			return true;

		} catch (ServerAddressNotSuppliedException | IOException e) {

			logger.error("Can't delete message from queue {} at {}", queue.getQueueName(), kubemqurl, e);

			String bpMsg = "Error deleting message " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.QUEUE_DELETE_MESSAGE_ERROR);
		}

	}

	TransactionMessagesResponse doExtendVisibility(Transaction tran, Integer seconds)
			throws ServerAddressNotSuppliedException, IOException {
		return tran.ExtendVisibility(seconds);
	}

	@Override
	public boolean extendVisibilityTimeout(QueueMessageHandler receipt, Integer seconds) throws BPException {

		Transaction tran = getHandleIdOrThrowException(receipt, "No receipt transaction found for message visibility timeout extension");

		try {

			logger.trace("Extending timeout of {}", tran.getCurrentHandledMessage().getMessageID());

			TransactionMessagesResponse response = doExtendVisibility(tran, seconds);

			if (Boolean.TRUE.equals(response.getIsError())) {

				logger.error("Error extending timeout of message from kubemq: {}", response.getError());

				throw new BPException("Error extending timeout of message " + response.getError(),
						BPException.ERROR_CODES.QUEUE_EXTEND_MESSAGE_ERROR);
			}

			return true;

		} catch (ServerAddressNotSuppliedException | IOException e) {

			logger.error("Can't extend visibility timeout of message from queue {} at {}", queue.getQueueName(), kubemqurl, e);

			String bpMsg = "Error extending timeout of message " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.QUEUE_EXTEND_MESSAGE_ERROR);
		}

	}

	@Override
	public String getQueueName() {
		return queue.getQueueName();
	}
}
