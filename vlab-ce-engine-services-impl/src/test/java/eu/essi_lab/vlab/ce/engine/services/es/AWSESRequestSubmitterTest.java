package eu.essi_lab.vlab.ce.engine.services.es;

import org.apache.http.client.HttpClient;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class AWSESRequestSubmitterTest {

	@Test
	public void test() {

		AWSESRequestSubmitter submitter = new AWSESRequestSubmitter();

		submitter.setUser("user");
		submitter.setPwd("p");

		HttpClient client = submitter.authenticatedClient();

	}
}