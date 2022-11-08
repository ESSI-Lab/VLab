package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.OutputIngestor;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import eu.essi_lab.vlab.core.engine.factory.StorageFactory;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class DefaultOutputIngestor implements OutputIngestor {

	private Logger logger = LogManager.getLogger(DefaultOutputIngestor.class);

	@Override
	public Boolean canIngest(BPOutputDescription output) {
		return output.getOutput().getValueSchema() == null || "url".equalsIgnoreCase(output.getOutput().getValueSchema())
				|| "wms".equalsIgnoreCase(output.getOutput().getValueSchema());
	}

	IBPOutputWebStorage getBPOutputWebStorage(String bucket) throws BPException {

		return new StorageFactory().getBPOutputWebStorage(bucket);

	}

	@Override
	public ContainerOrchestratorCommandResult ingest(BPOutputDescription output, String runid,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser pathParser) throws BPException {

		String outputPath = pathParser.getDockerContainerAbsolutePath(output);

		String bucket = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_S3_OUTPUT_BUCKET_NAME.getParameter());

		IBPOutputWebStorage outputWebStorage = getBPOutputWebStorage(bucket);

		String s3key = outputWebStorage.getKey(output.getOutput(), runid);

		logger.trace("Saving {} to bucket {} with key {}", outputPath, bucket, s3key);

		return commandExecutor.saveFileToWebStorage(outputPath, bucket, s3key, true, 1000L * 60 * 60);
	}

	@Override
	public ContainerOrchestratorCommandResult ingestArray(BPOutputDescription output, String runid,
			IContainerOrchestratorCommandExecutor commandExecutor, PathConventionParser pathParser) throws BPException {

		logger.info("Request to save {} [{}]", output.getOutput().getName(), output.getOutput().getId());

		String outputPath = pathParser.getDockerContainerAbsolutePath(output);

		String bucket = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.AWS_S3_OUTPUT_BUCKET_NAME.getParameter());

		IBPOutputWebStorage outputWebStorage = getBPOutputWebStorage(bucket);

		String s3BaseKey = outputWebStorage.getKey(output.getOutput(), runid);

		logger.trace("Saving {} to bucket {} with key {}", outputPath, bucket, s3BaseKey);

		return commandExecutor.saveFolderToWebStorage(outputPath, bucket, s3BaseKey, true, 1000L * 60 * 60);

	}
}
