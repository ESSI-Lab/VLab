package eu.essi_lab.vlab.core.engine.validator;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BasicValidationResponse;
import eu.essi_lab.vlab.core.engine.validator.plan.ValidationPlan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPValidator<T, R extends BasicValidationResponse> {

	private Logger logger = LogManager.getLogger(BPValidator.class);
	private final ValidationPlan<T, R> plan;

	public BPValidator(ValidationPlan<T, R> validationPlan) {
		plan = validationPlan;
	}

	public R validate(T tobevalidated) throws BPException {

		logger.info("Starting validation");

		if (plan == null) {

			logger.error("Found null validation plan");

			throw new BPException("Internal error: null validation plan", BPException.ERROR_CODES.NO_VALIDATION_PLAN);

		}

		R response = plan.apply(tobevalidated);

		logger.info("Completed validation");

		return response;

	}

}
