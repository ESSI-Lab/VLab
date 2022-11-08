package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class URIBPRealizationValidatorTest {

	@Test
	public void testNotValidURI() {

		String uri = "http://mw1.google.com/mw-earth-vectordb/kml-samples/gp/seattle/gigapxl/$[level]/r$[y]_c$[x].jpg";

		URIBPRealizationValidator val = new URIBPRealizationValidator();

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = val.validate(realization);

		assertFalse("Expected not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());
	}

	@Test
	public void testNullURI() {

		String uri = null;

		URIBPRealizationValidator val = new URIBPRealizationValidator();

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = val.validate(realization);

		assertFalse("Expected not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());
	}

	@Test
	public void testEmptyURI() {

		String uri = "";

		URIBPRealizationValidator val = new URIBPRealizationValidator();

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = val.validate(realization);

		assertFalse("Expected not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());
	}

	@Test
	public void testValidURI() {

		String uri = "http://mw1.google.com/mw-earth-vectordb/kml-samples/ex.bpmn";

		URIBPRealizationValidator val = new URIBPRealizationValidator();

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = val.validate(realization);

		assertTrue("Expected valid", response.getValid());

		assertEquals(ValidationMessageType.INFO, response.getMessages().get(0).getType());
	}

}