package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPDefaultInputObjectTest {

	private void verify(BPIOObject obj) {
		List<BPInputDescription> inputs = obj.getInputs();

		assertEquals(2, inputs.size());

		String target0 = inputs.get(0).getTarget();

		assertEquals("P1/Classification/Clumps.kea", target0);

		assertEquals("Clumps", obj.getInputs().get(0).getInput().getName());

		List<Object> valAr = inputs.get(1).getDefaultValueArray();

		List<String> array = new ArrayList<>();

		array.add("sftp://test.example.it/vlab/P1/Thematic/*");

		assertEquals(array, valAr);

		assertEquals(1, obj.getOutputs().size());

		assertEquals("P1/Classification/Clumps.kea", obj.getOutputs().get(0).getTarget());

	}

	@Test
	public void testDeserialize() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test_iodescription.json");

		assertNotNull(stream);

		BPIOObject obj = new JSONDeserializer().deserialize(stream, BPIOObject.class);

		verify(obj);

		String serialized = new JSONSerializer().serialize(obj);

		assertEquals(
				"{\"inputs\":[{\"valueType\":\"value\",\"description\":\"Clumps description\",\"name\":\"Clumps\",\"id\":\"DataObject_1wb6a70\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\",\"obligation\":true,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null,\"target\":\"P1/Classification/Clumps.kea\",\"defaultValue\":\"sftp://test.example.it/vlab/P1/Classification/Clumps.kea\",\"defaultValueArray\":null},{\"valueType\":\"value\",\"description\":\"Thematic Layers description\",\"name\":\"Thematic Layers\",\"id\":\"DataObject_1ogs25p\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\",\"obligation\":true,\"hasDefault\":true,\"inputType\":\"array\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null,\"target\":\"P1/Thematic/\",\"defaultValue\":null,\"defaultValueArray\":[\"sftp://test.example.it/vlab/P1/Thematic/*\"]}],\"outputs\":[{\"valueType\":\"value\",\"description\":null,\"name\":\"Output\",\"id\":\"DataObject_0loj2kk\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\",\"outputType\":\"individual\",\"createFolder\":true,\"target\":\"P1/Classification/Clumps.kea\"}]}",
				serialized);

		verify(new JSONDeserializer().deserialize(serialized, BPIOObject.class));
	}

	@Test
	public void testDeserializeNew() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test_iodescription_newdm.json");

		assertNotNull(stream);

		BPIOObject obj = new JSONDeserializer().deserialize(stream, BPIOObject.class);

		verify(obj);
		String serialized = new JSONSerializer().serialize(obj);
		System.out.println(serialized);
		assertEquals(
				"{\"inputs\":[{\"valueType\":\"value\",\"description\":\"Clumps description\",\"name\":\"Clumps\",\"id\":\"DataObject_1wb6a70\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\",\"obligation\":true,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null,\"target\":\"P1/Classification/Clumps.kea\",\"defaultValue\":\"sftp://test.example.it/vlab/P1/Classification/Clumps.kea\",\"defaultValueArray\":null},{\"valueType\":\"value\",\"description\":\"Thematic Layers description\",\"name\":\"Thematic Layers\",\"id\":\"DataObject_1ogs25p\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\",\"obligation\":true,\"hasDefault\":true,\"inputType\":\"array\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null,\"target\":\"P1/Thematic/\",\"defaultValue\":null,\"defaultValueArray\":[\"sftp://test.example.it/vlab/P1/Thematic/*\"]}],\"outputs\":[{\"valueType\":\"value\",\"description\":null,\"name\":\"Output\",\"id\":\"DataObject_0loj2kk\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\",\"outputType\":\"individual\",\"createFolder\":true,\"target\":\"P1/Classification/Clumps.kea\"}]}",
				serialized);
		verify(new JSONDeserializer().deserialize(serialized, BPIOObject.class));
	}
}