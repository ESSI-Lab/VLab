package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.SimpleNameValue;
import eu.essi_lab.vlab.core.datamodel.WMSValue;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * @author Mattia Santoro
 */
public class BPOutputWebStorage implements IBPOutputWebStorage {

	private String bucket;

	private Logger logger = LogManager.getLogger(BPOutputWebStorage.class);
	private String region;
	private String accessKey;
	private String secretKey;
	private String serviceUrl;

	private static final String VALUE_INFO = "Value {} of output {} ({}) of run {}";

	public List<ConfigurationParameter> configurationParameters() {

		return Arrays.asList(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter(),
				BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter());

	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) {
		setAccessKey(parameters.get(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter()));
		setSecretKey(parameters.get(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter()));

		setRegion(parameters.get(BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter()));

		Optional<String> s3_url = Optional.ofNullable(parameters.get(BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter()));
		setServiceUrl(s3_url.orElse(null));

	}

	@Override
	public Boolean supports(String type) {
		return Boolean.TRUE;
	}

	public String getKey(BPOutput output, String runid) {

		String type = output.getOutputType();

		String key = runid + "/" + output.getId();

		if ("array".equalsIgnoreCase(type)) {
			key = runid + "/" + output.getId() + "/";
		}

		logger.trace("Web Storage key of {} for run {} is {}", output.getId(), runid, key);
		return key;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public void addValue(BPOutput output, String runid, IWebStorage webStorage) throws BPException {

		String webBaseFinal = getWebBase();

		logger.trace("Web storage base {} of output {} ({}) of run {}", webBaseFinal, output.getName(), output.getId(), runid);

		String type = output.getOutputType();

		logger.trace("Type {} of output {} ({}) of run {}", type, output.getName(), output.getId(), runid);

		if ("array".equalsIgnoreCase(type)) {

			List<String> content = webStorage.listSubOjects(getKey(output, runid));

			List<Object> valueArray = new ArrayList<>();

			content.forEach(c -> {

				String name = c.split("/")[c.split("/").length - 1];

				if (name.equalsIgnoreCase("array.zip"))
					name = "All files";

				String url = getS3PublicBucketPrefix(webBaseFinal, runid) + c;

				SimpleNameValue snv = new SimpleNameValue();
				snv.setName(name);
				snv.setUrl(url);

				logger.trace(VALUE_INFO, name + ": " + url, output.getName(), output.getId(), runid);

				valueArray.add(snv);

			});

			output.setValueArray(valueArray);

			return;
		}

		String schema = output.getValueSchema();

		logger.trace("Schema {} of output {} ({}) of run {}", schema, output.getName(), output.getId(), runid);

		if ("url".equalsIgnoreCase(schema)) {

			String v = getS3PublicUrl(webBaseFinal, output, runid);

			logger.trace(VALUE_INFO, v, output.getName(), output.getId(), runid);

			output.setValue(v);
			output.setValueType("value");

		}

		if ("wms".equalsIgnoreCase(schema)) {

			String k = getKey(output, runid);
			InputStream stream = webStorage.read(k);

			String v;
			try {
				v = new String(stream.readAllBytes());
			} catch (IOException e) {
				logger.error("IOException converting stream of wms object from {}", k, e);
				throw new BPException(
						"IOException reading WMS object for run " + runid + " and output " + output.getName() + " (" + output.getId() + ")",
						BPException.ERROR_CODES.BAD_WMS_OBJECT);
			}

			WMSValue wms = new JSONDeserializer().deserialize(v, WMSValue.class);

			logger.trace(VALUE_INFO, v.replace(System.lineSeparator(), ""), output.getName(), output.getId(), runid);

			output.setValue(wms);
			output.setValueType("value");

		}
	}

	private String getS3PublicBucketPrefix(String webBaseFinal, String runid) {
		return webBaseFinal + bucket + "/";
	}

	private String getS3PublicUrl(String webBaseFinal, BPOutput output, String runid) {
		return getS3PublicBucketPrefix(webBaseFinal, runid) + getKey(output, runid);
	}

	private String getWebBase() {
		String webBase = "https://s3.amazonaws.com/";

		if (null != serviceUrl && !"".equalsIgnoreCase(serviceUrl)) {
			webBase = serviceUrl;

			if (!webBase.endsWith("/"))
				webBase += "/";
		}

		return webBase;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRegion() {
		return region;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

}
