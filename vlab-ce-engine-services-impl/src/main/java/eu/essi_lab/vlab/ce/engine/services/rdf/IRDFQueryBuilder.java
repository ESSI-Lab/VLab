package eu.essi_lab.vlab.ce.engine.services.rdf;

import eu.essi_lab.vlab.core.datamodel.BPUser;
import java.util.List;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;

/**
 * @author Mattia Santoro
 */
public interface IRDFQueryBuilder {
	GraphQuery resourceDetailsQuery(RepositoryConnection connection, List<String> identifiers);

	TupleQuery countQuery(String text, Boolean undertest, BPUser user, RepositoryConnection connection);

	TupleQuery matchQuery(String text, Integer start, Integer count, Boolean undertest, BPUser user, RepositoryConnection connection);

	SelectQuery matchQueryBase(String text, Boolean undertest, BPUser user);
}
