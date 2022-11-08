package eu.essi_lab.vlab.ce.engine.services.rdf;

import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.GraphQueryResult;

/**
 * @author Mattia Santoro
 */
public class RDFGraphQueryResponse {

	private Logger logger = LogManager.getLogger(RDFGraphQueryResponse.class);

	private Model model;

	public RDFGraphQueryResponse(GraphQueryResult graphQueryResult) {

		parse(graphQueryResult);

	}

	private void parse(GraphQueryResult graphQueryResult) {

		logger.trace("Parsing graph query result started");
		Date d1 = new Date();

		logger.trace("Adding statements");
		ModelBuilder builder = new ModelBuilder();

		graphQueryResult.stream().forEach(st -> builder.add(st.getSubject(), st.getPredicate(), st.getObject()));

		Date d2 = new Date();

		logger.trace("Statements added in {}", (d2.getTime() - d1.getTime()));

		logger.trace("Building model...");

		model = builder.build();

		Date d3 = new Date();
		logger.trace("Model built in {}", (d3.getTime() - d2.getTime()));

		logger.trace("Parsing graph query result completed (size {}) in {}", model.filter(null, null, null).size(),
				(d3.getTime() - d1.getTime()));

	}

	public List<APIWorkflowDetail> getResources() {

		List<APIWorkflowDetail> list = new ArrayList<>();

		Set<Resource> subjects = model.filter(null, null, null).subjects();

		subjects.forEach(
				sub -> list.add(new APIWorkflowDetailParser(sub.stringValue(), model.filter(sub, null, null)).getAPIWorkflowDetail()));

		return list;
	}
}
