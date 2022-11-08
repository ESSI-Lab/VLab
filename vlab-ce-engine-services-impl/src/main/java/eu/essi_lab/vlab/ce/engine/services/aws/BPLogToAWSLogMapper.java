package eu.essi_lab.vlab.ce.engine.services.aws;

import eu.essi_lab.vlab.core.datamodel.BPLog;
import java.util.function.Function;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;

/**
 * @author Mattia Santoro
 */
public class BPLogToAWSLogMapper implements Function<BPLog, InputLogEvent> {

	@Override
	public InputLogEvent apply(BPLog bpLog) {

		return InputLogEvent.builder().message(bpLog.getMessage()).timestamp(timeStampMillis(bpLog)).build();

	}

	private Long timeStampMillis(BPLog bpLog) {

		String nanos = bpLog.getNanostamp().toString();

		Long millis = Long.valueOf(nanos.substring(0, Math.min(3, nanos.length())));

		if (nanos.length() > 3 && Integer.valueOf(nanos.substring(3, 4)) > 5)
			millis++;

		return bpLog.getTimestamp() + millis;

	}
}
