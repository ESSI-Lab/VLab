package eu.essi_lab.vlab.core.engine;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.factory.ISourceCodeConectorFactory;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.engine.bpmn.BPDiagramGenerator;
import eu.essi_lab.vlab.core.engine.bpmn.BPDiagramPublisher;
import eu.essi_lab.vlab.core.engine.bpmn.BPModelGenerator;
import eu.essi_lab.vlab.core.engine.validator.BPValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.AdapterAvailableRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.NotNullBPRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.URIAccessibilityBPRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.URIBPRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.VLABConventionValidator;
import eu.essi_lab.vlab.core.engine.validator.plan.ValidationPlan;
import eu.essi_lab.vlab.core.engine.validator.plan.impl.FilterChainValidationPlan;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPPreprocessor {

	private final Logger logger = LogManager.getLogger(BPPreprocessor.class);
	private static final String NO_NAME_PROVIDED = "No name provided";
	private static final String NO_DESCRIPTION_PROVIDED = "No description provided";
	private static final String ESSI_WORKFLOW_ID_PREFIX = "http://eu.essi_lab.vlab.core/workflow/";

	public ValidateRealizationResponse execValidation(ValidateRealizationRequest request) throws BPException {

		ValidateRealizationResponse response = validateRealization(request);

		if (Boolean.FALSE.equals(response.getValid()))
			return response;

		APIWorkflowDetail workflow = createWorkflow(request);

		response.setWorkflow(workflow);

		return response;
	}

	ValidateRealizationResponse validateRealization(ValidateRealizationRequest request) throws BPException {

		logger.info("Starting PreProcessing action: validation");

		ValidationPlan<BPRealization, ValidateRealizationResponse> plan = createDefaultValidationPlan();

		BPValidator<BPRealization, ValidateRealizationResponse> validator = new BPValidator<>(plan);

		ValidateRealizationResponse validatorResponse = validator.validate(request.getRealization());

		logger.info("Completed PreProcessing action: validation");

		return validatorResponse;
	}

	BPModelGenerator instantiateGenerator() {
		return new BPModelGenerator();
	}

	ISourceCodeConventionFileLoader getISourceCodeConventionFileLoader(String path) throws BPException {
		return new ISourceCodeConectorFactory().getSourceCodeConventionFileLoader(path);
	}

	public APIWorkflowDetail createWorkflow(ValidateRealizationRequest request) throws BPException {

		logger.info("Starting PreProcessing action: bpmn model generation");

		BPModelGenerator modelGenerator = instantiateGenerator();

		AtomicExecutableBP bpmn = modelGenerator.createModel(request);

		logger.info("Completed PreProcessing action: bpmn model generation");

		logger.info("Starting PreProcessing action: bpmn diagram generation");

		BPDiagramGenerator generator = new BPDiagramGenerator(bpmn);

		AtomicExecutableBP diagram = generator.createDiagram();

		logger.info("Completed PreProcessing action: bpmn diagram generation");

		logger.info("Starting PreProcessing action: bpmn diagram publication");

		String url = publishDiagram(bpmn, diagram);

		logger.info("Completed PreProcessing action: bpmn diagram publication");

		APIWorkflowDetail workflowDetail = new APIWorkflowDetail();

		workflowDetail.setId(ESSI_WORKFLOW_ID_PREFIX + bpmn.getId());

		workflowDetail.setDescription(request.getModelDescription() == null ? NO_DESCRIPTION_PROVIDED : request.getModelDescription());

		workflowDetail.setName(request.getModelName() == null ? NO_NAME_PROVIDED : request.getModelName());

		workflowDetail.setBpmn_url(url);

		workflowDetail.setModelDeveloper(request.getModelDeveloper());

		workflowDetail.setModelDeveloperEmail(request.getModelDeveloperEmail());

		workflowDetail.setModelDeveloperOrg(request.getModelDeveloperOrg());

		return workflowDetail;
	}

	String publishDiagram(AtomicExecutableBP bpmn, AtomicExecutableBP diagram) throws BPException {
		return new BPDiagramPublisher().publish(bpmn.getId(), diagram.asStream());
	}

	private ValidationPlan<BPRealization, ValidateRealizationResponse> createDefaultValidationPlan() throws BPException {

		logger.debug("Initializing Default Validation Plan");

		List<IBPRealizationValidatorFilter<BPRealization, ValidateRealizationResponse>> chain = new ArrayList<>();

		NotNullBPRealizationValidator v1 = new NotNullBPRealizationValidator();
		traceAdd(v1.getClass().getName());
		chain.add(v1);

		URIBPRealizationValidator v2 = new URIBPRealizationValidator();
		traceAdd(v2.getClass().getName());
		chain.add(v2);

		URIAccessibilityBPRealizationValidator v3 = new URIAccessibilityBPRealizationValidator();
		traceAdd(v3.getClass().getName());
		chain.add(v3);

		AdapterAvailableRealizationValidator v4 = instantiateAdapterValidator();
		traceAdd(v4.getClass().getName());
		chain.add(v4);

		VLABConventionValidator v5 = new VLABConventionValidator();

		v5.setAdapterValidator(v4);

		traceAdd(v5.getClass().getName());
		chain.add(v5);

		ValidationPlan<BPRealization, ValidateRealizationResponse> plan = new FilterChainValidationPlan<>(chain,
				new ValidateRealizationResponse());

		logger.debug("Default Validation Plan Initialized");

		return plan;
	}

	AdapterAvailableRealizationValidator instantiateAdapterValidator() {

		return new AdapterAvailableRealizationValidator();
	}

	private void traceAdd(String name) {
		logger.trace("Adding {}", name);
	}
}
