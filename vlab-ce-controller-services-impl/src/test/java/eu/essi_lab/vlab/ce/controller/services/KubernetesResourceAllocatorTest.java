package eu.essi_lab.vlab.ce.controller.services;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class KubernetesResourceAllocatorTest {

	@Test
	public void test() {
		KubernetesResourceAllocator allocator = new KubernetesResourceAllocator();

		assertFalse(allocator.request(null).requestSent());
	}
}