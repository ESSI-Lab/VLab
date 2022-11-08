package eu.essi_lab.vlab.controller.serviceloader;

import eu.essi_lab.vlab.core.engine.services.provider.IBPServiceProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * This is needed to enable the loading of controller services implementations which can't be loaded via the BPEngineServiceLoader because
 * the core engine module can't declare the "use" of such services since they are defined in the vlab.controller.services.api module (not
 * imported by core engine)
 *
 * @author Mattia Santoro
 */
public class BPControllerServiceLoader {

	private BPControllerServiceLoader() {
		//force static usage
	}

	public static <T> Iterable<T> load(Class<T> service) {

		ServiceLoader<IBPServiceProvider> services = ServiceLoader.load(IBPServiceProvider.class);

		if (!services.iterator().hasNext())
			return new ArrayList<>();

		IBPServiceProvider bpServiceProvider = services.iterator().next();

		List<ServiceLoader.Provider<T>> filtered = bpServiceProvider.bpServiceFilter(ServiceLoader.load(service).stream()
				.collect(Collectors.toList()), service);

		return filtered.stream().map(i -> i.get()).collect(Collectors.toList());

	}
}
