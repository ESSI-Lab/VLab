package eu.essi_lab.vlab.controller.executors.ingest;

import eu.essi_lab.vlab.controller.factory.IngestorFactory;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.controller.services.OutputIngestor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class IndividualInputManager {

	private final String runid;

	private PathConventionParser pathParser;
	private IContainerOrchestratorCommandExecutor dockerHost;

	private Logger logger = LogManager.getLogger(IndividualInputManager.class);

	private static final String EXECUTING_ING_OF_WITH_SHEMA = "Executing ingestion of {} with schema {} and type {} from {}";
	private static final String EXECUTING_SAVE_OF_WITH_SHEMA = "Executing save of {} with schema {} and type {} from {}";

	public IndividualInputManager(String runid) {
		this.runid = runid;

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

	public ContainerOrchestratorCommandResult createFolder(BPOutputDescription output) throws BPException {

		logger.trace("Required folder creation of {} [{}]", output.getOutput().getName(), output.getOutput().getId());

		if (output.getTarget() == null || "".equalsIgnoreCase(output.getTarget()))
			throw new BPException("Can't find output target for " + output.getOutput().getName() + " [" + output.getOutput().getId() + "]",
					BPException.ERROR_CODES.INVALID_REQUEST.getCode());

		return dockerHost.createDirectory(pathParser.getDockerContainerAbsolutePath(output), 1000L * 60 * 60);

	}

	String sourceValue(BPInputDescription defaultInput, BPInput bpInput) {

		logger.trace("Reading actual value of input {}", defaultInput.getInput().getId());

		String defaultValue = defaultInput.getDefaultValue();

		String defaultValueType = defaultInput.getInput().getValueType();

		String source = defaultValue;

		logger.trace("Raw default value of input {} is {}", defaultInput.getInput().getId(), source);

		if ("keyValue".equalsIgnoreCase(defaultValueType) && defaultValue != null) {
			source = defaultValue.split("=")[1];
		}

		logger.trace("Default value of input {} is {}", defaultInput.getInput().getId(), source);

		if (bpInput != null) {

			logger.trace("Found user input object for {}", defaultInput.getInput().getId());

			String userValue = (String) bpInput.getValue();

			if (userValue != null && !"null".equalsIgnoreCase(userValue)) {

				logger.trace("Found user value of input {}", defaultInput.getInput().getId());

				source = userValue;

				logger.trace("Raw user value of input {} is {}", defaultInput.getInput().getId(), source);

				if ("keyValue".equalsIgnoreCase(bpInput.getValueType())) {
					source = userValue.split("=")[1];
				}

				logger.trace("User value of input {} is {}", defaultInput.getInput().getId(), source);
			}
		}

		logger.trace("Returning value {} for input {}", source, defaultInput.getInput().getId());

		return source;
	}

	public ContainerOrchestratorCommandResult ingest(BPInputDescription defaultInput, BPInput bpInput) throws BPException {

		String defaultValueType = defaultInput.getInput().getValueType();

		logger.trace("Required ingestion of {} with type {}", defaultInput.getInput().getName(), defaultValueType);

		String source = sourceValue(defaultInput, bpInput);

		if (source == null || "".equalsIgnoreCase(source)) {

			if (Boolean.TRUE.equals(defaultInput.getInput().getObligation())) {
				throw new BPException("Can't find input value for " + defaultInput.getInput().getName() + " [" + defaultInput.getInput().getId() + "]",
						BPException.ERROR_CODES.INVALID_REQUEST.getCode());
			} else {
				var result = new ContainerOrchestratorCommandResult();

				result.setSuccess(true);
				result.setMessage("No value found for optional input " + defaultInput.getInput().getName() + " [" + defaultInput.getInput().getId() + "]");
				return result;
			}
		}

		Optional<IndividualInputIngestor> ingestor = getIngestorFactory().getIngestor(defaultInput);

		if (!ingestor.isPresent()) {
			logger.error("Unrecognized input with schema {} for input {} ({})", defaultInput.getInput().getValueSchema(), defaultInput.getInput().getName(),
					defaultInput.getInput().getId());

			throw new BPException("Can't ingest input " + defaultInput.getInput().getName(), BPException.ERROR_CODES.NO_INPUT_INGESTOR);
		}

		logger.trace(EXECUTING_ING_OF_WITH_SHEMA, defaultInput.getInput().getName(), defaultInput.getInput().getValueSchema(), defaultValueType, source);
		return ingestor.get().ingest(defaultInput, source, dockerHost, pathParser);

	}

	IngestorFactory getIngestorFactory() {
		return new IngestorFactory();
	}

	public PathConventionParser getPathParser() {
		return pathParser;
	}

	public void setPathParser(PathConventionParser pathParser) {
		this.pathParser = pathParser;
	}

	public void setDockerHost(IContainerOrchestratorCommandExecutor dockerHost) {
		this.dockerHost = dockerHost;
	}

	public IContainerOrchestratorCommandExecutor getDockerHost() {
		return dockerHost;
	}

}
