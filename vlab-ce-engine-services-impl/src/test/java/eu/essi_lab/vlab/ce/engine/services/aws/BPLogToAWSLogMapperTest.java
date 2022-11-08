package eu.essi_lab.vlab.ce.engine.services.aws;

import eu.essi_lab.vlab.core.datamodel.BPLog;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;

/**
 * @author Mattia Santoro
 */
public class BPLogToAWSLogMapperTest {

	@Test
	public void test() {
		BPLogToAWSLogMapper mapper = new BPLogToAWSLogMapper();

		Long nano = 1120000000000L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);
		InputLogEvent l = mapper.apply(log);

		assertEquals(msg, l.message());
		assertEquals((Long) milli + 112, (long) l.timestamp());
	}


	@Test
	public void test2() {
		BPLogToAWSLogMapper mapper = new BPLogToAWSLogMapper();

		Long nano = 1120L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);
		InputLogEvent l = mapper.apply(log);

		assertEquals(msg, l.message());
		assertEquals((Long) milli + 112, (long) l.timestamp());
	}

	@Test
	public void test3() {
		BPLogToAWSLogMapper mapper = new BPLogToAWSLogMapper();

		Long nano = 112L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);
		InputLogEvent l = mapper.apply(log);

		assertEquals(msg, l.message());
		assertEquals((Long) milli + 112, (long) l.timestamp());
	}


	@Test
	public void test4() {
		BPLogToAWSLogMapper mapper = new BPLogToAWSLogMapper();

		Long nano = 2L;
		Long milli = 1634303408000L;
		BPLog log = new BPLog(milli, nano);
		String msg = "Test message";
		log.setMessage(msg);
		InputLogEvent l = mapper.apply(log);

		assertEquals(msg, l.message());
		assertEquals((Long) milli + 2, (long) l.timestamp());
	}

}