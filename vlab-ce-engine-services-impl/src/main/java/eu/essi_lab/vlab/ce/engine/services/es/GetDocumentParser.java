package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class GetDocumentParser {

	private static final String SOURCE_KEY = "_source";
	private final IESHttpResponseReader responseReader;

	public GetDocumentParser(IESHttpResponseReader reader) {

		responseReader = reader;
	}

	public Optional<InputStream> getSource() {
		InputStream stream = null;

		try {

			stream = responseReader.readStream();

			JSONObject json = new JSONObject(new String(stream.readAllBytes()));

			JSONObject source = json.getJSONObject(SOURCE_KEY);

			return Optional.of(new ByteArrayInputStream(source.toString().getBytes()));

		} catch (IOException e) {
			return Optional.empty();
		}
	}
}
