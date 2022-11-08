package eu.essi_lab.vlab.core.engine.serviceloader;

import eu.essi_lab.vlab.core.engine.services.provider.IBPServiceProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author Mattia Santoro
 */
public class BPEngineServiceLoader {

	private BPEngineServiceLoader() {
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
