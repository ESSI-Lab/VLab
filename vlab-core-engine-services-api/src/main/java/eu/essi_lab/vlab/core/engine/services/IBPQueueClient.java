package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.QueueMessage;
import eu.essi_lab.vlab.core.datamodel.QueueMessageHandler;

/**
 * @author Mattia Santoro
 */
public interface IBPQueueClient extends IBPConfigurableService {



	void quequeExists(Boolean createIfNeeded) throws BPException;

	/**
	 * Sends the message to the queue, returns the message identifier returned by the queue service
	 *
	 * @param message
	 * @return
	 * @throws BPException
	 */
	String add(String message) throws BPException;

	QueueMessage getNextMessage() throws BPException;

	boolean deleteMessage(QueueMessageHandler receipt) throws BPException;

	boolean extendVisibilityTimeout(QueueMessageHandler receipt, Integer seconds) throws BPException;

	String getQueueName();

	Boolean supports(String queueServiceType);
}
