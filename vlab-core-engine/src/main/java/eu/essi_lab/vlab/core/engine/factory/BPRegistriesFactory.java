package eu.essi_lab.vlab.core.engine.factory;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.utils.BPExceptionLogger;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import eu.essi_lab.vlab.core.engine.serviceloader.BPEngineServiceLoader;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPRegistriesFactory {

	private static BPRegistriesFactory instance;
	private Logger logger = LogManager.getLogger(BPRegistriesFactory.class);

	private BPRegistriesFactory() {
	}

	public static BPRegistriesFactory getFactory() {
		if (instance == null)
			instance = new BPRegistriesFactory();

		return instance;
	}

	public IBPRunRegistry getBPRunRegistry() throws BPException {

		Iterable<IBPRunRegistry> runregistryLoader = BPEngineServiceLoader.load(IBPRunRegistry.class);

		for (IBPRunRegistry bpRunRegistry : runregistryLoader) {
			try {
				bpRunRegistry.setBPRunStorage(new StorageFactory().getBPRunRegistryStorage());

				bpRunRegistry.setStatusRegistry(getBPRunStatusRegistry());

				return bpRunRegistry;

			} catch (BPException bpException) {

				BPExceptionLogger.logBPException(bpException, logger, Level.WARN);
			}
		}

		throw new BPException("No implementation of IBPRunRegistry was found", BPException.ERROR_CODES.BAD_CONFIGURATION);
	}

	public IBPRunStatusRegistry getBPRunStatusRegistry() throws BPException {

		Iterable<IBPRunStatusRegistry> runStatusRegistryLoader = BPEngineServiceLoader.load(IBPRunStatusRegistry.class);

		for (IBPRunStatusRegistry bpRunStatusRegistry : runStatusRegistryLoader) {

			try {
				bpRunStatusRegistry.setBPRunStatusStorage(new StorageFactory().getBPRunStatusStorage());
				return bpRunStatusRegistry;
			} catch (BPException bpException) {

				BPExceptionLogger.logBPException(bpException, logger, Level.WARN);
			}

		}

		throw new BPException("No implementation of IBPRunStatusRegistry was found", BPException.ERROR_CODES.BAD_CONFIGURATION);

	}

	public IExecutableBPRegistry getExecutableRegistry() throws BPException {

		Iterable<IExecutableBPRegistry> executableBPRegistries = BPEngineServiceLoader.load(IExecutableBPRegistry.class);

		for (IExecutableBPRegistry executableBPRegistry : executableBPRegistries) {
			try {
				executableBPRegistry.setBPExecutableStorage(new StorageFactory().getBPExecutableStorage());

				return executableBPRegistry;
			} catch (BPException bpException) {

				BPExceptionLogger.logBPException(bpException, logger, Level.WARN);
			}
		}

		throw new BPException("No implementation of IExecutableBPRegistry was found", BPException.ERROR_CODES.BAD_CONFIGURATION);

	}

	public IBPRunLogRegistry getBPRunLogRegistry() throws BPException {

		Iterable<IBPRunLogRegistry> runLogRegistries = BPEngineServiceLoader.load(IBPRunLogRegistry.class);

		for (IBPRunLogRegistry runLogRegistry : runLogRegistries) {
			try {
				runLogRegistry.setBPRunLogStorage(new StorageFactory().getBPRunLogStorage());

				return runLogRegistry;

			} catch (BPException bpException) {

				BPExceptionLogger.logBPException(bpException, logger, Level.WARN);
			}
		}

		throw new BPException("No implementation of IBPRunLogRegistry was found", BPException.ERROR_CODES.BAD_CONFIGURATION);

	}
}
