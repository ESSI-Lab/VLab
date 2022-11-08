package eu.essi_lab.vlab.web.ce;

/**
 * @author Mattia Santoro
 */

import eu.essi_lab.vlab.core.engine.services.provider.IBPServiceProvider;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Mattia Santoro
 */
public class CEBPServiceProvider<S> implements IBPServiceProvider<S> {

	@Override
	public List<ServiceLoader.Provider<S>> bpServiceFilter(List<ServiceLoader.Provider<S>> availableServices, Class<S> service) {

		return availableServices;

	}
}
