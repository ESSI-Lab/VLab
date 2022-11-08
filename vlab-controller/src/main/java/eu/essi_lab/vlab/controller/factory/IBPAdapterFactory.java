package eu.essi_lab.vlab.controller.factory;

import eu.essi_lab.vlab.controller.serviceloader.BPControllerServiceLoader;
import eu.essi_lab.vlab.controller.services.IBPAdapter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class IBPAdapterFactory {

	private static Logger logger = LogManager.getLogger(IBPAdapterFactory.class);

	private IBPAdapterFactory() {
	}

	/**
	 * Looks for an {@link IBPAdapter} which supports the given {@link BPRealization}. If more than one adapter is found, the first one is
	 * returned. If no adapter supports the given realization, a {@link BPException} is thrown.
	 *
	 * @param realization
	 * @return
	 * @throws BPException
	 */
	public static IBPAdapter getBPAdapter(BPRealization realization) throws BPException {

		Iterable<IBPAdapter> bpAdapters = BPControllerServiceLoader.load(IBPAdapter.class);

		for (IBPAdapter bpAdapter : bpAdapters) {

			try {

				logger.trace("Testing {} for realization URI {}", bpAdapter.getClass().getSimpleName(), realization.getRealizationURI());

				if (bpAdapter.supports(realization)) {
					logger.info("Found Adapter {} for realization {}", bpAdapter.getClass().getName(), realization.getRealizationURI());

					return bpAdapter;
				}

			} catch (Exception thr) {

				logger.warn("Exception testing adapter", thr);

			}
		}

		throw new BPException("No adapter was found for realization " + realization.getRealizationURI(),
				BPException.ERROR_CODES.NO_ADAPTER_AVAILABLE);

	}

}
