package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class URIAccessibilityBPRealizationValidatorTest {

	@Test
	public void testLocalNotAccessible() {
		String uri = "/notexistingfolder";

		URIAccessibilityBPRealizationValidator val = new URIAccessibilityBPRealizationValidator();

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = val.validate(realization);

		assertFalse("Expected not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());
	}

	@Test
	public void testLocalAccessible() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		String uri = dirUrl.getFile();

		URIAccessibilityBPRealizationValidator val = new URIAccessibilityBPRealizationValidator();

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = val.validate(realization);

		assertTrue("Expected valid", response.getValid());

		assertEquals(ValidationMessageType.INFO, response.getMessages().get(0).getType());
	}

}