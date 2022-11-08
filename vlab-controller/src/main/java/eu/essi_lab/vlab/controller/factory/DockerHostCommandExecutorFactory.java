package eu.essi_lab.vlab.controller.factory;

import eu.essi_lab.vlab.controller.executors.SourceCodeExecutor;
import eu.essi_lab.vlab.controller.serviceloader.BPControllerServiceLoader;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.utils.BPExceptionLogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class DockerHostCommandExecutorFactory {

	private Logger logger = LogManager.getLogger(DockerHostCommandExecutorFactory.class);

	public IContainerOrchestratorManager getExecutor(BPComputeInfrastructure computeInfrastructure) throws BPException {

		return getExecutor(computeInfrastructure, true);

	}

	public IContainerOrchestratorManager getExecutor(BPComputeInfrastructure computeInfrastructure, Boolean testConnection)
			throws BPException {

		logger.info("Configured computing infrastructure {}", computeInfrastructure.getType());

		Iterable<IContainerOrchestratorManager> managers = BPControllerServiceLoader.load(IContainerOrchestratorManager.class);

		for (IContainerOrchestratorManager manager : managers) {

			logger.trace("Testing {}", manager.getClass().getSimpleName());

			if (manager.supports(computeInfrastructure)) {

				logger.trace("Setting up {}", manager.getClass().getSimpleName());

				manager.setBPInfrastructure(computeInfrastructure);
				manager.setRootExecutionFolder(SourceCodeExecutor.VLAB_ROOT_FOLDER);
				manager.initialize();

				if (Boolean.TRUE.equals(testConnection)) {

					logger.trace("Connection test of {}", manager.getClass().getSimpleName());

					try {
						manager.getExecutor().testConnection();

						return manager;
					} catch (BPException ex) {
						BPExceptionLogger.logBPException(ex, logger, Level.WARN);
					}

				} else
					return manager;
			}

		}

		throw new BPException(
				"No Container Orchestration Manager was found for compute infrastructure with type " + computeInfrastructure.getType(),
				BPException.ERROR_CODES.NO_CONTAINER_ORCHESTRATOR_MANAGER);

	}

}
