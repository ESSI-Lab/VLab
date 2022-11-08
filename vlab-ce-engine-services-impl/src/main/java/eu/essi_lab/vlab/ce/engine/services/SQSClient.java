package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.QueueMessage;
import eu.essi_lab.vlab.core.datamodel.QueueMessageHandler;
import eu.essi_lab.vlab.core.engine.services.IBPQueueClient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.InvalidIdFormatException;
import software.amazon.awssdk.services.sqs.model.InvalidMessageContentsException;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.OverLimitException;
import software.amazon.awssdk.services.sqs.model.QueueDeletedRecentlyException;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import software.amazon.awssdk.services.sqs.model.ReceiptHandleIsInvalidException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * @author Mattia Santoro
 */
public class SQSClient implements IBPQueueClient {

	private String sqsName;

	private SqsClient client;

	private Logger logger = LogManager.getLogger(SQSClient.class);
	private static final String PAR_CODE = " (code ";

	public SQSClient() {
	}

	@Override
	public Boolean supports(String queueServiceType) {
		return "sqs".equalsIgnoreCase(queueServiceType);
	}

	@Override
	public List<ConfigurationParameter> configurationParameters() {
		return Arrays.asList(BPStaticConfigurationParameters.AWS_SQS_BP_RUN_QUEQUE_NAME.getParameter(),
				BPStaticConfigurationParameters.AWS_SQS_ACCESS_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_SQS_SECRET_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_SQS_REGION.getParameter());
	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) {

		String region = parameters.get(BPStaticConfigurationParameters.AWS_SQS_REGION.getParameter());
		String accessKey = parameters.get(BPStaticConfigurationParameters.AWS_SQS_ACCESS_KEY.getParameter());
		String secretKey = parameters.get(BPStaticConfigurationParameters.AWS_SQS_SECRET_KEY.getParameter());

		this.setSQSClient(SqsClient.builder().region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))).build());
		this.setQueueName(parameters.get(BPStaticConfigurationParameters.AWS_SQS_BP_RUN_QUEQUE_NAME.getParameter()));
	}

	public void setQueueName(String name) {
		this.sqsName = name;
	}

	public void setSQSClient(SqsClient cli) {
		this.client = cli;
	}



	@Override
	public void quequeExists(Boolean createIfNeeded) throws BPException {

		checkQueque(createIfNeeded);
	}

	private boolean checkQueque(boolean create) throws BPException {

		String err;

		try {

			client.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsName).build());

			return true;

		} catch (QueueDoesNotExistException e) {

			logger.info("Required queque {} does not exist", sqsName, e);

		} catch (SdkClientException serviceEx) {

			logger.warn("Required queque {} can not be found because of a service exception", sqsName, serviceEx);

			if (create)
				throw serviceEx;

		}

		if (!create)
			return false;

		try {

			client.createQueue(CreateQueueRequest.builder().queueName(sqsName).build());

			logger.info("Successfully created Queque {}", sqsName);

			return true;

		} catch (QueueDeletedRecentlyException | QueueNameExistsException e) {

			err = sqsName + " can not be created (error code " + e.awsErrorDetails().errorCode() + "): " + e.getMessage();

			logger.error(err, e);

		}

		throw new BPException(err, BPException.ERROR_CODES.AWS_SQS_INIT_ERROR);
	}

	@Override
	public String add(String message) throws BPException {

		String err;

		try {

			logger.trace("Sending message to queque {}", sqsName);

			SendMessageResponse response = client.sendMessage(SendMessageRequest.builder().messageBody(message)
					.queueUrl(client.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsName).build()).queueUrl()).build());

			logger.trace("Message sent to queque {} [message id: {}]", sqsName, response.messageId());

			return response.messageId();

		} catch (QueueDoesNotExistException | InvalidMessageContentsException e) {

			err = "Exception sending message to queque " + sqsName + PAR_CODE + e.awsErrorDetails().errorCode() + "): " + e.getMessage();

			logger.warn(err);

		} catch (UnsupportedOperationException | AwsServiceException e) {

			err = "Exception sending message to queque " + sqsName + ": " + e.getMessage();

			logger.warn(err);

		}

		throw new BPException(err, BPException.ERROR_CODES.QUEUE_SEND_MESSAGE_ERROR);
	}

	@Override
	public QueueMessage getNextMessage() throws BPException {

		String err;

		try {

			logger.trace("Receiving message from queque {}", sqsName);

			ReceiveMessageResponse response = client.receiveMessage(ReceiveMessageRequest.builder()

					.queueUrl(client.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsName).build()).queueUrl()).maxNumberOfMessages(1)

					.build());

			if (response.messages().isEmpty()) {

				logger.trace("Received 0 messages from queque {}", sqsName);

				return null;

			}

			logger.trace("Received 1 message from queque {}", sqsName);

			QueueMessage queueMessage = new QueueMessage();

			Message awsmessage = response.messages().get(0);

			queueMessage.setBody(awsmessage.body());

			QueueMessageHandler handler = new QueueMessageHandler();

			handler.setReceiptHandleId(awsmessage.receiptHandle());

			queueMessage.setHandler(handler);

			return queueMessage;

		} catch (QueueDoesNotExistException | OverLimitException e) {

			err = "Exception receiving message from queque " + sqsName + PAR_CODE + e.awsErrorDetails().errorCode() + "): "
					+ e.getMessage();

			logger.warn(err);

		} catch (AwsServiceException e) {

			err = "Exception receiving message from queque " + sqsName + ": " + e.getMessage();

			logger.warn(err);

		}

		throw new BPException(err, BPException.ERROR_CODES.QUEUE_RECEIVE_MESSAGE_ERROR);
	}

	private String getHandleIdOrThrowException(QueueMessageHandler receipt, String exmsg) throws BPException {

		Optional<String> handleid = receipt.getReceiptHandleId();

		if (!handleid.isPresent()) {

			logger.error(exmsg);
			throw new BPException(exmsg, BPException.ERROR_CODES.BAD_QUEUE_MESSAGE_RECIPT_HANDLER);
		}

		return handleid.get();
	}

	@Override
	public boolean deleteMessage(QueueMessageHandler receipt) throws BPException {

		String err;

		String msgid = getHandleIdOrThrowException(receipt, "No receipt text found for message delete");

		try {

			logger.trace("Deleting message from queque {} with receipt {}", sqsName, receipt);

			client.deleteMessage(DeleteMessageRequest.builder().queueUrl(
					client.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsName).build()).queueUrl()).receiptHandle(msgid).build());

			logger.trace("Deleted message from queque {} with receipt {}", sqsName, receipt);

			return true;

		} catch (QueueDoesNotExistException | InvalidIdFormatException | ReceiptHandleIsInvalidException e) {

			err = "Exception deleting message from queque " + sqsName + PAR_CODE + e.awsErrorDetails().errorCode() + "): " + e.getMessage();

			logger.warn(err);

		} catch (AwsServiceException e) {

			err = "Exception deleting message from queque " + sqsName + ": " + e.getMessage();

			logger.warn(err);

		}

		throw new BPException(err, BPException.ERROR_CODES.QUEUE_DELETE_MESSAGE_ERROR);

	}

	@Override
	public boolean extendVisibilityTimeout(QueueMessageHandler receipt, Integer seconds) throws BPException {
		String err;

		String msgid = getHandleIdOrThrowException(receipt, "No receipt text found for message visibility extension");

		try {

			logger.trace("Extending visibility timeout of message from queque {} with receipt {}", sqsName, receipt);

			client.changeMessageVisibility(ChangeMessageVisibilityRequest.builder().queueUrl(
							client.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsName).build()).queueUrl()).receiptHandle(msgid)
					.visibilityTimeout(seconds).build());

			logger.trace("Extended visibility timeout of message from queque {} with receipt {}", sqsName, receipt);

			return true;

		} catch (QueueDoesNotExistException | InvalidIdFormatException | ReceiptHandleIsInvalidException e) {

			err = "Exception extending visibility timeout of message from queque " + sqsName + PAR_CODE + e.awsErrorDetails().errorCode()
					+ "): " + e.getMessage();

			logger.warn(err);

		} catch (AwsServiceException e) {

			err = "Exception extending visibility timeout of message from queque " + sqsName + ": " + e.getMessage();

			logger.warn(err);

		}

		throw new BPException(err, BPException.ERROR_CODES.QUEUE_EXTEND_MESSAGE_ERROR);
	}

	@Override
	public String getQueueName() {
		return sqsName;
	}

}
