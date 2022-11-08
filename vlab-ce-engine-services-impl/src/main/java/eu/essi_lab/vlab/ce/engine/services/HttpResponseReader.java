package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class HttpResponseReader implements IESHttpResponseReader {

	private InputStream stream;
	private final HttpResponse response;
	private Logger logger = LogManager.getLogger(HttpResponseReader.class);
	private IOException ioException;

	public HttpResponseReader(HttpResponse httpResponse) {
		response = httpResponse;

		try {
			if (httpResponse.getEntity() != null && httpResponse.getEntity().getContent() != null) {

				stream = new BufferedInputStream(httpResponse.getEntity().getContent());
				stream.mark(0);
			}
		} catch (IOException e) {
			logger.warn("IOException reading response stream", e);
			ioException = e;
		}

	}

	@Override
	public InputStream readStream() throws IOException {
		if (stream == null)
			throw ioException;
		return stream;
	}

	@Override
	public int readCode() {
		return response.getStatusLine().getStatusCode();
	}
}
