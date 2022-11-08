package eu.essi_lab.vlab.controller.factory;

import eu.essi_lab.vlab.controller.serviceloader.BPControllerServiceLoader;
import eu.essi_lab.vlab.controller.services.IBPComputeInfraProvider;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.factory.ServiceConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPComputeInfraProviderFactory {

	private Logger logger = LogManager.getLogger(BPComputeInfraProviderFactory.class);

	public IBPComputeInfraProvider getIBPComputeInfraProvider() throws BPException {

		String infraprovider = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.BP_COMPUTE_INFRA_PROVIDER.getParameter());

		logger.debug("Infrastructure provider {}", infraprovider);
		Iterable<IBPComputeInfraProvider> services = BPControllerServiceLoader.load(IBPComputeInfraProvider.class);

		for (IBPComputeInfraProvider provider : services)
			if (provider.supports(infraprovider)) {

				new ServiceConfigurator(provider).configure();
				return provider;
			}
		throw new BPException("Unknown infrastructure provider configuration " + infraprovider,
				BPException.ERROR_CODES.UNKNOWN_INRA_PROVIDER);

	}

}
