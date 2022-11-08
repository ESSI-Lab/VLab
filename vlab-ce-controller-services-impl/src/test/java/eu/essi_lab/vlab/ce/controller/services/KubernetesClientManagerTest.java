package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPKubernetesComputeInfrastructure;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class KubernetesClientManagerTest {
	@Test
	public void test() {
		KubernetesClientManager manager = new KubernetesClientManager();

		assertNotNull(manager.getExecutor());

		assertTrue(manager.supports(Mockito.mock(BPKubernetesComputeInfrastructure.class)));
	}
}