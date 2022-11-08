package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class HttpToJson {

	private final JSONObject json;
	private Logger logger = LogManager.getLogger(HttpToJson.class);

	public HttpToJson(IESHttpResponseReader reader, String err, BPException.ERROR_CODES code) throws BPException {

		try {
			InputStream stream = reader.readStream();

			json = new JSONObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8));

			logger.debug("Response body: {}", json);

		} catch (IOException e) {
			throw new BPException(err, code);
		}
	}

	public JSONObject getJson() {
		return json;
	}
}
