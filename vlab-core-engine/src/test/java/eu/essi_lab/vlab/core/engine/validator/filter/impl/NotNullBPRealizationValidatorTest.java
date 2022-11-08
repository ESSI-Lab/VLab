package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class NotNullBPRealizationValidatorTest {

	@Test
	public void testNullRealization() {

		NotNullBPRealizationValidator val = new NotNullBPRealizationValidator();

		ValidateRealizationResponse response = val.validate(null);

		assertFalse("Expected not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());
	}

	@Test
	public void testNotNullRealization() {

		NotNullBPRealizationValidator val = new NotNullBPRealizationValidator();

		ValidateRealizationResponse response = val.validate(new BPRealization());

		assertTrue("Expected  valid", response.getValid());

		assertEquals(ValidationMessageType.INFO, response.getMessages().get(0).getType());

	}

}