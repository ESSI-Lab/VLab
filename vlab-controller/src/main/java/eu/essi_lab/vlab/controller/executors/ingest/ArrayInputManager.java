package eu.essi_lab.vlab.controller.executors.ingest;

import eu.essi_lab.vlab.controller.factory.IngestorFactory;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.controller.services.OutputIngestor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import java.io.File;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class ArrayInputManager {

	private final String runid;

	private PathConventionParser pathParser;
	private IContainerOrchestratorCommandExecutor dockerHost;

	private Logger logger = LogManager.getLogger(ArrayInputManager.class);
	private static final String EXECUTING_ING_OF_WITH_SHEMA = "Executing ingestion of {} with schema {} and type {} from {}";
	private int idx = 0;
	private BPRunStatus status;
	private static final String EXECUTING_SAVE_OF_WITH_SHEMA = "Executing save of {} with schema {} and type {} from {}";

	public ArrayInputManager(String runid) {
		this.runid = runid;
	}

	public ContainerOrchestratorCommandResult ingest(BPInputDescription defaultInput, BPInput bpInput) throws BPException {

		logger.info("Request to ingest {} [{}]", defaultInput.getInput().getName(), defaultInput.getInput().getId());

		if (bpInput == null) {
			return ingestDefault(defaultInput);
		}

		var interpretation = "extend";

		String userInterpretation = bpInput.getValueArrayInterpretation();

		if (userInterpretation != null) {
			logger.debug("Found user interpretation: {}", userInterpretation);

			interpretation = userInterpretation;
		}
		if ("replace".equalsIgnoreCase(interpretation))
			return ingetReplace(defaultInput, bpInput);

		/**
		 * From here is ingest extend
		 */

		var finalResult = new ContainerOrchestratorCommandResult();

		ContainerOrchestratorCommandResult defIngestResult = ingestDefault(defaultInput);

		if (!defIngestResult.isSuccess()) {

			finalResult.setSuccess(false);

			finalResult.setMessage("Error default input. " + defIngestResult.getMessage());

			return finalResult;

		}

		ContainerOrchestratorCommandResult userIngestResult = ingetReplace(defaultInput, bpInput);

		if (!userIngestResult.isSuccess()) {

			finalResult.setSuccess(false);

			finalResult.setMessage("Error ingesting user input. " + userIngestResult.getMessage());

			return finalResult;

		}

		finalResult.setSuccess(true);
		return finalResult;
	}

	private ContainerOrchestratorCommandResult ingetReplace(BPInputDescription defaultInput, BPInput bpInput) throws BPException {

		logger.debug("Ingesting replace {}", bpInput.getId());

		return executeIngets(bpInput.getValueArray(), bpInput.getValueType(), defaultInput);

	}

	private ContainerOrchestratorCommandResult executeIngets(List<Object> valueArray, String valueType,
			BPInputDescription defaultInputDescription) throws BPException {

		logger.debug("Executing ingestion from valueType: {}", valueType);

		var result = new ContainerOrchestratorCommandResult();

		for (Object val : valueArray) {

			String source = (String) val;
			String key = null;

			if ("keyValue".equalsIgnoreCase(valueType)) {

				key = ((String) val).split("=")[0];

				source = ((String) val).split("=")[1];
			}

			logger.trace("Found source {} and key {}", source, key);

			String target = pathParser.getDockerContainerAbsolutePath(defaultInputDescription);

			String suffix = null;

			if (key != null) {
				suffix = key;
				target += File.separator + key;
			}

			logger.trace("Target: {} and Suffix: {}", target, suffix);

			Optional<IndividualInputIngestor> ingestor = getIngestorFactory().getIngestor(defaultInputDescription);

			if (!ingestor.isPresent()) {
				logger.error("Unrecognized input with schema {} for input {} ({})", defaultInputDescription.getInput().getValueSchema(),
						defaultInputDescription.getInput().getName(), defaultInputDescription.getInput().getId());

				throw new BPException("Can't ingest input " + defaultInputDescription.getInput().getName(), BPException.ERROR_CODES.NO_INPUT_INGESTOR);
			}

			logger.trace(EXECUTING_ING_OF_WITH_SHEMA, defaultInputDescription.getInput().getName(), defaultInputDescription.getInput().getValueSchema(),
					defaultInputDescription, source);

			ContainerOrchestratorCommandResult singleResult = ingestor.get().ingest(defaultInputDescription, source, dockerHost,
					pathParser);

			if (!singleResult.isSuccess()) {
				logger.error("Failed to ingest {} with error: {}", source, singleResult.getMessage());

				result.setSuccess(false);
				result.setMessage("Failed to ingest " + source + " with error: " + singleResult.getMessage());

				return result;
			}

			idx++;
		}

		result.setSuccess(true);

		return result;

	}

	IngestorFactory getIngestorFactory() {
		return new IngestorFactory();
	}

	private ContainerOrchestratorCommandResult ingestDefault(BPInputDescription defaultInput) throws BPException {

		logger.debug("Ingesting default {}", defaultInput.getInput().getId());

		return executeIngets(defaultInput.getDefaultValueArray(), defaultInput.getInput().getValueType(), defaultInput);

	}

	public IContainerOrchestratorCommandExecutor getDockerHost() {
		return dockerHost;
	}

	public void setDockerHost(IContainerOrchestratorCommandExecutor dockerHost) {
		this.dockerHost = dockerHost;
	}

	public PathConventionParser getPathParser() {
		return pathParser;
	}

	public void setPathParser(PathConventionParser pathParser) {
		this.pathParser = pathParser;
	}

	public ContainerOrchestratorCommandResult save(BPOutputDescription output) throws BPException {

		Optional<OutputIngestor> ingestor = getIngestorFactory().getIngestor(output);

		if (!ingestor.isPresent()) {
			logger.error("Unrecognized output with schema {} for output {} ({})", output.getOutput().getValueSchema(), output.getOutput().getName(),
					output.getOutput().getId());

			throw new BPException("Can't save output " + output.getOutput().getName(), BPException.ERROR_CODES.NO_OUTPUT_INGESTOR);
		}

		logger.trace(EXECUTING_SAVE_OF_WITH_SHEMA, output.getOutput().getName(), output.getOutput().getValueSchema(), output.getOutput().getValueType(), output.getTarget());

		return ingestor.get().ingest(output, runid, dockerHost, pathParser);

	}

	public void setStatus(BPRunStatus status) {
		this.status = status;
	}
}
