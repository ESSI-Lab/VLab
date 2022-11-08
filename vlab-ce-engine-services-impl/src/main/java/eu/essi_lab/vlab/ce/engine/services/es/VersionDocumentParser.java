package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class VersionDocumentParser {

	private final JSONObject json;
	private static final String VERSION_KEY = "version";

	public VersionDocumentParser(IESHttpResponseReader r) throws BPException {
		json = new HttpToJson(r, "Unparsable query response from elastic search", BPException.ERROR_CODES.BAD_ES_RESPONSE).getJson();
	}

	public String getVersion() {
		return json.getJSONObject(VERSION_KEY).getString("number");
	}
}
