package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.core.configuration.BPECSComputeInfrastructure;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class ECSManagerTest {
	@Test
	public void test() {
		ECSManager manager = new ECSManager();

		assertNotNull(manager.getExecutor());

		assertTrue(manager.supports(Mockito.mock(BPECSComputeInfrastructure.class)));
	}
}