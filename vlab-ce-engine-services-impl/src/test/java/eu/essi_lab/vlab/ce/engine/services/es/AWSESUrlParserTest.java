package eu.essi_lab.vlab.ce.engine.services.es;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class AWSESUrlParserTest {

	@Test
	public void test() {

		String url = "http://example.com";

		assertFalse(new AWSESUrlParser(url).isAWSESEndpoint());

	}

	@Test
	public void test2() {

		String url = "https://search-test-nimt6nd7mfghjk54.us-east-1.es.amazonaws.com";

		assertTrue(new AWSESUrlParser(url).isAWSESEndpoint());

	}

	@Test
	public void test3() {

		String url = "http://example.:::com";

		assertFalse(new AWSESUrlParser(url).isAWSESEndpoint());

	}

	@Test
	public void test4() {

		String url = "https://search-test-nimt6nd7mfghjk54.us-east-1.es.amazonaws.com";

		assertEquals("test", new AWSESUrlParser(url).getDomainName());

	}


	@Test
	public void test5() {

		String url = "https://search-test-nimt6nd7mfghjk54.us-east-1.es.amazonaws.com";

		assertEquals("us-east-1", new AWSESUrlParser(url).getRegion());

	}

	@Test
	public void test6() {

		String url = "https://search-test-nimt6nd7mfghjk54.us-east-1.es.amazonaws.com/";

		assertEquals("us-east-1", new AWSESUrlParser(url).getRegion());

	}
}