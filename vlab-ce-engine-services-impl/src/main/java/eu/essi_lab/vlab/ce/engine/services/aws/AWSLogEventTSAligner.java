package eu.essi_lab.vlab.ce.engine.services.aws;

import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;

/**
 * @author Mattia Santoro
 */
public class AWSLogEventTSAligner implements java.util.function.Function<InputLogEvent, InputLogEvent> {

	private Long ts;

	public AWSLogEventTSAligner(Long initialTS) {
		ts = initialTS;
	}

	@Override
	public InputLogEvent apply(InputLogEvent inputLogEvent) {

		Long lastTS = getTs();

		if (lastTS >= inputLogEvent.timestamp())
			inputLogEvent = inputLogEvent.toBuilder().timestamp(lastTS + 1L).build();

		setTs(inputLogEvent.timestamp());

		return inputLogEvent;
	}

	public synchronized Long getTs() {
		return ts;
	}

	public synchronized void setTs(Long ts) {
		this.ts = ts;
	}
}
