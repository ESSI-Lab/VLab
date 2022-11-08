package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class KubernetesLogParserTest {

	@Test
	public void test() throws IOException, BPException {

		InputStream s = this.getClass().getClassLoader().getResourceAsStream("logchunck.txt");

		String text = IOUtils.toString(s, Charset.forName("UTF-8"));

		KubernetesLogParser parser = new KubernetesLogParser();
		BPLogChunk parsed = parser.parse(text);

		assertEquals(417, parsed.getSet().size());

	}

	@Test
	public void testNull() throws IOException, BPException {

		KubernetesLogParser parser = new KubernetesLogParser();
		BPLogChunk parsed = parser.parse(null);

		assertEquals(0, parsed.getSet().size());

	}

	@Test
	public void testParseTS() throws ParseException {
		KubernetesLogParser parser = new KubernetesLogParser();

		String kubets = "2021-10-15T15:09:58.441430971Z";
		String kubets2 = "2021-10-15T15:10:08.010274086Z";

		assertTrue(parser.parseTimestamp(kubets) < parser.parseTimestamp(kubets2));
	}

	@Test
	public void testParseTS2() throws ParseException {
		KubernetesLogParser parser = new KubernetesLogParser();

		String kubets = "2021-10-15T15:10:04.111311632Z";
		String kubets2 = "2021-10-15T15:10:04.111315713Z";

		assertFalse(parser.parseTimestamp(kubets) < parser.parseTimestamp(kubets2));
		assertTrue(parser.parseNanostamp(kubets) < parser.parseNanostamp(kubets2));
	}

	@Test
	public void testParseTS3() throws ParseException {
		KubernetesLogParser parser = new KubernetesLogParser();

		String kubets = "2021-10-15T15:10:04.111311632Z";
		String kubets2 = "2021-10-15T15:10:04.111615713Z";

		assertFalse(parser.parseTimestamp(kubets) < parser.parseTimestamp(kubets2));
		assertTrue(parser.parseNanostamp(kubets) < parser.parseNanostamp(kubets2));
	}

}