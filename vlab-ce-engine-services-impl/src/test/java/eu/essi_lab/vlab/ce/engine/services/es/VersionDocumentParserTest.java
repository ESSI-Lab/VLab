package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.ce.engine.services.HttpResponseReader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class VersionDocumentParserTest {

	@Test
	public void test() throws BPException, IOException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("esVersion.json");

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doReturn(stream).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		VersionDocumentParser parser = new VersionDocumentParser(new HttpResponseReader(response));

		Assert.assertEquals("5.3.2", parser.getVersion());

	}



	@Test
	public void test2() throws BPException, IOException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("esVersion7.json");

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doReturn(stream).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		VersionDocumentParser parser = new VersionDocumentParser(new HttpResponseReader(response));

		Assert.assertEquals("7.6.1", parser.getVersion());

	}
}