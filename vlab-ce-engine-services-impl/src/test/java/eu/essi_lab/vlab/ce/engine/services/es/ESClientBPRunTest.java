package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.ce.engine.services.BPExceptionMatcher;
import eu.essi_lab.vlab.ce.engine.services.ESRequestSubmitter;
import eu.essi_lab.vlab.ce.engine.services.HttpResponseReader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.ESQueryBPRuns;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class ESClientBPRunTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void test() {
		String url = "http://localhost";

		ESClient connector = new ESClientBPRun(url, null, null);

		assertEquals(url + "/", connector.getBaseUrl());

	}

	@Test
	public void test2() {

		String url = "http://localhost/";

		ESClient connector = new ESClientBPRun(url, null, null);

		assertEquals(url, connector.getBaseUrl());

	}

	@Test
	public void test3() throws BPException, IOException {

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = new ESClientBPRun(url[0], index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpPut put = (HttpPut) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/" + key;

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				assertNotNull(put.getEntity());

				assertNotNull(put.getEntity().getContent());

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		assertTrue(connector.store(key, stream));

	}

	@Test
	public void test4() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("IO exception writing to es at", BPException.ERROR_CODES.ES_WRITE_IOERROR));

		String url = "http://localhost/";

		String type = "type";
		String index = "index";

		ESClient connector = new ESClientBPRun(url, index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		Mockito.doThrow(IOException.class).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");
		String key = "key";

		connector.store(key, stream);

	}

	@Test
	public void test5() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("Can't write to elasticsearch at", BPException.ERROR_CODES.ES_WRITE_ERROR));

		String url = "http://localhost/";

		String type = "type";
		String index = "index";

		ESClient connector = new ESClientBPRun(url, index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(500).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doReturn(new HttpResponseReader(submitResponse)).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");
		String key = "key";

		connector.store(key, stream);

	}

	@Test
	public void test6() throws BPException, IOException {

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = new ESClientBPRun(url[0], index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpDelete put = (HttpDelete) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/" + key;

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		assertTrue(connector.remove(key));

	}

	@Test
	public void test8() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("IO exception deleting from es at ", BPException.ERROR_CODES.ES_DELETE_IOERROR));

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = new ESClientBPRun(url[0], index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doThrow(IOException.class).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		connector.remove(key);

	}

	@Test
	public void test9() throws BPException, IOException {

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = new ESClientBPRun(url[0], index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpGet put = (HttpGet) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/" + key;

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		assertTrue(connector.exists(key));

	}

	@Test
	public void test10() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("IO exception searching on es at ", BPException.ERROR_CODES.ES_GET_IOERROR));

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = new ESClientBPRun(url[0], index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doThrow(IOException.class).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		connector.exists(key);

	}

	@Test
	public void test11() throws BPException, IOException {

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = new ESClientBPRun(url[0], index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(404).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpGet put = (HttpGet) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/" + key;

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		assertFalse(connector.exists(key));

	}

	@Test
	public void test12() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("Unexpected error code 500 from es", BPException.ERROR_CODES.ES_GET_ERROR));

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = new ESClientBPRun(url[0], index, type);

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(500).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpGet put = (HttpGet) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/" + key;

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		connector.exists(key);

	}

	@Test
	public void test14() throws BPException, IOException {

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = Mockito.spy(new ESClientBPRun(url[0], index, type));

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpGet put = (HttpGet) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/" + key;

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		InputStream stream = Mockito.mock(InputStream.class);

		Mockito.doReturn(Optional.of(stream)).when(connector).parse(Mockito.any());

		assertNotNull(connector.read(key));

	}

	@Test
	public void test15() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("Can't parse response from es at ", BPException.ERROR_CODES.ES_GET_PARSEERROR));

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = Mockito.spy(new ESClientBPRun(url[0], index, type));

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpGet put = (HttpGet) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/" + key;

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		Mockito.doReturn(Optional.empty()).when(connector).parse(Mockito.any());

		connector.read(key);

	}

	@Test
	public void test16() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("IO exception reading from es at ", BPException.ERROR_CODES.ES_GET_IOERROR));

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient connector = Mockito.spy(new ESClientBPRun(url[0], index, type));

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		Mockito.doThrow(IOException.class).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		connector.read(key);

	}

	@Test
	public void test18() throws BPException, IOException {

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient<BPRuns> connector = Mockito.spy(new ESClientBPRun(url[0], index, type));

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(200).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpPost put = (HttpPost) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/_search";

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		QueryDocumentParser parser = Mockito.mock(QueryDocumentParser.class);

		Mockito.doReturn(parser).when(connector).queryDocumentParser(Mockito.any());

		List<InputStream> sources = new ArrayList<>();

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithDate.json");

		sources.add(stream);

		Mockito.doReturn(sources).when(parser).getSources();

		int total = 1;
		Mockito.doReturn(total).when(parser).getTotal();

		BPUser user = Mockito.mock(BPUser.class);
		String text = "text";
		int start = 0;
		int count = 3;

		ESQueryBPRuns queryBPRuns = new ESQueryBPRuns();
		queryBPRuns.setText(text);
		queryBPRuns.setStart(start);
		queryBPRuns.setCount(count);

		BPRuns runs = connector.search(user, queryBPRuns);

		assertEquals(1, runs.getRuns().size() - 0);

		assertEquals(total, runs.getTotal() - 0);

		assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", runs.getRuns().get(0).getRunid());

	}

	@Test
	public void test19() throws BPException, IOException {

		final String[] url = { "http://localhost/" };

		String type = "type";
		String index = "index";
		String key = "key";

		ESClient<BPRuns> connector = Mockito.spy(new ESClientBPRun(url[0], index, type));

		Mockito.doReturn("7.6.1").when(connector).readVersion();

		ESRequestSubmitter submitter = Mockito.mock(ESRequestSubmitter.class);

		HttpResponse submitResponse = Mockito.mock(HttpResponse.class);

		StatusLine line = Mockito.mock(StatusLine.class);

		Mockito.doReturn(404).when(line).getStatusCode();

		Mockito.doReturn(line).when(submitResponse).getStatusLine();

		HttpEntity entity = Mockito.mock(HttpEntity.class);
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("noindexfound.json");
		Mockito.doReturn(stream).when(entity).getContent();
		Mockito.doReturn(entity).when(submitResponse).getEntity();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				HttpPost put = (HttpPost) invocation.getArguments()[0];

				String requrl = put.getURI().toString();

				String exurl = "http://localhost/" + index + "/" + type + "/_search";

				if (!exurl.equalsIgnoreCase(requrl))
					throw new Exception("Bad url generated " + requrl);

				return new HttpResponseReader(submitResponse);
			}
		}).when(submitter).submit(Mockito.any());

		connector.setSubmitter(submitter);

		BPUser user = Mockito.mock(BPUser.class);
		String text = "text";
		int start = 0;
		int count = 3;
		ESQueryBPRuns queryBPRuns = new ESQueryBPRuns();
		queryBPRuns.setText(text);
		queryBPRuns.setStart(start);
		queryBPRuns.setCount(count);
		BPRuns runs = connector.search(user, queryBPRuns);

		assertEquals(0, runs.getRuns().size() - 0);

		assertEquals(0, runs.getTotal() - 0);

	}
}