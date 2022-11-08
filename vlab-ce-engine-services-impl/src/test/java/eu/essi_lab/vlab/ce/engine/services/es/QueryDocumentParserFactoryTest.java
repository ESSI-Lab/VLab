package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.ce.engine.services.HttpResponseReader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class QueryDocumentParserFactoryTest {

	@Test
	public void test() throws IOException, BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("esPostResponse.json");

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doReturn(stream).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		QueryDocumentParser parser = QueryDocumentParserFactory.getParser(new HttpResponseReader(response), Optional.empty());

		Assert.assertFalse(parser instanceof QueryDocumentParser7);
	}

	@Test
	public void test2() throws IOException, BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("esPostResponse.json");

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doReturn(stream).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		QueryDocumentParser parser = QueryDocumentParserFactory.getParser(new HttpResponseReader(response), Optional.of("5.3.4"));

		Assert.assertFalse(parser instanceof QueryDocumentParser7);
	}

	@Test
	public void test3() throws IOException, BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("esPostResponse.json");

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doReturn(stream).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		QueryDocumentParser parser = QueryDocumentParserFactory.getParser(new HttpResponseReader(response), Optional.of("7.3.4"));

		Assert.assertTrue(parser instanceof QueryDocumentParser7);
	}

}