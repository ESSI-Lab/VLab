package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.util.List;
import java.util.Map;

/**
 * @author Mattia Santoro
 */
public interface IBPConfigurableService {

	List<ConfigurationParameter> configurationParameters();

	void configure(Map<ConfigurationParameter, String> parameters) throws BPException;

	Boolean supports(String type);

}
