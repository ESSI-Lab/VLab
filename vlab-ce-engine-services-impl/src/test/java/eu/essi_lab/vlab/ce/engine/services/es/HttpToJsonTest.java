package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.ce.engine.services.BPExceptionMatcher;
import eu.essi_lab.vlab.ce.engine.services.HttpResponseReader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class HttpToJsonTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void test() throws BPException, IOException {

		String err = "Error";
		BPException.ERROR_CODES code = BPException.ERROR_CODES.OPERATION_NOT_SUPPORTED;

		expectedException.expect(new BPExceptionMatcher(err, code));

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doThrow(IOException.class).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		new HttpToJson(new HttpResponseReader(response), err, code);
	}

	@Test
	public void test2() throws BPException, IOException {

		String err = "Error";
		BPException.ERROR_CODES code = BPException.ERROR_CODES.OPERATION_NOT_SUPPORTED;

		HttpResponse response = Mockito.mock(HttpResponse.class);

		HttpEntity entity = Mockito.mock(HttpEntity.class);

		Mockito.doReturn(new ByteArrayInputStream("{test: \"value\"}".getBytes(Charset.forName("UTF-8")))).when(entity).getContent();

		Mockito.doReturn(entity).when(response).getEntity();

		HttpToJson httpToJson = new HttpToJson(new HttpResponseReader(response), err, code);

		Assert.assertNotNull(httpToJson.getJson());

		Assert.assertEquals("value", httpToJson.getJson().getString("test"));
	}

}