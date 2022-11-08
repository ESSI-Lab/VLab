package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class QueryDocumentParser7 extends QueryDocumentParser {

	private Logger logger = LogManager.getLogger(QueryDocumentParser7.class);

	private static final String HITS_KEY = "hits";
	private static final String TOTAL_KEY = "total";
	private static final String VALUE_KEY = "value";

	public QueryDocumentParser7(IESHttpResponseReader reader) throws BPException {

		super(reader);

	}

	@Override
	public Integer getTotal() {
		logger.trace("Get total");
		if (isIndexNotFound())
			return 0;
		return responseObject().getJSONObject(HITS_KEY).getJSONObject(TOTAL_KEY).getInt(VALUE_KEY);
	}
}
