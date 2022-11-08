package eu.essi_lab.vlab.core.configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author mattia
 */
public class ConfigurationParameterTest {

	@Test
	public void test() {

		ConfigurationParameter parameter = new ConfigurationParameter("test.par.PARAM.key", "value");

		Assert.assertEquals("test.par.value.key", parameter.getKey());

	}

	@Test
	public void test2() {

		ConfigurationParameter parameter = new ConfigurationParameter("test.par.PARAM.key.PARAM", "value", "value2");

		Assert.assertEquals("test.par.value.key.value2", parameter.getKey());

	}
}