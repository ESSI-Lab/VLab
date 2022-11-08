package eu.essi_lab.vlab.ce.engine.services.rdf;

import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 * @author Mattia Santoro
 */
public class APIWorkflowDetailParser {

	private final Model model;
	private final String id;
	private final SimpleValueFactory factory;
	private static final String NO_VALUE = "No value found";

	public APIWorkflowDetailParser(String identifier, Model resultModel) {

		id = identifier;
		model = resultModel;
		factory = SimpleValueFactory.getInstance();

	}

	public APIWorkflowDetailParser(APIWorkflowDetail workflowDetail) {

		this(workflowDetail.getId(), new ModelBuilder().build());

		addStatements(workflowDetail);

	}

	private Set<Value> readObjects(IRI predicate) {
		return model.filter(factory.createIRI(id), predicate, null).objects();
	}

	private Optional<String> readFirstValue(IRI predicate) {

		return readObjects(predicate).stream().filter(value -> value instanceof Literal).findFirst().map(this::readString);

	}

	private String readString(Value v) {
		return v.stringValue();
	}

	public String getId() {
		return id;
	}

	public Optional<String> getTitle() {

		return readFirstValue(RDFS.LABEL);

	}

	public Optional<List<String>> getSharedWith() {

		Set<Value> obj = readObjects(factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.SHARED_WITH_PREDICATE));

		List<String> list = new ArrayList<>();
		Iterator<Value> it = obj.iterator();

		while (it.hasNext())
			list.add(it.next().stringValue());

		return Optional.of(list);

	}

	public Optional<String> getDescription() {

		return readFirstValue(DCTERMS.ABSTRACT);

	}

	public Optional<String> getUnderTest() {

		return readFirstValue(factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.UNDER_TEST_PREDICATE));

	}

	public Optional<String> getBpmnUrl() {
		return readFirstValue(factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.DESCRIBED_BY_PREDICATE));
	}

	public Optional<String> getModelDeveloper() {
		return readFirstValue(factory.createIRI(RDFQueryBuilder.DCE_NAMESPACE, RDFQueryBuilder.DEC_CREATOR_PREDICATE));
	}

	public Optional<String> getModelDeveloperOrg() {
		return readFirstValue(factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.MODEL_DEV_ORG_PREDICATE));
	}

	public Optional<String> getModelDeveloperEmail() {
		return readFirstValue(factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.MODEL_DEV_EMAIL_PREDICATE));
	}

	public APIWorkflowDetail getAPIWorkflowDetail() {

		APIWorkflowDetail workflow = new APIWorkflowDetail();

		workflow.setBpmn_url(getBpmnUrl().orElse(null));
		workflow.setId(getId());
		workflow.setDescription(getDescription().orElse(NO_VALUE));
		workflow.setModelDeveloper(getModelDeveloper().orElse(NO_VALUE));
		workflow.setModelDeveloperOrg(getModelDeveloperOrg().orElse(NO_VALUE));
		workflow.setModelDeveloperEmail(getModelDeveloperEmail().orElse(NO_VALUE));
		workflow.setName(getTitle().orElse(NO_VALUE));
		workflow.setSharedWith(getSharedWith().orElse(new ArrayList<>()));

		String ut = getUnderTest().orElse("false");
		workflow.setUnder_test(Boolean.valueOf(ut));

		return workflow;
	}

	public Stream<Statement> toRDFStatements() {

		return model.stream();

	}

	private void addStatements(APIWorkflowDetail bp) {

		IRI iri = factory.createIRI(bp.getId());

		model.add(iri, RDF.TYPE, factory.createIRI("http://eu.essi_lab.vlab.core/businessprocess#Business_Process"));

		model.add(iri, factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.DESCRIBED_BY_PREDICATE),
				factory.createLiteral(bp.getBpmn_url()));

		model.add(iri, DCTERMS.ABSTRACT, factory.createLiteral(bp.getDescription()));

		model.add(iri, factory.createIRI(RDFQueryBuilder.DCE_NAMESPACE, RDFQueryBuilder.DEC_CREATOR_PREDICATE),
				factory.createLiteral(bp.getModelDeveloper()));

		model.add(iri, factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.MODEL_DEV_ORG_PREDICATE),
				factory.createLiteral(bp.getModelDeveloperOrg()));

		model.add(iri, factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.MODEL_DEV_EMAIL_PREDICATE),
				factory.createLiteral(bp.getModelDeveloperEmail()));

		model.add(iri, RDFS.LABEL, factory.createLiteral(bp.getName()));

		model.add(iri, factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.UNDER_TEST_PREDICATE),
				factory.createLiteral(bp.isUnder_test()));

		bp.getSharedWith().forEach(s ->

				model.add(iri, factory.createIRI(RDFQueryBuilder.ESKOS_NAMESPACE, RDFQueryBuilder.SHARED_WITH_PREDICATE),
						factory.createLiteral(s))

		);

	}

}
