package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.ce.engine.services.HttpResponseReader;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class GetDocumentParserTest {

	@Test
	public void test() throws IOException {

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("esResponse.json");

		Mockito.doReturn(stream).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		GetDocumentParser parser = new GetDocumentParser(new HttpResponseReader(response));

		assertTrue(parser.getSource().isPresent());

	}

}