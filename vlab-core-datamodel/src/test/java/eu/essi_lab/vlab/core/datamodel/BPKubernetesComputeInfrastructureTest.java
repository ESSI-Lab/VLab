package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.util.Optional;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class BPKubernetesComputeInfrastructureTest {

	@Test
	public void test1() {

		assertFalse(new BPKubernetesComputeInfrastructure().getS3ServiceUrl().isPresent());

	}

	@Test
	public void test2() throws BPException {
		BPKubernetesComputeInfrastructure infra = new BPKubernetesComputeInfrastructure();

		BPKubernetesComputeInfrastructure des = new JSONDeserializer().deserialize(new JSONSerializer().serialize(infra),
				BPKubernetesComputeInfrastructure.class);

		assertFalse(des.getS3ServiceUrl().isPresent());

	}

	@Test
	public void test3() throws BPException {
		BPKubernetesComputeInfrastructure infra = new BPKubernetesComputeInfrastructure();

		infra.setS3ServiceUrl(Optional.of("value"));

		BPKubernetesComputeInfrastructure des = new JSONDeserializer().deserialize(new JSONSerializer().serialize(infra),
				BPKubernetesComputeInfrastructure.class);

		assertTrue(des.getS3ServiceUrl().isPresent());
		assertEquals("value", des.getS3ServiceUrl().get());

	}

}