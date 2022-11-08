package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPRunStatusTest {

	@Test
	public void test() throws BPException, ParseException {

		BPRunStatus st = new BPRunStatus();

		st.setMessage("message");

		System.out.println(st.getMessage());

		String serialized = new JSONSerializer().serialize(st);

		BPRunStatus deserialized = new JSONDeserializer().deserialize(serialized, BPRunStatus.class);

		String msg = deserialized.getMessage();

		System.out.println(msg);

		String sub = msg.substring(msg.indexOf("[") + 1, msg.indexOf("]"));

		System.out.println(sub);

		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");

		Date date = sdf.parse(sub);

		String nodate = msg.replace("[" + sub + "]", "");

		assertFalse(st.hasDate(nodate));

	}

	@Test
	public void hasDateTest() {
		BPRunStatus st = new BPRunStatus();

		assertFalse(st.hasDate("msg [id:test]"));

		assertFalse(st.hasDate("msg"));

		assertFalse(st.hasDate("msg [id:test"));

		assertFalse(st.hasDate("msg id:test]"));

		assertTrue(st.hasDate("[2017-09-06T13:23:51+0000] test msg"));

	}

}