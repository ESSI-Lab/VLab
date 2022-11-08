package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessage;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import eu.essi_lab.vlab.core.utils.TrustedHttpClientBuilder;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class URIAccessibilityBPRealizationValidator implements IBPRealizationValidatorFilter<BPRealization, ValidateRealizationResponse> {

	private Logger logger = LogManager.getLogger(URIAccessibilityBPRealizationValidator.class);

	@Override
	public ValidateRealizationResponse validate(BPRealization r) {

		logger.debug("Validating URI accessibility: {}", r.getRealizationURI());

		ValidateRealizationResponse response = new ValidateRealizationResponse();

		String lc = r.getRealizationURI();

		if (isLocalURI(lc)) {

			if (new File(lc).exists()) {

				response.setValid(true);

				logger.debug("URI {} is accessible", r.getRealizationURI());

				ValidationMessage msg = new ValidationMessage();

				msg.setType(ValidationMessageType.INFO);

				msg.setMessage("Accessible URL found: " + r.getRealizationURI());

				response.setMessages(Arrays.asList(msg));
			} else
				setNotValid(response, lc);
		} else {

			try {

				HttpGet get = new HttpGet(lc);

				HttpClient client = new TrustedHttpClientBuilder().build();
				int code = client.execute(get).getStatusLine().getStatusCode();

				response.setValid(true);

				logger.debug("URI {} is accessible with code {}", r.getRealizationURI(), code);

				ValidationMessage msg = new ValidationMessage();

				msg.setType(ValidationMessageType.INFO);

				msg.setMessage("Accessible URL found: " + r.getRealizationURI());

				response.setMessages(Arrays.asList(msg));

			} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
				setNotValid(response, "Not accessible URL found: " + r.getRealizationURI() + " with error " + e.getMessage());
				logger.debug("Not Accessible URI {}", lc, e);
			}

		}

		logger.debug("Completed URI Accessibility validation with result: {}", response.getValid());

		return response;
	}

	private void setNotValid(ValidateRealizationResponse response, String message) {

		response.setValid(false);

		ValidationMessage msg = new ValidationMessage();

		msg.setType(ValidationMessageType.ERROR);

		msg.setMessage(message);

		response.setMessages(Arrays.asList(msg));
	}

	private boolean isLocalURI(String lc) {
		return lc.contains("file://") || lc.startsWith("/");
	}
}
