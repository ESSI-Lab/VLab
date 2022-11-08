package eu.essi_lab.vlab.ce.engine.services.rdf;

import static eu.essi_lab.vlab.ce.engine.services.rdf.RDFQueryBuilder.IDENTIFIER_VAR_LOCAL_NAME;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

/**
 * @author Mattia Santoro
 */
public class RDFIdentifiersTupleQueryResponse {

	private List<String> identifiers = new ArrayList<>();

	private Logger logger = LogManager.getLogger(RDFIdentifiersTupleQueryResponse.class);

	public RDFIdentifiersTupleQueryResponse(TupleQueryResult tupleQueryResult) {
		parse(tupleQueryResult);

	}

	private void parse(TupleQueryResult tupleQueryResult) {

		logger.trace("Parsing tuple query result started");

		Iterator<BindingSet> iterator = tupleQueryResult.iterator();

		while (iterator.hasNext()) {

			BindingSet bindingSet = iterator.next();

			Binding id = bindingSet.getBinding(IDENTIFIER_VAR_LOCAL_NAME);

			String identifier = id.getValue().stringValue();

			logger.trace("Found identifier {}", identifier);

			identifiers.add(identifier);

			logger.trace("Parsing tuple query result completed");
		}
	}

	public List<String> getIdentifiers() {
		return identifiers;
	}
}
