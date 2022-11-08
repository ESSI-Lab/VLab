package eu.essi_lab.vlab.controller.executors;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import eu.essi_lab.vlab.core.engine.factory.StorageFactory;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class SourceCodeExecutorResourceCleaner {

	private final String runid;
	private Logger logger = LogManager.getLogger(SourceCodeExecutorResourceCleaner.class);

	public SourceCodeExecutorResourceCleaner(String runid) {
		this.runid = runid;
	}

	public void cleanAll(IContainerOrchestratorManager manager) throws BPException {

		logger.info("Cleaning all SourceCodeExecutor resources associated with BPRun {}", runid);

		var err = "";

		try {

			cleanOutputData();

		} catch (BPException e) {
			err = "Excpetion deleting output data of run " + runid + ": " + e.getMessage();
			logger.error(err);
		}

		try {
			cleanDockerContainerExecutionFolder(manager.getExecutor());
		} catch (BPException e) {
			if (!err.equalsIgnoreCase(""))
				err += " --> ";

			err += "Excpetion deleting docker container execution folder of run " + runid + ": " + e.getMessage();

			logger.error(err);
		}

		if (!err.equalsIgnoreCase(""))
			throw new BPException(err, BPException.ERROR_CODES.CLEAN_RESOURCE_EXCEPTION.getCode());

		cleanExecutionLog();

		cleanManagerResources(manager);

		logger.info("Successfully cleaned all SourceCodeExecutor resources associated with BPRun {}", runid);

	}

	public void cleanManagerResources(IContainerOrchestratorManager manager) throws BPException {
		manager.cleanResources();
	}

	public void cleanOutputData() throws BPException {

		logger.debug("Deleting all output data associated with BPRun {}", runid);

		var client = new StorageFactory().getWebStorage(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_OUTPUT_BUCKET_NAME.getParameter()));

		List<String> keys = client.listSubOjects(runid);

		for (String key : keys) {

			logger.trace("Deleting {}", key);

			//			client.remove(key);

		}

		logger.debug("Deleted all output data associated with BPRun {}", runid);

	}

	public void cleanExecutionLog() {
		//TODO

	}

	public void cleanDockerContainerExecutionFolder(IContainerOrchestratorCommandExecutor executor) throws BPException {

		String folder = new PathConventionParser(runid, SourceCodeExecutor.VLAB_ROOT_FOLDER).getRunFolder();

		logger.debug("Deleting DockerContainerExecutionFolder of run {} @ {}", runid, folder);

		ContainerOrchestratorCommandResult result = executor.removeDirectory(folder, 1000L * 60 * 5);

		if (!result.isSuccess()) {

			throw new BPException("Can not clean folder for run " + runid + ": " + result.getMessage());

		}
		logger.debug("Successfully deleted DockerContainerExecutionFolder of run {} @ {}", runid, folder);
	}

}
