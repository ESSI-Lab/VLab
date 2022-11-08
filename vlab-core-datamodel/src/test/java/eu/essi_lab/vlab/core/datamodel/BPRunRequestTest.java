package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPRunRequestTest {

	private String infra = "cloud2";
	private String infra_name = "Run with infra";
	private String no_infra_name = "Run with no infra";

	@Test
	public void test() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("noInfraBPRunRequest.json");

		BPRunRequest deserialized = new JSONDeserializer().deserialize(stream, BPRunRequest.class);

		assertNull(deserialized.getInfra());

		assertNotNull(deserialized.getName());
		assertEquals(no_infra_name, deserialized.getName());
	}

	@Test
	public void test2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("infraBPRunRequest.json");

		BPRunRequest deserialized = new JSONDeserializer().deserialize(stream, BPRunRequest.class);

		assertNotNull(deserialized.getInfra());
		assertEquals(infra, deserialized.getInfra());

		assertNotNull(deserialized.getName());
		assertEquals(infra_name, deserialized.getName());
	}

}