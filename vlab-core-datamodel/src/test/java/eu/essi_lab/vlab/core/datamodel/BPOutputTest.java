package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPOutputTest {

	/**
	 * this tests that old output definitions work and the default behaviour is to create (e.g. it looks like metapopulation needs it to be
	 * created)
	 *
	 * @throws BPException
	 */
	@Test
	public void retrocompatibilityTestCreateFolder() throws BPException {

		String content = "{\n" + "      \"id\": \"DataObject_0loj2kk\",\n" + "      \"outputType\": \"individual\",\n"
				+ "      \"valueType\": \"value\",\n" + "      \"description\": \"\",\n" + "      \"name\": \"Clumps L4 Period 2\",\n"
				+ "      \"valueSchema\": \"url\",\n" + "      \"target\": \"P2/Classification/Clumps_LCCSL4_P2.kea\"\n" + "    }";

		BPOutput deserialized = new JSONDeserializer().deserialize(content, BPOutput.class, false);

		Assert.assertTrue(deserialized.getCreateFolder());

	}

	@Test
	public void testCreateFolderTrue() throws BPException {

		String content = "{\n" + "      \"id\": \"DataObject_0loj2kk\",\n" + "      \"outputType\": \"individual\",\n"
				+ "      \"valueType\": \"value\",\n" + "      \"description\": \"\",\n" + "      \"name\": \"Clumps L4 Period 2\",\n"
				+ "      \"valueSchema\": \"url\",\n" + "      \"target\": \"P2/Classification/Clumps_LCCSL4_P2.kea\",\n" +

				"\"createFolder\": true\n"

				+ "    }";
		BPOutput deserialized = new JSONDeserializer().deserialize(content, BPOutput.class, false);

		Assert.assertTrue(deserialized.getCreateFolder());

	}

	@Test
	public void testCreateFolderFalse() throws BPException {

		String content = "{\n" + "      \"id\": \"DataObject_0loj2kk\",\n" + "      \"outputType\": \"individual\",\n"
				+ "      \"valueType\": \"value\",\n" + "      \"description\": \"\",\n" + "      \"name\": \"Clumps L4 Period 2\",\n"
				+ "      \"valueSchema\": \"url\",\n" + "      \"target\": \"P2/Classification/Clumps_LCCSL4_P2.kea\",\n" +

				"\"createFolder\": false\n"

				+ "    }";
		BPOutput deserialized = new JSONDeserializer().deserialize(content, BPOutput.class, false);

		Assert.assertFalse(deserialized.getCreateFolder());

	}

}