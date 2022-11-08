package eu.essi_lab.vlab.core.datamodel;

import java.util.Optional;

/**
 * @author Mattia Santoro
 */
public class QueueMessageHandler {

	private Optional<String> receiptHandleId = Optional.empty();

	private Optional<Object> receiptHandler = Optional.empty();

	public void setReceiptHandleId(String receiptHandleId) {
		this.receiptHandleId = Optional.of(receiptHandleId);
	}

	public Optional<String> getReceiptHandleId() {
		return receiptHandleId;
	}

	public Optional<Object> getReceiptHandler() {
		return receiptHandler;
	}

	public void setReceiptHandler(Object receiptHandler) {
		this.receiptHandler = Optional.of(receiptHandler);
	}

	@Override
	public String toString() {

		if (receiptHandleId.isPresent())
			return "Receipt id: " + receiptHandleId.get();

		if (receiptHandler.isPresent())
			return "Receipt object: " + receiptHandler.get();

		return "No Handler found";

	}
}
