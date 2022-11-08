package eu.essi_lab.vlab.web.api.exception;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPHttpErrorMessageTest {

	@Test
	public void serialize() throws BPException {

		BPHttpErrorMessage message = new BPHttpErrorMessage();

		message.setHttpStatusCode(403);

		message.setCode(BPException.ERROR_CODES.NOT_AUTHORIZED.getCode());

		message.setDevError("dev error");

		message.setError("user error");

		System.out.println(new JSONSerializer().serialize(message));

	}
}