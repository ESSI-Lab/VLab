package eu.essi_lab.vlab.core.engine.validator.plan.impl;

import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class FilterChainValidationPlanTest {

	@Test
	public void testOrder() {

		List<IBPRealizationValidatorFilter> chain = new ArrayList<>();

		ValidateRealizationResponse r = new ValidateRealizationResponse();

		r.setValid(false);

		IBPRealizationValidatorFilter first = Mockito.mock(IBPRealizationValidatorFilter.class);
		Mockito.doReturn(r).when(first).validate(Mockito.any());
		chain.add(first);

		IBPRealizationValidatorFilter second = Mockito.mock(IBPRealizationValidatorFilter.class);
		Mockito.doReturn(r).when(second).validate(Mockito.any());
		chain.add(second);

		FilterChainValidationPlan plan = new FilterChainValidationPlan(chain, Mockito.mock(ValidateRealizationResponse.class));

		plan.apply(null);

		Mockito.verify(first, Mockito.times(1)).validate(Mockito.any());
		Mockito.verify(second, Mockito.times(0)).validate(Mockito.any());
	}
}