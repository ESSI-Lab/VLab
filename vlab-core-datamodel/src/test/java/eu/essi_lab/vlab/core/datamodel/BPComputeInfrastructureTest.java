package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.configuration.BPECSComputeInfrastructure;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPComputeInfrastructureTest {

	@Test
	public void test() throws BPException {

		new JSONDeserializer().deserialize(new JSONSerializer().serialize(new BPECSComputeInfrastructure()),
				BPComputeInfrastructure.class);

	}

}