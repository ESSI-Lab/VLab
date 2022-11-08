package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class ValidationMessageTest {

	@Test
	public void test() throws BPException {
		String msg = "Error Message";

		ValidationMessage vm = new ValidationMessage();

		vm.setType(ValidationMessageType.ERROR);
		vm.setMessage(msg);

		String serialized = new JSONSerializer().serialize(vm);

		System.out.println(serialized);

		ValidationMessage deserialized = new JSONDeserializer().deserialize(serialized, ValidationMessage.class);

		Assert.assertEquals(msg, deserialized.getMessage());

		Assert.assertEquals(ValidationMessageType.ERROR, deserialized.getType());
	}

	@Test
	public void test2() throws BPException {
		String msg = "Info Message";

		ValidationMessage vm = new ValidationMessage();

		vm.setType(ValidationMessageType.INFO);
		vm.setMessage(msg);

		String serialized = new JSONSerializer().serialize(vm);

		System.out.println(serialized);

		ValidationMessage deserialized = new JSONDeserializer().deserialize(serialized, ValidationMessage.class);

		Assert.assertEquals(msg, deserialized.getMessage());

		Assert.assertEquals(ValidationMessageType.INFO, deserialized.getType());
	}
}