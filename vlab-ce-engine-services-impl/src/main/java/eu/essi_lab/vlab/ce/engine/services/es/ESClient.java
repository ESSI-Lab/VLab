package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.ESQueryObject;
import eu.essi_lab.vlab.core.engine.services.IESClient;
import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import eu.essi_lab.vlab.core.engine.services.IESRequestSubmitter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public abstract class ESClient<T> implements IESClient {

	private final String indexName;
	private final String indexType;
	private final String baseUrl;
	private static Logger logger = LogManager.getLogger(ESClient.class);
	private IESRequestSubmitter submitter;

	private static final String SEARCH = "_search";

	private static final String GET_EXCEPTION = "GET exception";
	private Optional<String> version = Optional.empty();
	private static final String GET_RESPONSE_CODE = "GET response code {}";
	private static final String GET_URL = "GET {}";

	public ESClient(String url, String index, String type) {

		if (!url.endsWith("/"))
			this.baseUrl = url + "/";
		else
			this.baseUrl = url;

		this.indexName = index;

		this.indexType = type;

	}

	String readVersion() throws BPException {
		logger.debug("Requested version of {}", baseUrl);

		String geturl = baseUrl;

		logger.trace(GET_URL, geturl);

		HttpGet get = new HttpGet(geturl);
		String msg = "Can't read version of elasticsearch at " + baseUrl;

		try {

			IESHttpResponseReader reader = getSubmitter().submit(get);

			int code = reader.readCode();

			logger.trace(GET_RESPONSE_CODE, code);

			checkCodeAndThrowEx(code, msg, null, reader, BPException.ERROR_CODES.ES_GET_ERROR, false);

			return versionDocumentParser(reader).getVersion();

		} catch (IOException e) {

			logger.warn(GET_EXCEPTION, e);

			String bpMsg = "IO exception reading version on es at " + this.baseUrl + ": " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.ES_GET_IOERROR);
		}
	}

	VersionDocumentParser versionDocumentParser(IESHttpResponseReader reader) throws BPException {
		return new VersionDocumentParser(reader);
	}

	@Override
	public Boolean store(String key, InputStream stream) throws BPException {

		logger.debug("Requested to write {} of type {} to {}", key, indexType, baseUrl);

		String puturl = baseUrl + indexName + "/" + indexType + "/" + key;

		logger.trace("Write PUT {}", puturl);

		HttpPut put = new HttpPut(puturl);

		String msg = "Can't write to elasticsearch at " + baseUrl;

		try {

			HttpEntity entity = EntityBuilder.create().setText(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).setContentType(
					ContentType.APPLICATION_JSON).build();

			put.setEntity(entity);

			IESHttpResponseReader reader = getSubmitter().submit(put);

			int code = reader.readCode();

			logger.trace("Write PUT response code {}", code);

			checkCodeAndThrowEx(code, msg, null, reader, BPException.ERROR_CODES.ES_WRITE_ERROR, false);

			return true;

		} catch (IOException e) {

			logger.warn("PUT exception", e);

			String bpMsg = "IO exception writing to es at " + this.baseUrl + ": " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.ES_WRITE_IOERROR);
		}

	}

	@Override
	public IESRequestSubmitter getSubmitter() {
		logger.trace("Using submitter {}", submitter.getClass().getName());

		return submitter;
	}

	private void checkCodeAndThrowEx(int code, String errMsg, String userErrMsg, IESHttpResponseReader reader,
			BPException.ERROR_CODES errCode, Boolean ignoreIndexNotFound) throws BPException {

		if (!validCode(code)) {
			String r = "Unparsable";

			try {

				InputStream stream = reader.readStream();
				r = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				stream.reset();

				if (Boolean.TRUE.equals(ignoreIndexNotFound) && isIndexNotFound(reader)) {
					stream.reset();
					return;
				}

			} catch (IOException | NullPointerException e) {
				logger.warn("Can't read response which returned invalid code {}", code, e);
			}

			logger.warn("Invalid code returned with response {}", r);

			BPException ex = new BPException(errMsg, errCode);

			ex.setUserMessage(userErrMsg);

			throw ex;
		}

	}

	private boolean isIndexNotFound(IESHttpResponseReader reader) {

		logger.trace("Checking if index not found has been returned from ES");

		try {
			return queryDocumentParser(reader).isIndexNotFound();
		} catch (BPException e) {
			logger.warn("Can't instantiate ES QueryDocumentParser and determine if ES responded with index not found, returning false");
		}

		return false;

	}

	private boolean validCode(int code) {
		logger.trace("Validating code [{}], valid code range is 200..399", code);

		return code >= 200 && code <= 399;
	}

	@Override
	public void setSubmitter(IESRequestSubmitter submitter) {

		this.submitter = submitter;

	}

	@Override
	public Boolean remove(String key) throws BPException {

		logger.debug("Requested to delete {} of type {} from {}", key, indexType, baseUrl);

		String deleteurl = baseUrl + indexName + "/" + indexType + "/" + key;

		logger.trace("Delete DELETE {}", deleteurl);

		HttpDelete delete = new HttpDelete(deleteurl);

		String msg = "Can't delete from elasticsearch at " + baseUrl;

		try {

			IESHttpResponseReader reader = getSubmitter().submit(delete);

			int code = reader.readCode();

			logger.trace("DELETE response code {}", code);

			checkCodeAndThrowEx(code, msg, null, reader, BPException.ERROR_CODES.ES_DELETE_ERROR, false);

			return true;

		} catch (IOException e) {

			logger.warn("DELETE exception", e);

			String bpMsg = "IO exception deleting from es at " + this.baseUrl + ": " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.ES_DELETE_IOERROR);
		}
	}

	@Override
	public Boolean exists(String key) throws BPException {

		logger.debug("Requested exist of {} of type {} from {}", key, indexType, baseUrl);

		String deleteurl = baseUrl + indexName + "/" + indexType + "/" + key;

		logger.trace(GET_URL, deleteurl);

		HttpGet get = new HttpGet(deleteurl);

		try {

			IESHttpResponseReader reader = getSubmitter().submit(get);

			int code = reader.readCode();

			logger.trace(GET_RESPONSE_CODE, code);

			if (code - 200 == 0)
				return true;

			if (code - 404 == 0)
				return false;

			throw new BPException("Unexpected error code " + code + " from es", BPException.ERROR_CODES.ES_GET_ERROR);

		} catch (IOException e) {

			logger.warn(GET_EXCEPTION, e);

			String bpMsg = "IO exception searching on es at " + this.baseUrl + ": " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.ES_GET_IOERROR);
		}
	}

	Optional<InputStream> parse(IESHttpResponseReader reader) {
		return new GetDocumentParser(reader).getSource();
	}

	public InputStream read(String key) throws BPException {

		logger.debug("Requested to read {} of type {} from {}", key, indexType, baseUrl);

		String deleteurl = baseUrl + indexName + "/" + indexType + "/" + key;

		logger.trace(GET_URL, deleteurl);

		HttpGet get = new HttpGet(deleteurl);

		String msg = "Can't get from elasticsearch at " + baseUrl;

		try {

			IESHttpResponseReader reader = getSubmitter().submit(get);

			int code = reader.readCode();

			logger.trace(GET_RESPONSE_CODE, code);

			checkCodeAndThrowEx(code, msg, null, reader, BPException.ERROR_CODES.ES_GET_ERROR, false);

			Optional<InputStream> source = parse(reader);

			if (!source.isPresent()) {

				throw new BPException("Can't parse response from es at " + baseUrl, BPException.ERROR_CODES.ES_GET_PARSEERROR);
			}

			return source.get();

		} catch (IOException e) {

			logger.warn(GET_EXCEPTION, e);

			String bpMsg = "IO exception reading from es at " + this.baseUrl + ": " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.ES_GET_IOERROR);
		}
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public abstract T search(BPUser user, ESQueryObject query) throws BPException;

	QueryDocumentParser queryDocumentParser(IESHttpResponseReader reader) throws BPException {

		if (!version.isPresent())
			version = Optional.of(readVersion());

		return QueryDocumentParserFactory.getParser(reader, version);

	}

	protected QueryDocumentParser execSearchPost(String body) throws BPException {

		logger.debug("Requested to query {} on type {}", baseUrl, indexType);

		String posturl = baseUrl + indexName + "/" + indexType + "/" + SEARCH;

		logger.trace("POST url {}", posturl);

		logger.trace("POST body {}", body);

		HttpPost post = new HttpPost(posturl);

		String msg = "Can't execute query to elasticsearch at " + baseUrl;

		try {

			InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

			HttpEntity entity = EntityBuilder.create().setText(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).setContentType(
					ContentType.APPLICATION_JSON).build();

			post.setEntity(entity);

			IESHttpResponseReader responseReader = getSubmitter().submit(post);

			int code = responseReader.readCode();

			logger.trace("POST response code {}", code);

			checkCodeAndThrowEx(code, msg, null, responseReader, BPException.ERROR_CODES.ES_GET_ERROR, true);

			return queryDocumentParser(responseReader);

		} catch (IOException e) {

			logger.warn(GET_EXCEPTION, e);

			String bpMsg = "IO exception searching from es at " + this.baseUrl + ": " + e.getMessage();

			throw new BPException(bpMsg, BPException.ERROR_CODES.ES_GET_IOERROR);
		}

	}

}
