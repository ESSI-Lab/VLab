package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import java.net.URISyntaxException;
import java.net.URL;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class SourceCodeConnectorTest {
	@Test
	public void testSupports() throws BPException, URISyntaxException {

		SourceCodeConnector adapter = Mockito.spy(new SourceCodeConnector());

		BPRealization realization = Mockito.mock(BPRealization.class);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		Mockito.doReturn(dirUrl.getFile()).when(realization).getRealizationURI();

		assertTrue(adapter.supports(realization).isConventionsOk());

	}
}