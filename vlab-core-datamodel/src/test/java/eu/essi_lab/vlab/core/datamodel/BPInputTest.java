package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.util.Arrays;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPInputTest {

	@Test
	public void test() throws BPException {

		BPInput input = new BPInput();

		input.setDescription("Test description");

		input.setObligation(false);

		input.setValueArray(Arrays.asList(new String[] { "ff", "f" }));

		System.out.println(new JSONSerializer().serialize(input));

	}
}