package eu.essi_lab.vlab.core.engine.factory;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.utils.BPExceptionLogger;
import eu.essi_lab.vlab.core.engine.serviceloader.BPEngineServiceLoader;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.engine.services.IBPQueueClient;
import eu.essi_lab.vlab.core.engine.services.IBPWorkflowRegistryStorage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class StorageFactory {

	private Logger logger = LogManager.getLogger(StorageFactory.class);

	public IBPRunStorage getBPRunRegistryStorage() throws BPException {

		String storagetype = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.STORAGE_BP_RUN_STORAGE_TYPE.getParameter());

		IBPQueueClient queueClient = getQueueClient();

		Iterable<IBPRunStorage> bpRunStorages = load(IBPRunStorage.class);

		for (IBPRunStorage bpRunStorage : bpRunStorages) {

			if (bpRunStorage.supports(storagetype)) {
				try {
					new ServiceConfigurator(bpRunStorage).configure();
					bpRunStorage.setQueueClient(queueClient);
					return bpRunStorage;
				} catch (BPException e) {

					BPExceptionLogger.logBPException(e, logger, Level.WARN);
				}
			}

		}

		throw new BPException("Bad Configuration of BP Run storage type " + storagetype, BPException.ERROR_CODES.BAD_CONFIGURATION);

	}

	public IBPQueueClient getQueueClient() throws BPException {

		String queueservicetype = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.BP_RUN_QUEQUE_SERVICE_TYPE.getParameter());

		Iterable<IBPQueueClient> clients = load(IBPQueueClient.class);

		for (IBPQueueClient client : clients) {

			if (client.supports(queueservicetype)) {

				try {
					new ServiceConfigurator(client).configure();

					client.quequeExists(false);
					return client;
				} catch (BPException e) {

					BPExceptionLogger.logBPException(e, logger, Level.WARN);
				}

			}
		}

		throw new BPException("Bad Configuration of IBPQueueClient with type " + queueservicetype,
				BPException.ERROR_CODES.BAD_CONFIGURATION);

	}

	public IBPRunStatusStorage getBPRunStatusStorage() throws BPException {

		Iterable<IBPRunStatusStorage> bpRunStatusStorages = load(IBPRunStatusStorage.class);

		for (IBPRunStatusStorage bpRunStatusStorage : bpRunStatusStorages) {
			if (bpRunStatusStorage.supports(null)) {
				try {
					new ServiceConfigurator(bpRunStatusStorage).configure();
					return bpRunStatusStorage;
				} catch (BPException e) {

					BPExceptionLogger.logBPException(e, logger, Level.WARN);
				}
			}
		}
		throw new BPException("Bad Configuration of IBPRunStatusStorage", BPException.ERROR_CODES.BAD_CONFIGURATION);

	}

	<T> Iterable<T> load(Class<T> service) {
		return BPEngineServiceLoader.load(service);
	}

	public IBPWorkflowRegistryStorage getBPExecutableStorage() throws BPException {

		String type = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.BP_EXECUTABLE_STORAGE_TYPE.getParameter());

		Iterable<IBPWorkflowRegistryStorage> bpWorkflowRegistryStorages = load(IBPWorkflowRegistryStorage.class);

		for (IBPWorkflowRegistryStorage bpWorkflowRegistryStorage : bpWorkflowRegistryStorages) {

			if (bpWorkflowRegistryStorage.supports(type)) {
				try {
					new ServiceConfigurator(bpWorkflowRegistryStorage).configure();
					return bpWorkflowRegistryStorage;
				} catch (BPException e) {

					BPExceptionLogger.logBPException(e, logger, Level.WARN);
				}
			}
		}

		throw new BPException("Bad Configuration of IBPWorkflowRegistryStorage with type " + type,
				BPException.ERROR_CODES.BAD_CONFIGURATION);

	}

	public IBPRunLogStorage getBPRunLogStorage() throws BPException {

		String type = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.LOG_BACKEND_TYPE.getParameter());

		logger.trace("Log Storage type {}", type);

		Iterable<IBPRunLogStorage> bpWorkflowRegistryStorages = load(IBPRunLogStorage.class);

		for (IBPRunLogStorage bpWorkflowRegistryStorage : bpWorkflowRegistryStorages) {

			logger.trace("Testing {}", bpWorkflowRegistryStorage.getClass().getSimpleName());

			if (bpWorkflowRegistryStorage.supports(type)) {
				try {
					new ServiceConfigurator(bpWorkflowRegistryStorage).configure();
					return bpWorkflowRegistryStorage;
				} catch (BPException e) {

					BPExceptionLogger.logBPException(e, logger, Level.WARN);
				}
			}
		}

		throw new BPException("Bad Configuration of IBPRunLogStorage", BPException.ERROR_CODES.BAD_CONFIGURATION);

	}

	public IWebStorage getWebStorage(String bucketName) throws BPException {

		Iterable<IWebStorage> webStorages = load(IWebStorage.class);

		for (IWebStorage webStorage : webStorages) {

			if (webStorage.supports(null)) {
				logger.trace("Configuring web storage from class {}", webStorage.getClass().getSimpleName());
				try {
					new ServiceConfigurator(webStorage).configure();
					webStorage.setBucket(bucketName);
					return webStorage;
				} catch (BPException e) {

					BPExceptionLogger.logBPException(e, logger, Level.WARN);
				}
			}
		}

		throw new BPException("Bad Configuration of IWebStorage", BPException.ERROR_CODES.BAD_CONFIGURATION);

	}



	public IBPOutputWebStorage getBPOutputWebStorage(String bucketName) throws BPException {

		Iterable<IBPOutputWebStorage> webStorages = load(IBPOutputWebStorage.class);

		for (IBPOutputWebStorage webStorage : webStorages) {

			if (webStorage.supports(null)) {
				logger.trace("Configuring bo output web storage from class {}", webStorage.getClass().getSimpleName());
				try {
					new ServiceConfigurator(webStorage).configure();
					webStorage.setBucket(bucketName);
					return webStorage;
				} catch (BPException e) {

					BPExceptionLogger.logBPException(e, logger, Level.WARN);
				}
			}
		}

		throw new BPException("Bad Configuration of IBPOutputWebStorage", BPException.ERROR_CODES.BAD_CONFIGURATION);

	}
}
