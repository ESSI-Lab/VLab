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
public class QueryDocumentParserTest {

	@Test
	public void test() throws BPException, IOException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("esPostResponse.json");

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doReturn(stream).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		QueryDocumentParser parser = new QueryDocumentParser(new HttpResponseReader(response));

		Assert.assertEquals((Integer) 3, parser.getTotal());
		Assert.assertEquals((Integer) 1, (Integer) parser.getSources().size());
		Assert.assertNotNull(parser.responseObject());
	}

}