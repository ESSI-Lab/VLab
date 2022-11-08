package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.ce.engine.services.rdf.APIWorkflowDetailParser;
import eu.essi_lab.vlab.ce.engine.services.rdf.RDFCountQueryResponse;
import eu.essi_lab.vlab.ce.engine.services.rdf.RDFIdentifiersTupleQueryResponse;
import eu.essi_lab.vlab.ce.engine.services.rdf.RDFQueryBuilder;
import eu.essi_lab.vlab.ce.engine.services.rdf.IRDFQueryBuilder;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;
import eu.essi_lab.vlab.core.engine.services.IBPWorkflowRegistryStorage;
import eu.essi_lab.vlab.ce.engine.services.rdf.RDFGraphQueryResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * @author Mattia Santoro
 */
public class RDFBPWorkflowRegistryStorage implements IBPWorkflowRegistryStorage {

	private Repository repository;

	private Logger logger = LogManager.getLogger(RDFBPWorkflowRegistryStorage.class);

	private IRDFQueryBuilder queryBuilder = new RDFQueryBuilder();

	public IRDFQueryBuilder getRDFBuilder() {
		return queryBuilder;
	}

	public void setRDFBuilder(IRDFQueryBuilder builder) {
		this.queryBuilder = builder;
	}

	@Override
	public APIWorkflowDetail getAPIWorkflowDetail(String workflowid, BPUser user) throws BPException {

		List<APIWorkflowDetail> resources = getAPIWorkflowDetail(Arrays.asList(workflowid));
		if (resources.isEmpty())
			throw new BPException("Can't find workflow with id " + workflowid, BPException.ERROR_CODES.RESOURCE_NOT_FOUND);

		return resources.get(0);

	}

	public List<APIWorkflowDetail> getAPIWorkflowDetail(List<String> workflowids) {

		try (RepositoryConnection connection = this.repository.getConnection()) {

			GraphQuery detailsQuery = getRDFBuilder().resourceDetailsQuery(connection, workflowids);

			logger.trace("Resource Details query submit {}", this.repository);

			List<APIWorkflowDetail> resources = getResourcesGraphQuery(detailsQuery, false);

			logger.trace("Resource Details query completed with {} matches", resources.size());

			return resources;
		}
	}

	List<APIWorkflowDetail> getResourcesGraphQuery(GraphQuery query, Boolean includeInferred) {

		query.setIncludeInferred(includeInferred);

		try (GraphQueryResult ev = query.evaluate()) {

			return new RDFGraphQueryResponse(ev).getResources();

		}
	}

	List<String> getIdentifiersTupleQuery(TupleQuery query, Boolean includeInferred) {

		query.setIncludeInferred(includeInferred);

		try (TupleQueryResult ev = query.evaluate()) {

			return new RDFIdentifiersTupleQueryResponse(ev).getIdentifiers();

		}
	}

	Integer countTupleQuery(TupleQuery query, Boolean includeInferred) {

		query.setIncludeInferred(includeInferred);

		try (TupleQueryResult ev = query.evaluate()) {

			return new RDFCountQueryResponse(ev).getCount();

		}

	}

	@Override
	public void storeWorkflowDetail(APIWorkflowDetail bp) throws BPException {

		logger.trace("Adding workflow {} to {}", bp.getId(), this.repository);

		addStatements(new APIWorkflowDetailParser(bp).toRDFStatements());

		logger.trace("Completed adding workflow {} to {}", bp.getId(), this.repository);

	}

	@Override
	public void deleteWorkflowDetail(String workflowid) throws BPException {

		APIWorkflowDetail bp = getAPIWorkflowDetail(workflowid, null);

		logger.trace("Deleting content in {}", this.repository);

		removeStatements(new APIWorkflowDetailParser(bp).toRDFStatements());

		logger.trace("Completed deleting content in {}", this.repository);

	}

	private void removeStatements(Stream<Statement> statementStream) throws BPException {

		logger.trace("Deleting content in {}", this.repository);

		try (RepositoryConnection connection = this.repository.getConnection()) {

			connection.remove(statementStream.collect(Collectors.toSet()), new Resource[] {});

		} catch (RepositoryException e) {
			logger.error("Error deleting in {}", this.repository, e);

			throw new BPException("RepositoryException deleting statements in RDF storage", BPException.ERROR_CODES.RDF_STORE_ERROR);
		}

		logger.trace("Completed deleting content in {}", this.repository);

	}

	private void addStatements(Stream<Statement> statementStream) throws BPException {

		logger.trace("Adding content in {}", this.repository);

		try (RepositoryConnection connection = this.repository.getConnection()) {

			connection.add(statementStream.collect(Collectors.toSet()), new Resource[] {});

		} catch (RepositoryException e) {
			logger.error("Error adding in {}", this.repository, e);

			throw new BPException("RepositoryException adding statements in RDF storage", BPException.ERROR_CODES.RDF_STORE_ERROR);
		}

		logger.trace("Completed adding content in {}", this.repository);

	}

	@Override
	public SearchWorkflowsResponse searchWorkflowDetail(String text, Integer start, Integer count, Boolean undertest, BPUser user)
			throws BPException {
		SearchWorkflowsResponse response = new SearchWorkflowsResponse();

		List<String> identifiers = new ArrayList<>();

		try (RepositoryConnection connection = this.repository.getConnection()) {

			logger.trace("Count query prepare");

			TupleQuery countQuery = getRDFBuilder().countQuery(text, undertest, user, connection);

			logger.trace("Count query submit");

			Integer result = countTupleQuery(countQuery, false);

			response.setTotal(result);

			logger.trace("Count query completed ({})", result);

			if (response.getTotal() == 0) {
				logger.debug("Found zero matches, returning");

				response.setWorkflows(new ArrayList<>());

				return response;
			}

			TupleQuery matchQuery = getRDFBuilder().matchQuery(text, start, count, undertest, user, connection);

			logger.trace("Identifiers query submit");

			identifiers = getIdentifiersTupleQuery(matchQuery, false);

			logger.trace("Identifiers query completed ({})", identifiers.size());

			if (identifiers.isEmpty()) {
				logger.debug("Found zero identifiers for page {}-{}, returning", start, (start + count - 1));

				response.setWorkflows(new ArrayList<>());

				return response;
			}
		}

		List<APIWorkflowDetail> workflows = getAPIWorkflowDetail(identifiers);

		response.setWorkflows(workflows);

		return response;

	}

	Set<Statement> calculateToRemove(APIWorkflowDetail updatedWorkflow, APIWorkflowDetail existingWorkflow) {

		APIWorkflowDetailParser updatedParser = new APIWorkflowDetailParser(updatedWorkflow);

		APIWorkflowDetailParser existingParser = new APIWorkflowDetailParser(existingWorkflow);

		return existingParser.toRDFStatements().filter(
						statement -> !"http://essi-lab.eu/2020/02/d2k/eskos#sharedWith".equalsIgnoreCase(statement.getPredicate().stringValue()))
				.filter(

						existing -> {

							boolean r = !isIn(existing, updatedParser.toRDFStatements());

							if (r)
								logger.trace("{} will be removed", existing);

							return r;
						}

				).collect(Collectors.toSet());

	}

	Set<Statement> calculateToAdd(APIWorkflowDetail updatedWorkflow, APIWorkflowDetail existingWorkflow) {

		APIWorkflowDetailParser updatedParser = new APIWorkflowDetailParser(updatedWorkflow);

		APIWorkflowDetailParser existingParser = new APIWorkflowDetailParser(existingWorkflow);

		return updatedParser.toRDFStatements().filter(

				update -> {

					boolean r = !isIn(update, existingParser.toRDFStatements());

					if (r)
						logger.trace("{} will be added", update);

					return r;
				}

		).collect(Collectors.toSet());
	}

	@Override
	public void updateWorkflowProperties(APIWorkflowDetail updatedWorkflow, APIWorkflowDetail existingWorkflow) throws BPException {

		logger.debug("Comparing statements for update operation");

		Set<Statement> toremove = calculateToRemove(updatedWorkflow, existingWorkflow);
		Set<Statement> toadd = calculateToAdd(updatedWorkflow, existingWorkflow);

		addStatements(toadd.stream());

		removeStatements(toremove.stream());

	}

	@Override
	public Boolean supports(String storagetype) {
		return "rdf".equalsIgnoreCase(storagetype);
	}

	private boolean isIn(Statement existing, Stream<Statement> toRDFStatements) {

		return toRDFStatements.anyMatch(statement ->

				existing.getSubject().stringValue().equalsIgnoreCase(statement.getSubject().stringValue()) && //
						existing.getPredicate().stringValue().equalsIgnoreCase(statement.getPredicate().stringValue()) && //
						existing.getObject().stringValue().equalsIgnoreCase(statement.getObject().stringValue())

		);

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Override
	public List<ConfigurationParameter> configurationParameters() {
		return Arrays.asList(BPStaticConfigurationParameters.RDF_REPOSITORY.getParameter(),
				BPStaticConfigurationParameters.RDF_UPDATE_REPOSITORY.getParameter());
	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) throws BPException {
		String rdf = parameters.get(BPStaticConfigurationParameters.RDF_REPOSITORY.getParameter());
		String rdfupdate = parameters.get(BPStaticConfigurationParameters.RDF_UPDATE_REPOSITORY.getParameter());

		Repository repository = new SPARQLRepository(rdf, rdfupdate);
		setRepository(repository);
	}
}
