package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessage;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class URIBPRealizationValidator implements IBPRealizationValidatorFilter<BPRealization, ValidateRealizationResponse> {

	private Logger logger = LogManager.getLogger(URIBPRealizationValidator.class);

	public ValidateRealizationResponse validate(BPRealization r) {

		logger.debug("Validating URI: {}", r.getRealizationURI());

		ValidateRealizationResponse response = new ValidateRealizationResponse();

		String uriString = r.getRealizationURI();

		if (uriString != null && !"".equalsIgnoreCase(uriString)) {

			try {

				new URI(uriString);
				response.setValid(true);

				logger.debug("URI {} is valid", uriString);

				ValidationMessage msg = new ValidationMessage();

				msg.setType(ValidationMessageType.INFO);

				msg.setMessage("Valid URL found: " + uriString);

				response.setMessages(Arrays.asList(msg));

			} catch (URISyntaxException e) {

				logger.debug("Exception validating URI {}", uriString, e);

				setNotValid(response, uriString);

			}
		} else {
			logger.debug("Found null or empty uri {}", uriString);
			setNotValid(response, uriString);
		}

		logger.debug("Completed URI Validation with result: {}", response.getValid());

		return response;
	}

	private void setNotValid(ValidateRealizationResponse response, String uriString) {
		response.setValid(false);

		ValidationMessage msg = new ValidationMessage();

		msg.setType(ValidationMessageType.ERROR);

		msg.setMessage("Invalid URL found: " + uriString);

		response.setMessages(Arrays.asList(msg));
	}
}
