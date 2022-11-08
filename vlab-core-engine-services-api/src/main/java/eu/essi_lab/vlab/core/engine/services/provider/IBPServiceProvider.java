package eu.essi_lab.vlab.core.engine.services.provider;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Mattia Santoro
 */
public interface IBPServiceProvider<S> {

	List<ServiceLoader.Provider<S>> bpServiceFilter(List<ServiceLoader.Provider<S>> availableImplementations, Class<S> service);
}
