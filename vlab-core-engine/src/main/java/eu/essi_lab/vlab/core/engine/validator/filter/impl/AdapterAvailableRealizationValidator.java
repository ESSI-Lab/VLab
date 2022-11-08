package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessage;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import eu.essi_lab.vlab.core.engine.factory.ISourceCodeConectorFactory;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class AdapterAvailableRealizationValidator implements IBPRealizationValidatorFilter<BPRealization, ValidateRealizationResponse> {

	private Logger logger = LogManager.getLogger(AdapterAvailableRealizationValidator.class);
	private ISourceCodeConnector adapter;
	private ISourceCodeConventionFileLoader loader;

	@Override
	public ValidateRealizationResponse validate(BPRealization r) {

		logger.debug("Validating Adapter Availability: {}", r.getRealizationURI());

		ValidateRealizationResponse response = new ValidateRealizationResponse();

		AdapterFound found = findAdapter(r);

		if (found.getAdapter() == null) {

			response.setValid(false);

			logger.debug("No adapter was found for {}", r.getRealizationURI());

			ValidationMessage msg = new ValidationMessage();

			msg.setType(ValidationMessageType.ERROR);

			msg.setMessage(found.getMsg());

			response.setMessages(Arrays.asList(msg));

		} else {

			response.setValid(true);

			logger.debug("Found adapter {} for {}", found.getAdapter().getClass().getName(), r.getRealizationURI());

			ValidationMessage msg = new ValidationMessage();

			msg.setType(ValidationMessageType.INFO);

			msg.setMessage("Found adapter " + found.getAdapter().getClass().getName());

			response.setMessages(Arrays.asList(msg));

		}

		logger.debug("Completed Adapter Validation with result: {}", response.getValid());

		return response;
	}

	public AdapterFound findAdapter(BPRealization realization) {

		AdapterFound found = new AdapterFound();

		logger.trace("Looking for IBPAdapter Started");

		adapter = null;

		try {

			adapter = new ISourceCodeConectorFactory().getConnector(realization);

			found.setAdapter(adapter);

			setLoader(new ISourceCodeConectorFactory().getSourceCodeConventionFileLoader(adapter.getDir().getAbsolutePath()));

		} catch (BPException e) {

			logger.debug("Error code {} with message {}", e.getErroCode(), e.getMessage());

			found.setMsg(e.getUserMessage());

		}

		logger.trace("Looking for IBPAdapter Completed");

		return found;
	}

	public ISourceCodeConnector getAdapter() {
		return adapter;
	}

	public ISourceCodeConventionFileLoader getLoader() {
		return loader;
	}

	public void setLoader(ISourceCodeConventionFileLoader loader) {
		this.loader = loader;
	}
}
