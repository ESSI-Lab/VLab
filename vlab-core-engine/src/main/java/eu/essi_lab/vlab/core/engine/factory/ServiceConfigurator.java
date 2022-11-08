package eu.essi_lab.vlab.core.engine.factory;

import eu.essi_lab.vlab.core.engine.services.IBPConfigurableService;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mattia Santoro
 */
public class ServiceConfigurator {

	private final IBPConfigurableService service;

	public ServiceConfigurator(IBPConfigurableService configurableService) {
		this.service = configurableService;
	}

	public void configure() throws BPException {
		Map<ConfigurationParameter, String> map = new HashMap<>();

		this.service.configurationParameters().forEach(configurationParameter -> map.put(configurationParameter,
				ConfigurationLoader.loadConfigurationParameter(configurationParameter)));

		this.service.configure(map);

	}
}
