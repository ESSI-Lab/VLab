package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import java.io.File;
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
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * A simplified wrapper for {@link S3Client}, which executed writes, reads and remove operations from one single bucket.
 *
 * @author Mattia Santoro
 */
public class S3BucketClient implements IWebStorage {

	private String bucket;
	private Logger logger = LogManager.getLogger(S3BucketClient.class);
	private S3Client s3cli;
	private static final String TO_S3 = " to s3 ";
	private static final String PAR_AWS_CODE = " (aws code ";
	private static final String EXCEPTION_LISTING_OBJECTS = "Exception listing objects from s3 ";
	private int maxCreateBucketTries = 3;

	public S3BucketClient() {
		//public constructor for java service provisioning
	}

	@Override
	public List<ConfigurationParameter> configurationParameters() {

		return Arrays.asList(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter(),
				BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter());

	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) {
		String accessKey = parameters.get(BPStaticConfigurationParameters.AWS_S3_ACCESS_KEY.getParameter());
		String secretKey = parameters.get(BPStaticConfigurationParameters.AWS_S3_SECRET_KEY.getParameter());

		String region = parameters.get(BPStaticConfigurationParameters.AWS_BUCKET_REGION.getParameter());
		Optional<String> s3_url = Optional.ofNullable(parameters.get(BPStaticConfigurationParameters.AWS_S3_SERVICEURL.getParameter()));

		var staticCredentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

		if (s3_url.isPresent()) {

			s3cli = S3Client.builder().region(Region.of(region)).credentialsProvider(staticCredentials).endpointOverride(
					URI.create(s3_url.get())).build();

		} else

			s3cli = S3Client.builder().region(Region.of(region)).credentialsProvider(staticCredentials).build();

	}

	@Override
	public Boolean supports(String type) {
		return Boolean.TRUE;
	}

	public S3BucketClient(String accessKey, String secretKey, String region, Optional<String> serviceUrl) {

		var staticCredentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

		if (serviceUrl.isPresent()) {

			s3cli = S3Client.builder().region(Region.of(region)).credentialsProvider(staticCredentials).endpointOverride(
					URI.create(serviceUrl.get())).build();

		} else

			s3cli = S3Client.builder().region(Region.of(region)).credentialsProvider(staticCredentials).build();

	}

	@Override
	public void setBucket(String bucketName) {
		this.bucket = bucketName;
	}

	public boolean bucketExists(Boolean createIfNeeded) throws BPException {
		return checkBucket(createIfNeeded, 1);
	}

	S3Client getClient() {
		return s3cli;
	}

	@Override
	public WebStorageObject upload(File file, String key) throws BPException {

		PutObjectRequest request = PutObjectRequest.builder().bucket(this.bucket).key(key).build();

		S3Object o = execUpload(request, createRequestBody(file), key);

		WebStorageObject wo = new WebStorageObject();
		wo.setKey(o.key());
		return wo;

	}

	S3Object execUpload(PutObjectRequest request, RequestBody rb, String key) throws BPException {
		String err;
		S3Object s3o = S3Object.builder().key(key).build();
		try {

			getClient().putObject(request, rb);

			return s3o;

		} catch (AwsServiceException e) {

			err = "Exception uploading file with key " + key + TO_S3 + this.bucket + PAR_AWS_CODE + e.awsErrorDetails().errorCode() + "): "
					+ e.getMessage();

			logger.warn(err);

		} catch (SdkClientException e) {

			err = "Client Exception uploading file with key " + key + TO_S3 + this.bucket + ": " + e.getMessage();

			logger.warn(err);

		}

		throw new BPException(err, BPException.ERROR_CODES.AWS_S3_PUT_ERROR);

	}

	@Override
	public WebStorageObject upload(InputStream stream, String key) throws BPException {

		S3Object o = upload(stream, key, false);

		WebStorageObject wo = new WebStorageObject();
		wo.setKey(o.key());
		return wo;

	}

	/**
	 * Uploads the stream to AWS S3 bucket with the provided key. Returns an {@link S3Object} with bucket name and key which were used to
	 * upoload this file.
	 *
	 * @param stream
	 * @return the uploaded {@link S3Object}
	 */
	public S3Object upload(InputStream stream, String key, Boolean publicObject) throws BPException {
		return upload(stream, key, publicObject, null);
	}

	RequestBody createRequestBody(File file) {
		return RequestBody.fromFile(file);
	}

	RequestBody createRequestBody(InputStream stream) throws IOException {
		return RequestBody.fromBytes(stream.readAllBytes());
	}

	RequestBody createRequestBody(InputStream stream, Long length) {
		return RequestBody.fromInputStream(stream, length);
	}

	/**
	 * Uploads the stream to AWS S3 bucket with the provided key. Returns an {@link S3Object} with bucket name and key which were used to
	 * upoload this file.
	 *
	 * @param stream
	 * @return the uploaded {@link S3Object}
	 */
	public S3Object upload(InputStream stream, String key, Boolean publicObject, Long length) throws BPException {

		String err;
		S3Object s3o = S3Object.builder().key(key).build();

		var builder = PutObjectRequest.builder().bucket(this.bucket).key(key);

		if (Boolean.TRUE.equals(publicObject)) {

			builder.acl(ObjectCannedACL.PUBLIC_READ);

		}
		RequestBody body = null;

		if (length != null) {

			body = createRequestBody(stream, length);

		} else {
			try {
				body = createRequestBody(stream);
			} catch (IOException e) {

				err = "Error creating S3 Object, unable to upload file with key " + key + TO_S3 + this.bucket + ": " + e.getMessage();

				logger.error(err, e);

				throw new BPException(err);
			}

		}

		execUpload(builder.build(), body, key);

		return s3o;

	}

	public GetObjectRequest createGetObjectRequest(String key) {

		return GetObjectRequest.builder().bucket(this.bucket).key(key).build();

	}

	public boolean exists(String key) {

		try {
			getClient().headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());

			return true;
		} catch (NoSuchKeyException nse) {
			logger.warn("Cant't find {} in {}", key, bucket, nse);

			return false;
		}

	}

	@Override
	public InputStream read(String key) throws BPException {

		var getObjectRequest = createGetObjectRequest(key);

		String err = "Object with key " + key + " does not exist";

		if (exists(key)) {
			try {

				return getClient().getObject(getObjectRequest);

			} catch (AwsServiceException e) {

				err = "Exception reading key " + key + " from s3 " + this.bucket + PAR_AWS_CODE + e.awsErrorDetails().errorCode() + "): "
						+ e.getMessage();

				logger.warn(err, e);

			} catch (SdkClientException e) {

				err = "Client Exception reading key " + key + " from s3 " + this.bucket + ": " + e.getMessage();

				logger.warn(err, e);

			}
		}

		throw new BPException(err, BPException.ERROR_CODES.RESOURCE_NOT_FOUND.getCode());

	}

	private boolean checkBucket(boolean create, int tries) throws BPException {

		String err;
		try {

			logger.trace("Checking bucket {} with try {}", bucket, tries);

			List<Bucket> buckets = getClient().listBuckets().buckets();

			for (Bucket b : buckets)
				if (b.name().equalsIgnoreCase(this.bucket)) {

					logger.trace("Bucket {} exists", bucket);
					return true;

				}

			logger.trace("Bucket {} does not exist", bucket);

			if (create && tries < maxCreateBucketTries) {

				logger.trace("Creating bucket {}", bucket);

				getClient().createBucket(CreateBucketRequest.builder().bucket(this.bucket).build());

				return checkBucket(true, tries + 1);

			} else {

				logger.trace("Skipping bucket creation as by paramter {}", create);

				return false;
			}
		} catch (AwsServiceException e) {

			err = "Exception creating bucket " + this.bucket + PAR_AWS_CODE + e.awsErrorDetails().errorCode() + "): " + e.getMessage();

			logger.warn(err, e);

		} catch (SdkClientException e) {

			err = "Client Exception creating bucket " + this.bucket + ": " + e.getMessage();

			logger.warn(err, e);

		}

		throw new BPException(err, BPException.ERROR_CODES.AWS_S3_BUCKET_INIT_ERROR.getCode());
	}

	/**
	 * Deletes the object identified by key from the bucket.
	 *
	 * @param key
	 * @return true if operation succeeds, false otherwise
	 */
	@Override
	public Boolean remove(String key) {

		try {

			getClient().deleteObject(DeleteObjectRequest.builder().bucket(this.bucket).key(key).build());

			return true;

		} catch (AwsServiceException e) {
			logger.warn("Client Exception deleting key {} from s3 {} (aws code {}): " + e.awsErrorDetails().errorCode(), key, this.bucket,
					e.getMessage(), e);
		} catch (SdkClientException e) {
			logger.warn("Client Exception deleting key {} from s3 {}: {}", key, this.bucket, e.getMessage(), e);
		}

		return false;
	}

	@Override
	public List<String> listSubOjects(int start, int count, String directory) throws BPException {
		String err;

		logger.trace("List object from directory {} from {} with count {}", directory, start, count);
		try {

			var listObjectsRequest = ListObjectsRequest.builder().bucket(this.bucket).prefix(directory).maxKeys(count).build();

			List<S3Object> list = getClient().listObjects(listObjectsRequest).contents();

			logger.trace("Objects list size {}", list.size());

			List<String> ret = new ArrayList<>();

			for (S3Object obj : list) {

				//TODO investigate here, maybe I should first check if it is dir, and if yes then do not add the object but only its sub
				// objects, like in listSubOjects(String directory). In case I'll have to change tests as in S3BucketClientTest line 974
				// for the listSubOjects(String directory) method
				ret.add(obj.key());

				if (obj.key().endsWith("/"))
					ret.addAll(listSubOjects(obj.key()));

			}

			return ret;

		} catch (AwsServiceException e) {

			err = EXCEPTION_LISTING_OBJECTS + this.bucket + "/" + directory + PAR_AWS_CODE + e.awsErrorDetails().errorCode() + "): "
					+ e.getMessage();

			logger.warn(err);

		} catch (SdkClientException e) {

			err = EXCEPTION_LISTING_OBJECTS + this.bucket + "/" + directory + ": " + e.getMessage();

			logger.warn(err);

		}

		throw new BPException(err, BPException.ERROR_CODES.AWS_S3_READ_ERROR);
	}

	public ListObjectsRequest createListObjectRequest(String key) {

		return ListObjectsRequest.builder().bucket(this.bucket).prefix(key).maxKeys(1000).build();

	}

	@Override
	public List<String> listSubOjects(String directory) throws BPException {
		String err;

		logger.trace("Listing from bucket {} directory {}", bucket, directory);

		try {

			ListObjectsRequest listRequest = createListObjectRequest(directory);
			List<S3Object> list = getClient().listObjects(listRequest).contents();

			List<String> ret = new ArrayList<>();

			for (S3Object obj : list) {

				String k = obj.key();

				if (!obj.key().endsWith("/"))
					ret.add(k);
				else
					logger.trace("Discarding directory {}", k);

				logger.trace("Object key {}", k);

				if (obj.key().endsWith("/") && !directory.equalsIgnoreCase(obj.key()))
					ret.addAll(listSubOjects(obj.key()));

			}

			return ret;

		} catch (AwsServiceException e) {

			err = EXCEPTION_LISTING_OBJECTS + this.bucket + "/" + directory + PAR_AWS_CODE + e.awsErrorDetails().errorCode() + "): "
					+ e.getMessage();

			logger.warn(err);

		} catch (SdkClientException e) {

			err = EXCEPTION_LISTING_OBJECTS + this.bucket + "/" + directory + ": " + e.getMessage();

			logger.warn(err);

		}

		throw new BPException(err);
	}

	public String getBucket() {
		return this.bucket;
	}

}
