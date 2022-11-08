package eu.essi_lab.vlab.ce.engine.services.es;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class AWSESUrlParser {

	private Logger logger = LogManager.getLogger(AWSESUrlParser.class);

	private final String endpoint;
	private static final String AWS_DOMAIN_HOST_SUFFIX_2 = ".es.amazonaws.com";
	private static final String AWS_DOMAIN_HOST_SUFFIX_1 = ".es.amazonaws.com/";
	private static final String HTTPS_PREFIX = "https://";

	public AWSESUrlParser(String url) {

		this.endpoint = url;
	}

	public boolean isAWSESEndpoint() {

		try {

			URL url = new URL(this.endpoint);

			String host = url.getHost();

			if (host.toLowerCase().contains(AWS_DOMAIN_HOST_SUFFIX_2))
				return true;

		} catch (MalformedURLException e) {
			logger.warn("Provided url {} is not well formed, returning false", endpoint, e);
		}

		return false;
	}

	private String removeUnneeded() {
		return this.endpoint.replace(AWS_DOMAIN_HOST_SUFFIX_1, "").replace(AWS_DOMAIN_HOST_SUFFIX_2, "").replace(HTTPS_PREFIX, "");
	}

	public String getDomainName() {

		String part = removeUnneeded().split("\\.")[0];

		return part.split("-")[1];

	}

	public String getRegion() {

		return removeUnneeded().split("\\.")[1];

	}

}
