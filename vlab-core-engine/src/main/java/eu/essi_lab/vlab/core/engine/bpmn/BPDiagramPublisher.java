package eu.essi_lab.vlab.core.engine.bpmn;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.factory.StorageFactory;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPDiagramPublisher {

	private final Logger logger = LogManager.getLogger(BPDiagramPublisher.class);

	private final String bucket;

	//TODO replace with url from conf
	private static final String S3_BASE = "https://s3.amazonaws.com/";
	private static final String PUBLISHED_BPMN_FOLDER = "published";

	public BPDiagramPublisher() {
		bucket = ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_EXECUTABLE_BP_BUCKET.getParameter());
	}

	public String publish(String identifier, InputStream stream) throws BPException {

		logger.debug("Publishing {}", identifier);

		IWebStorage webStorage = new StorageFactory().getWebStorage(bucket);

		webStorage.upload(stream, getKey(identifier));

		String url = getBPUrl(identifier);

		logger.debug("Published {} to {}", identifier, url);

		return url;
	}

	/**
	 * Returns the HTTPS url to download the bpmn for the given identifier.
	 *
	 * @param identifier
	 * @return
	 */
	public String getBPUrl(String identifier) {
		return S3_BASE + bucket + "/" + getKey(identifier);
	}

	/**
	 * Returns the S3 Object key corresponding to the given identifier.
	 *
	 * @param identifier
	 * @return
	 */
	public String getKey(String identifier) {
		return PUBLISHED_BPMN_FOLDER + "/" + identifier + ".bpmn";
	}
}
