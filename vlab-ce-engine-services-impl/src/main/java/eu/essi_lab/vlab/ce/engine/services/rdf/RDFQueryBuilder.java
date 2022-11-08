package eu.essi_lab.vlab.ce.engine.services.rdf;

import eu.essi_lab.vlab.core.datamodel.BPUser;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

/**
 * @author Mattia Santoro
 */
public class RDFQueryBuilder implements IRDFQueryBuilder {

	public static final String PREFIX = "";
	public static final String NAMESPACE = "http://essi-lab.eu/2020/02/d2k/core#";
	public static final Iri IRI = Rdf.iri(NAMESPACE);
	public static final String ESKOS_PREFIX = "eskos";
	public static final String ESKOS_NAMESPACE = "http://essi-lab.eu/2020/02/d2k/eskos#";
	public static final Iri ESKOS_IRI = Rdf.iri(ESKOS_NAMESPACE);
	public static final String DESCRIBED_BY_PREDICATE = "described_by";
	public static final String MODEL_DEV_EMAIL_PREDICATE = "modelDeveloperEmail";
	public static final String MODEL_DEV_ORG_PREDICATE = "modelDeveloperOrg";
	public static final String UNDER_TEST_PREDICATE = "under_test";
	public static final String DCE_PREFIX = "dce";
	public static final String DCE_NAMESPACE = "http://purl.org/dc/elements/";
	public static final Iri DCE_IRI = Rdf.iri(DCE_NAMESPACE);
	public static final String DEC_CREATOR_PREDICATE = "creator";
	public static final String SHARED_WITH_PREDICATE = "sharedWith";
	public static final String IDENTIFIER_VAR_LOCAL_NAME = "identifier";
	protected final Prefix vlabrdfPrefix = SparqlBuilder.prefix(PREFIX, IRI);
	protected final Prefix eskosPrefix = SparqlBuilder.prefix(ESKOS_PREFIX, ESKOS_IRI);
	protected final Prefix dce = SparqlBuilder.prefix(DCE_PREFIX, DCE_IRI);
	private Logger logger = LogManager.getLogger(RDFQueryBuilder.class);
	private static final String MODEL_NAME_LOCAL_NAME = "name";
	private static final String MODEL_DESC_LOCAL_NAME = "desc";

	private List<Prefix> prefixes() {
		return Arrays.asList(vlabrdfPrefix, eskosPrefix, dce);
	}

	@Override
	public GraphQuery resourceDetailsQuery(RepositoryConnection connection, List<String> identifiers) {

		ConstructQuery constructQuery = Queries.CONSTRUCT();

		prefixes().forEach(constructQuery::prefix);
		SimpleValueFactory factory = SimpleValueFactory.getInstance();

		int c = 0;
		for (String id : identifiers) {
			Variable l = SparqlBuilder.var("l" + c);
			Variable t = SparqlBuilder.var("t" + c);
			Variable d = SparqlBuilder.var("d" + c);
			Variable cr = SparqlBuilder.var("cr" + c);
			Variable db = SparqlBuilder.var("db" + c);
			Variable mde = SparqlBuilder.var("mde" + c);
			Variable mdo = SparqlBuilder.var("mdo" + c);
			Variable ut = SparqlBuilder.var("ut" + c);
			Variable sh = SparqlBuilder.var("sh" + c);

			IRI irr = factory.createIRI(id);

			TriplePattern gp1 = GraphPatterns.tp(irr, Rdf.iri(RDFS.LABEL), l);
			TriplePattern gp2 = GraphPatterns.tp(irr, Rdf.iri(RDF.TYPE), t);
			TriplePattern gp3 = GraphPatterns.tp(irr, DCTERMS.ABSTRACT, d);
			TriplePattern gp4 = GraphPatterns.tp(irr, dce.iri(DEC_CREATOR_PREDICATE), cr);
			TriplePattern gp5 = GraphPatterns.tp(irr, eskosPrefix.iri(DESCRIBED_BY_PREDICATE), db);
			TriplePattern gp6 = GraphPatterns.tp(irr, eskosPrefix.iri(MODEL_DEV_EMAIL_PREDICATE), mde);
			TriplePattern gp7 = GraphPatterns.tp(irr, eskosPrefix.iri(MODEL_DEV_ORG_PREDICATE), mdo);
			TriplePattern gp8 = GraphPatterns.tp(irr, eskosPrefix.iri(UNDER_TEST_PREDICATE), ut);
			TriplePattern gp9 = GraphPatterns.tp(irr, eskosPrefix.iri(SHARED_WITH_PREDICATE), sh);

			constructQuery.construct(gp1, gp2, gp3, gp4, gp5, gp6, gp7, gp8, gp9)//
					.where(Rdf.iri(irr).has(Rdf.iri(RDF.TYPE), t))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(Rdf.iri(RDFS.LABEL), l)//
							))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(DCTERMS.ABSTRACT, d)//
							))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(dce.iri(DEC_CREATOR_PREDICATE), cr)//
							))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(eskosPrefix.iri(DESCRIBED_BY_PREDICATE), db)//
							))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(eskosPrefix.iri(MODEL_DEV_EMAIL_PREDICATE), mde)//
							))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(eskosPrefix.iri(MODEL_DEV_ORG_PREDICATE), mdo)//
							))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(eskosPrefix.iri(UNDER_TEST_PREDICATE), ut)//
							))//
					.where(//
							GraphPatterns.optional(Rdf.iri(irr).has(eskosPrefix.iri(SHARED_WITH_PREDICATE), sh)//
							));
			c++;

		}

		if (logger.isTraceEnabled())
			logger.trace("Graph query {}{}", System.lineSeparator(), constructQuery.getQueryString());
		return connection.prepareGraphQuery(QueryLanguage.SPARQL, constructQuery.getQueryString());

	}

	@Override
	public TupleQuery countQuery(String text, Boolean undertest, BPUser user, RepositoryConnection connection) {

		SelectQuery selectQuery = matchQueryBase(text, undertest, user);

		String coreQuery = selectQuery.getQueryString().replace("SELECT DISTINCT ?identifier",
				"SELECT ( COUNT( ?identifier ) AS ?cnt ) WHERE { SELECT DISTINCT ?identifier");

		StringBuilder builder = new StringBuilder(coreQuery);

		builder.append("}");
		String countQuery = builder.toString();

		if (logger.isTraceEnabled())
			logger.trace("Count query {}{}", System.lineSeparator(), countQuery);

		return connection.prepareTupleQuery(QueryLanguage.SPARQL, countQuery);

	}

	@Override
	public TupleQuery matchQuery(String text, Integer start, Integer count, Boolean undertest, BPUser user,
			RepositoryConnection connection) {

		SelectQuery selectQuery = matchQueryBase(text, undertest, user);
		selectQuery.offset(start).limit(count);

		if (logger.isTraceEnabled())
			logger.trace("Match query {}{}", System.lineSeparator(), selectQuery.getQueryString());

		return connection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());

	}

	@Override
	public SelectQuery matchQueryBase(String text, Boolean undertest, BPUser user) {
		SelectQuery selectQuery = Queries.SELECT();

		Variable identifier = SparqlBuilder.var(IDENTIFIER_VAR_LOCAL_NAME);
		selectQuery.distinct().select(identifier);
		selectQuery.groupBy(identifier);
		prefixes().forEach(selectQuery::prefix);

		Variable nameVar = SparqlBuilder.var(MODEL_NAME_LOCAL_NAME);
		Variable descVar = SparqlBuilder.var(MODEL_DESC_LOCAL_NAME);

		Expression<?> exp1 = Expressions.regex(Expressions.str(nameVar), text, "i");
		Expression<?> exp2 = Expressions.regex(Expressions.str(descVar), text, "i");
		Expression<?> exp = Expressions.or(exp1, exp2);

		selectQuery.where(//
				identifier.has(eskosPrefix.iri(UNDER_TEST_PREDICATE), Rdf.literalOf(undertest))//
						.andHas(eskosPrefix.iri(MODEL_DEV_EMAIL_PREDICATE), Rdf.literalOf(user.getEmail()))//
						.union(identifier.has(eskosPrefix.iri(UNDER_TEST_PREDICATE), Rdf.literalOf(false)))//
						.union(identifier.has(eskosPrefix.iri(SHARED_WITH_PREDICATE), Rdf.literalOf(user.getEmail()))//
								.andHas(eskosPrefix.iri(UNDER_TEST_PREDICATE), Rdf.literalOf(undertest)))//
						.and(GraphPatterns.optional(identifier.has(Rdf.iri(RDFS.LABEL), nameVar)))//
						.and(GraphPatterns.optional(identifier.has(Rdf.iri(DCTERMS.ABSTRACT), descVar)))//
						.filter(exp)//

		);
		return selectQuery;
	}
}
