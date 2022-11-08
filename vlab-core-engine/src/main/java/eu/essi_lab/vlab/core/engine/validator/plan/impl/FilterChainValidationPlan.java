package eu.essi_lab.vlab.core.engine.validator.plan.impl;

import eu.essi_lab.vlab.core.datamodel.BasicValidationResponse;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import eu.essi_lab.vlab.core.engine.validator.plan.ValidationPlan;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class FilterChainValidationPlan<T, R extends BasicValidationResponse> implements ValidationPlan<T, R> {

	private final List<IBPRealizationValidatorFilter<T, R>> chain;

	private Logger logger = LogManager.getLogger(FilterChainValidationPlan.class);
	private R response;

	public FilterChainValidationPlan(List<IBPRealizationValidatorFilter<T, R>> filterChain, R initialResponse) {
		chain = filterChain;
		response = initialResponse;

	}

	@Override
	public R apply(T realization) {

		Iterator<IBPRealizationValidatorFilter<T, R>> it = chain.iterator();

		while (it.hasNext()) {

			IBPRealizationValidatorFilter<T, R> filter = it.next();

			logger.debug("Applying filter {}", filter.getClass().getName());

			response = filter.validate(realization);

			if (!response.getValid())
				return response;
		}

		return response;
	}

}
