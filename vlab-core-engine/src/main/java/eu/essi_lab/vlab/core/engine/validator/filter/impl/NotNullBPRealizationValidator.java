package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessage;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class NotNullBPRealizationValidator implements IBPRealizationValidatorFilter<BPRealization, ValidateRealizationResponse> {

	private Logger logger = LogManager.getLogger(NotNullBPRealizationValidator.class);

	public ValidateRealizationResponse validate(BPRealization r) {

		logger.debug("Validating Not Null");

		ValidateRealizationResponse response = new ValidateRealizationResponse();

		if (r != null) {

			response.setValid(true);

			logger.debug("Validation succeded");

			ValidationMessage msg = new ValidationMessage();

			msg.setType(ValidationMessageType.INFO);

			msg.setMessage("Not Null realization found");

			response.setMessages(Arrays.asList(msg));

		} else {

			response.setValid(false);

			logger.debug("Validation failed");

			ValidationMessage msg = new ValidationMessage();

			msg.setType(ValidationMessageType.ERROR);

			msg.setMessage("Null realization found");

			response.setMessages(Arrays.asList(msg));

		}

		logger.debug("Completed Not Null Validation with result: {}", response.getValid());

		return response;

	}
}
