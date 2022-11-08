package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;

import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class ValidateRealizationResponseTest {

	@Test
	public void test() throws BPException {
		String msg = "Error Message";

		ValidationMessage vm = new ValidationMessage();

		vm.setType(ValidationMessageType.ERROR);
		vm.setMessage(msg);

		ValidateRealizationResponse response = new ValidateRealizationResponse();

		response.setValid(false);

		String bpmn = "http://bpmn.com";

		APIWorkflowDetail wf = new APIWorkflowDetail();
		wf.setBpmn_url(bpmn);

		response.setWorkflow(wf);

		response.setMessages(Arrays.asList(new ValidationMessage[] { vm }));

		String serialized = new JSONSerializer().serialize(response);

		System.out.println(serialized);

		ValidateRealizationResponse deserialized = new JSONDeserializer().deserialize(serialized, ValidateRealizationResponse.class);

		Assert.assertEquals(msg, deserialized.getMessages().get(0).getMessage());

		Assert.assertEquals(ValidationMessageType.ERROR, deserialized.getMessages().get(0).getType());

		Assert.assertFalse("Expected not valid", response.getValid());

		Assert.assertEquals(bpmn, response.getWorkflow().getBpmn_url());
	}

}