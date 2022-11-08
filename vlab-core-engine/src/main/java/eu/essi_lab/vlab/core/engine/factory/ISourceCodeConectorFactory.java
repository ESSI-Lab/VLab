package eu.essi_lab.vlab.core.engine.factory;

import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import eu.essi_lab.vlab.core.engine.serviceloader.BPEngineServiceLoader;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class ISourceCodeConectorFactory {

	private Logger logger = LogManager.getLogger(ISourceCodeConectorFactory.class);

	Iterable<ISourceCodeConnector> loadConnectors() {
		return BPEngineServiceLoader.load(ISourceCodeConnector.class);
	}

	public ISourceCodeConnector getConnector(BPRealization realization) throws BPException {

		Iterable<ISourceCodeConnector> sourceCodeConnectors = loadConnectors();

		SupportResponse nearest = null;

		for (ISourceCodeConnector sourceCodeConnector : sourceCodeConnectors) {

			try {

				logger.trace("Testing {} for realization URI {}", sourceCodeConnector.getClass().getSimpleName(),
						realization.getRealizationURI());

				SupportResponse supported = sourceCodeConnector.supports(realization);

				if (supported.isConventionsOk()) {
					logger.info("Found Source Code Connector {} for realization {}", sourceCodeConnector.getClass().getName(),
							realization.getRealizationURI());
					return sourceCodeConnector;
				}

				if (nearest == null)
					nearest = supported;
				else {

					if (!nearest.isCanRead() && supported.isCanRead())
						nearest = supported;

				}

			} catch (Exception thr) {

				logger.warn("Exception testing Source Code Connector", thr);

			}
		}

		BPException ex = new BPException("No Source Code Connector was found for realization " + realization.getRealizationURI(),
				BPException.ERROR_CODES.NO_ADAPTER_AVAILABLE);

		if (nearest != null)
			ex.setUserMessage(nearest.getConventionError());
		else
			ex.setUserMessage("No Source Code Connector was found to read your implementation");

		throw ex;
	}

	public ISourceCodeConventionFileLoader getSourceCodeConventionFileLoader(String rootDirAbsPath) throws BPException {

		Iterator<ISourceCodeConventionFileLoader> iterator = BPEngineServiceLoader.load(ISourceCodeConventionFileLoader.class).iterator();

		if (iterator.hasNext()) {
			ISourceCodeConventionFileLoader loader = iterator.next();
			loader.setRootPath(rootDirAbsPath);
			return loader;
		}

		BPException exception = new BPException("No SourceCodeConventionFileLoader found",
				BPException.ERROR_CODES.NO_SOURCECODE_FILE_LOADER_FOUND);

		throw exception;
	}
}
