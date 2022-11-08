package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class QueryDocumentParser {

	private Logger logger = LogManager.getLogger(QueryDocumentParser.class);

	private static final String SOURCE_KEY = "_source";

	private static final String HITS_KEY = "hits";
	private final JSONObject json;
	private static final String TOTAL_KEY = "total";
	private static final String ERROR_KEY = "error";

	public QueryDocumentParser(IESHttpResponseReader reader) throws BPException {

		logger.trace("Parsing http response to json");

		json = new HttpToJson(reader, "Unparsable query response from elastic search", BPException.ERROR_CODES.BAD_ES_RESPONSE).getJson();

	}

	public JSONObject responseObject() {
		return json;
	}

	public List<InputStream> getSources() {
		logger.trace("Get sources");

		List<InputStream> lis = new ArrayList<>();

		if (isIndexNotFound())
			return lis;

		JSONArray jar = json.getJSONObject(HITS_KEY).getJSONArray(HITS_KEY);

		for (int i = 0; i < jar.length(); i++) {

			JSONObject jobj = jar.getJSONObject(i);

			JSONObject source = jobj.getJSONObject(SOURCE_KEY);

			lis.add(new ByteArrayInputStream(source.toString().getBytes()));

		}

		return lis;

	}

	public Integer getTotal() {
		logger.trace("Get total");
		if (isIndexNotFound())
			return 0;
		return json.getJSONObject(HITS_KEY).getInt(TOTAL_KEY);
	}

	public boolean isIndexNotFound() {

		if (json.has(ERROR_KEY) && json.getJSONObject(ERROR_KEY).has("type")) {

			return "index_not_found_exception".equalsIgnoreCase(json.getJSONObject(ERROR_KEY).getString("type"));

		}
		return false;

	}
}
