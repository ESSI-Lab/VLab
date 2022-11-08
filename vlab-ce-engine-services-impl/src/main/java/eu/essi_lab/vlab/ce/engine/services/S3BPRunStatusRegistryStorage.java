package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusStorage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class S3BPRunStatusRegistryStorage implements IBPRunStatusStorage {

	private S3BucketClient client;

	private static final String STATUS_FILE_NAMEPREFIX = "BPStatus_";

	private static final String STATUS_DIRECTORY_NAME = "BPStatusDir";
	private Logger logger = LogManager.getLogger(S3BPRunStatusRegistryStorage.class);

	@Override
	public List<ConfigurationParameter> configurationParameters() {

		return Arrays.asList(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter(),
				BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter());
	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) throws BPException {

		String ak = parameters.get(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter());
		String sk = parameters.get(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter());

		String region = parameters.get(BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter());
		Optional<String> s3_url = Optional.ofNullable(parameters.get(BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter()));

		S3BucketClient cli = new S3BucketClient(ak, sk, region, s3_url);
		cli.setBucket(
				ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.AWS_S3_BPRUN_STATUS_BUCKET.getParameter()));

		this.setS3BucketClient(cli);
		client.bucketExists(false);
	}

	@Override
	public Boolean supports(String type) {
		return Boolean.TRUE;
	}

	public void setS3BucketClient(S3BucketClient s3BucketClient) {
		this.client = s3BucketClient;

	}

	@Override
	public boolean store(BPRunStatus status) {

		String s3Key = getS3Key(status.getRunid());

		WebStorageObject object = null;

		try {

			object = client.upload(new ByteArrayInputStream(new JSONSerializer().serialize(status).getBytes(StandardCharsets.UTF_8)),
					s3Key);

		} catch (BPException e) {

			logger.error("Exception storing status of run {}: ", status.getRunid(), e);

			return false;

		}

		return s3Key.equalsIgnoreCase(object.getKey());

	}

	@Override
	public BPRunStatus get(String runid) throws BPException {

		InputStream stream = client.read(getS3Key(runid));

		return new JSONDeserializer().deserialize(stream, BPRunStatus.class);

	}

	@Override
	public boolean remove(String runid) throws BPException {

		return client.remove(getS3Key(runid));

	}

	private String getS3Key(String runid) {
		return STATUS_DIRECTORY_NAME + "/" + STATUS_FILE_NAMEPREFIX + runid;
	}

}
