package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.ESQueryBPRuns;
import eu.essi_lab.vlab.core.datamodel.ESQueryObject;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class ESClientBPRun extends ESClient<BPRuns> {

	private static Logger logger = LogManager.getLogger(ESClientBPRun.class);

	JSONObject createESQuery(ESQueryBPRuns query, BPUser user) {

		return new ESQueryBuilder().//
				setStart(query.getStart()).//
				setCount(query.getCount()).//
				setQueryText(query.getText()).//
				setUser(user).//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setWildCardTextSearchFields(Arrays.asList("name", "inputs.name", "inputs.description")).//
				setExactTextSearchFields(Arrays.asList("inputs.value", "inputs.valueArray")).//
				addMustConstraint("workflowid.keyword", query.getWfid()).build();
	}

	@Override
	public BPRuns search(BPUser user, ESQueryObject query) throws BPException {

		if (!(query instanceof ESQueryBPRuns))
			throw new BPException("Bad query to ESClientBPRun, expected class ESQueryBPRuns but found " + query.getClass().getName(),
					BPException.ERROR_CODES.INVALID_REQUEST);

		ESQueryBPRuns q = (ESQueryBPRuns) query;

		QueryDocumentParser parser = execSearchPost(createESQuery(q, user).toString());

		List<BPRun> list = new ArrayList<>();

		List<InputStream> streams = parser.getSources();

		streams.forEach(stream -> {
			try {
				list.add(new JSONDeserializer().deserialize(stream, BPRun.class));
			} catch (BPException e) {

				logger.error("Unable to deserialize run from elastic search query response");

			}
		});

		BPRuns response = new BPRuns();

		response.setRuns(list);

		response.setTotal(parser.getTotal());

		return response;
	}

	public ESClientBPRun(String url, String index, String type) {
		super(url, index, type);
	}
}
