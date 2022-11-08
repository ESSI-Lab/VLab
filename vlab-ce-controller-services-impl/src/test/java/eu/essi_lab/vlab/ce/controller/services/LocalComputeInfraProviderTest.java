package eu.essi_lab.vlab.ce.controller.services;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class LocalComputeInfraProviderTest {

	@Test
	public void test() {

		LocalComputeInfraProvider provider = new LocalComputeInfraProvider();

		Assert.assertTrue(provider.supports(null));

	}

}