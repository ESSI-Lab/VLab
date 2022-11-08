package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class QueueMessage {

	private String body;

	private QueueMessageHandler handler;

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public QueueMessageHandler getHandler() {
		return handler;
	}

	public void setHandler(QueueMessageHandler handler) {
		this.handler = handler;
	}
}
