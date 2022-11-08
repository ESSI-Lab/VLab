package eu.essi_lab.vlab.core.engine.validator;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.AdapterAvailableRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.NotNullBPRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.URIAccessibilityBPRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.URIBPRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.plan.ValidationPlan;
import eu.essi_lab.vlab.core.engine.validator.plan.impl.FilterChainValidationPlan;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mattia Santoro
 */
public class BPRealizationValidatorTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();
	private Logger logger = LoggerFactory.getLogger(BPRealizationValidatorTest.class);

	@Test
	public void testNullPlan() throws BPException {

		expected.expect(BPException.class);

		BPValidator<BPRealization, ValidateRealizationResponse> validator = new BPValidator(null);

		ValidateRealizationResponse response = validator.validate(null);

	}

	@Test
	public void testNotNullPlan() throws BPException {

		ValidationPlan plan = Mockito.mock(ValidationPlan.class);
		ValidateRealizationResponse response = new ValidateRealizationResponse();

		Mockito.doReturn(response).when(plan).apply(Mockito.any());

		BPValidator<BPRealization, ValidateRealizationResponse> validator = new BPValidator(plan);

		BPRealization realization = new BPRealization();

		ValidateRealizationResponse r = validator.validate(realization);

		assertNotNull(r);

	}

	@Test
	public void testFilterChainPlanWithNonValidURI() throws BPException {
		ValidationPlan plan = getPlan();

		BPValidator<BPRealization, ValidateRealizationResponse> validator = new BPValidator(plan);

		BPRealization realization = new BPRealization();

		ValidateRealizationResponse response = validator.validate(realization);

		assertNotNull(response);

		assertFalse("Expceted not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());

		logger.debug("Response Message: {}", response.getMessages().get(0).getMessage());

	}

	@Test
	public void testNonExistingFolder() throws BPException {

		ValidationPlan plan = getPlan();

		BPValidator<BPRealization, ValidateRealizationResponse> validator = new BPValidator(plan);

		String uri = "/nonexisting/";

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn(uri).when(realization).getRealizationURI();

		ValidateRealizationResponse response = validator.validate(realization);

		assertNotNull(response);

		assertFalse("Expceted not valid", response.getValid());

		assertEquals(ValidationMessageType.ERROR, response.getMessages().get(0).getType());

		logger.debug("Response Message: {}", response.getMessages().get(0).getMessage());

	}

	private ValidationPlan getPlan() {

		List<IBPRealizationValidatorFilter> chain = new ArrayList<>();

		chain.add(new NotNullBPRealizationValidator());
		chain.add(new URIBPRealizationValidator());
		chain.add(new URIAccessibilityBPRealizationValidator());
		chain.add(new AdapterAvailableRealizationValidator());

		ValidationPlan plan = new FilterChainValidationPlan(chain, Mockito.mock(ValidateRealizationResponse.class));
		return plan;
	}
}