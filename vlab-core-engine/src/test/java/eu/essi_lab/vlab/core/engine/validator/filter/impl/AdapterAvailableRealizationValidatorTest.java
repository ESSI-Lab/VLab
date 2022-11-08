package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import java.io.File;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class AdapterAvailableRealizationValidatorTest {

	@Test
	public void testMockNoAdapterAvailable() {

		String uri = "http://mw1.google.com/mw-earth-vectordb/kml-samples/gp/seattle/gigapxl/$[level]/r$[y]_c$[x].jpg";

		AdapterAvailableRealizationValidator val = new AdapterAvailableRealizationValidator();

		AdapterAvailableRealizationValidator mockedVal = Mockito.spy(val);

		AdapterFound found = new AdapterFound();

		found.setMsg("No adapter");

		Mockito.doReturn(found).when(mockedVal).findAdapter(Mockito.any());

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = mockedVal.validate(realization);

		assertFalse("Expected not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());
	}

	@Test
	public void testMockAdapterAvailable() {

		String uri = "http://mw1.google.com/mw-earth-vectordb/kml-samples/gp/seattle/gigapxl/$[level]/r$[y]_c$[x].jpg";

		AdapterAvailableRealizationValidator val = new AdapterAvailableRealizationValidator();

		AdapterAvailableRealizationValidator mockedVal = Mockito.spy(val);

		ISourceCodeConnector adapter = Mockito.mock(ISourceCodeConnector.class);

		AdapterFound found = new AdapterFound();
		found.setAdapter(adapter);
		Mockito.doReturn(found).when(mockedVal).findAdapter(Mockito.any());

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = mockedVal.validate(realization);

		assertTrue("Expected valid", response.getValid());

		assertEquals(ValidationMessageType.INFO, response.getMessages().get(0).getType());
	}

	private ISourceCodeConnector mockSCConnector(URL root) {

		ISourceCodeConnector sourceCodeConnector = Mockito.mock(ISourceCodeConnector.class);

		Mockito.doReturn(new File(root.getPath())).when(sourceCodeConnector).getDir();

		return sourceCodeConnector;
	}

	@Test
	public void testAdapterAvailable() {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		String uri = dirUrl.getFile();

		AdapterAvailableRealizationValidator val = Mockito.spy(new AdapterAvailableRealizationValidator());

		AdapterFound found = new AdapterFound();
		found.setAdapter(mockSCConnector(dirUrl));
		Mockito.doReturn(found).when(val).findAdapter(Mockito.any());

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = val.validate(realization);

		assertTrue("Expected valid", response.getValid());

		assertEquals(ValidationMessageType.INFO, response.getMessages().get(0).getType());
	}

}