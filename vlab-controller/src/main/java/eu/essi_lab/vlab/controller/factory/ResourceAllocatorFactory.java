package eu.essi_lab.vlab.controller.factory;

import eu.essi_lab.vlab.controller.serviceloader.BPControllerServiceLoader;
import eu.essi_lab.vlab.controller.services.IResourceAllocator;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class ResourceAllocatorFactory {

	private static Logger logger = LogManager.getLogger(ResourceAllocatorFactory.class);

	private ResourceAllocatorFactory() {
		//force use as static
	}

	public static IResourceAllocator getResourceAllocator(BPComputeInfrastructure infra) throws BPException {

		logger.info("Configured computing infrastructure {}", infra.getType());

		Iterable<IResourceAllocator> services = BPControllerServiceLoader.load(IResourceAllocator.class);

		for (IResourceAllocator resourceAllocator : services)
			if (resourceAllocator.supports(infra)) {
				resourceAllocator.setInfra(infra);
				return resourceAllocator;
			}
		throw new BPException("Unsupported infrastructure type " + infra.getType() + " - can not instantiate resource allocator",
				BPException.ERROR_CODES.UNSUPPORTED_EXECUTION_INFRASTRUCTURE);
	}
}




