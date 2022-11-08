package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * @author Mattia Santoro
 */
public class S3BucketClientTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private String region = "us-east-1";
	private String bucket = "name";
	private String ak = "ak";
	private String sk = "sk";
	private Logger logger = LoggerFactory.getLogger(S3BucketClientTest.class);

	@Test
	public void test() throws BPException {

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);
		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<Bucket> buckets = new ArrayList<>();

		ListBucketsResponse response = Mockito.mock(ListBucketsResponse.class);
		Mockito.doReturn(buckets).when(response).buckets();

		Mockito.doReturn(response).when(s3cli).listBuckets();

		CreateBucketResponse createBucketResponse = Mockito.mock(CreateBucketResponse.class);

		Bucket createdBucket = Mockito.mock(Bucket.class);
		Mockito.doReturn(bucket).when(createdBucket).name();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String reqbucket = ((CreateBucketRequest) invocation.getArguments()[0]).bucket();

				if (!bucket.equalsIgnoreCase(reqbucket))
					throw new Exception("Bad bucket create operation " + reqbucket);

				buckets.add(createdBucket);

				return createBucketResponse;

			}
		}).when(s3cli).createBucket((CreateBucketRequest) Mockito.any());

		assertTrue(client.bucketExists(true));

	}

	@Test
	public void test2() throws BPException {

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<Bucket> buckets = new ArrayList<>();

		Bucket anotherb = Mockito.mock(Bucket.class);
		Mockito.doReturn("ff").when(anotherb).name();
		buckets.add(anotherb);

		ListBucketsResponse response = Mockito.mock(ListBucketsResponse.class);
		Mockito.doReturn(buckets).when(response).buckets();

		Mockito.doReturn(response).when(s3cli).listBuckets();

		CreateBucketResponse createBucketResponse = Mockito.mock(CreateBucketResponse.class);
		Bucket createdBucket = Mockito.mock(Bucket.class);
		Mockito.doReturn(bucket).when(createdBucket).name();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String reqbucket = ((CreateBucketRequest) invocation.getArguments()[0]).bucket();

				if (!bucket.equalsIgnoreCase(reqbucket))
					throw new Exception("Bad bucket create operation " + reqbucket);

				buckets.add(createdBucket);

				return createBucketResponse;

			}
		}).when(s3cli).createBucket((CreateBucketRequest) Mockito.any());

		assertTrue(client.bucketExists(true));

	}

	@Test
	public void test3() throws BPException {

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<Bucket> buckets = new ArrayList<>();

		ListBucketsResponse response = Mockito.mock(ListBucketsResponse.class);
		Mockito.doReturn(buckets).when(response).buckets();

		Mockito.doReturn(response).when(s3cli).listBuckets();

		assertFalse(client.bucketExists(false));

		Mockito.verify(s3cli, Mockito.never()).createBucket((CreateBucketRequest) Mockito.any());

	}

	@Test
	public void test4() throws BPException {

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<Bucket> buckets = new ArrayList<>();
		Bucket anotherb = Mockito.mock(Bucket.class);
		Mockito.doReturn("ff").when(anotherb).name();
		buckets.add(anotherb);

		ListBucketsResponse response = Mockito.mock(ListBucketsResponse.class);
		Mockito.doReturn(buckets).when(response).buckets();
		Mockito.doReturn(response).when(s3cli).listBuckets();

		assertFalse(client.bucketExists(false));

		Mockito.verify(s3cli, Mockito.never()).createBucket((CreateBucketRequest) Mockito.any());

	}

	@Test
	public void test5() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Exception creating bucket", BPException.ERROR_CODES.AWS_S3_BUCKET_INIT_ERROR));

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<Bucket> buckets = new ArrayList<>();
		Bucket anotherb = Mockito.mock(Bucket.class);
		Mockito.doReturn("ff").when(anotherb).name();
		buckets.add(anotherb);

		ListBucketsResponse response = Mockito.mock(ListBucketsResponse.class);
		Mockito.doReturn(buckets).when(response).buckets();
		Mockito.doReturn(response).when(s3cli).listBuckets();

		AwsServiceException ex = Mockito.mock(AwsServiceException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(s3cli).createBucket((CreateBucketRequest) Mockito.any());

		client.bucketExists(true);

	}

	@Test
	public void test6() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Client Exception creating bucket", BPException.ERROR_CODES.AWS_S3_BUCKET_INIT_ERROR));

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<Bucket> buckets = new ArrayList<>();
		Bucket anotherb = Mockito.mock(Bucket.class);
		Mockito.doReturn("ff").when(anotherb).name();
		buckets.add(anotherb);

		ListBucketsResponse response = Mockito.mock(ListBucketsResponse.class);
		Mockito.doReturn(buckets).when(response).buckets();
		Mockito.doReturn(response).when(s3cli).listBuckets();

		Mockito.doThrow(SdkClientException.class).when(s3cli).createBucket((CreateBucketRequest) Mockito.any());

		client.bucketExists(true);

	}

	@Test
	public void test7() throws BPException {

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		String key = "key";

		AwsServiceException ex = Mockito.mock(AwsServiceException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doReturn(Mockito.mock(DeleteObjectResponse.class)).doThrow(ex).doThrow(SdkClientException.class).when(s3cli).deleteObject(
				(DeleteObjectRequest) Mockito.any());

		assertTrue(client.remove(key));
		assertFalse(client.remove(key));
		assertFalse(client.remove(key));

	}

	@Test
	public void test8() throws BPException {

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		String key = "key";

		HeadObjectResponse response = Mockito.mock(HeadObjectResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String reqbucket = ((HeadObjectRequest) invocation.getArguments()[0]).bucket();
				String reqkey = ((HeadObjectRequest) invocation.getArguments()[0]).key();

				if (!bucket.equalsIgnoreCase(reqbucket))
					throw new Exception("Bad bucket requested " + reqbucket);

				if (!key.equalsIgnoreCase(reqkey))
					throw new Exception("Bad object requested " + reqkey);

				return response;

			}
		}).when(s3cli).headObject((HeadObjectRequest) Mockito.any());

		ResponseInputStream stream = Mockito.mock(ResponseInputStream.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				GetObjectRequest getObjectRequest = (GetObjectRequest) invocation.getArguments()[0];

				if (!bucket.equalsIgnoreCase(getObjectRequest.bucket()))
					throw new Exception("Bad bucket requested " + getObjectRequest.bucket());

				if (!key.equalsIgnoreCase(getObjectRequest.key()))
					throw new Exception("Bad object requested " + getObjectRequest.key());

				return stream;

			}
		}).when(s3cli).getObject((GetObjectRequest) Mockito.any());
		InputStream r = client.read(key);
		assertNotNull(r);

	}

	@Test
	public void test9() throws BPException {
		String key = "key";

		expectedException.expect(
				new BPExceptionMatcher("Object with key " + key + " does not exist", BPException.ERROR_CODES.RESOURCE_NOT_FOUND));

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		Mockito.doThrow(NoSuchKeyException.class).when(s3cli).headObject((HeadObjectRequest) Mockito.any());

		client.read(key);

	}

	@Test
	public void test10() throws BPException {
		String key = "key";

		expectedException.expect(new BPExceptionMatcher("Exception reading key ", BPException.ERROR_CODES.RESOURCE_NOT_FOUND));

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		HeadObjectResponse response = Mockito.mock(HeadObjectResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String reqbucket = ((HeadObjectRequest) invocation.getArguments()[0]).bucket();
				String reqkey = ((HeadObjectRequest) invocation.getArguments()[0]).key();

				if (!bucket.equalsIgnoreCase(reqbucket))
					throw new Exception("Bad bucket requested " + reqbucket);

				if (!key.equalsIgnoreCase(reqkey))
					throw new Exception("Bad object requested " + reqkey);

				return response;

			}
		}).when(s3cli).headObject((HeadObjectRequest) Mockito.any());

		AwsServiceException ex = Mockito.mock(AwsServiceException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(s3cli).getObject((GetObjectRequest) Mockito.any());
		client.read(key);

	}

	@Test
	public void test11() throws BPException {
		String key = "key";

		expectedException.expect(new BPExceptionMatcher("Client Exception reading key ", BPException.ERROR_CODES.RESOURCE_NOT_FOUND));

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();
		HeadObjectResponse response = Mockito.mock(HeadObjectResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String reqbucket = ((HeadObjectRequest) invocation.getArguments()[0]).bucket();
				String reqkey = ((HeadObjectRequest) invocation.getArguments()[0]).key();

				if (!bucket.equalsIgnoreCase(reqbucket))
					throw new Exception("Bad bucket requested " + reqbucket);

				if (!key.equalsIgnoreCase(reqkey))
					throw new Exception("Bad object requested " + reqkey);
				return response;

			}
		}).when(s3cli).headObject((HeadObjectRequest) Mockito.any());

		Mockito.doThrow(SdkClientException.class).when(s3cli).getObject((GetObjectRequest) Mockito.any());
		client.read(key);

	}

	@Test
	public void test12() throws BPException {
		String key = "key";
		File file = Mockito.mock(File.class);

		String fname = "fname";
		Mockito.doReturn(fname).when(file).getName();

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		PutObjectResponse putObjectResult = Mockito.mock(PutObjectResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				File rf = (File) invocationOnMock.getArguments()[0];

				if (!fname.equalsIgnoreCase(rf.getName()))
					throw new Exception("Bad file name requested " + rf.getName());

				return Mockito.mock(RequestBody.class);
			}
		}).when(client).createRequestBody((File) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				PutObjectRequest request = (PutObjectRequest) invocation.getArguments()[0];

				String rb = request.bucket();

				String rk = request.key();

				if (!bucket.equalsIgnoreCase(rb))
					throw new Exception("Bad bucket requested " + rb);

				if (!key.equalsIgnoreCase(rk))
					throw new Exception("Bad object requested " + rk);

				return putObjectResult;

			}

		}).when(s3cli).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

		client.upload(file, key);

		Mockito.verify(client, Mockito.times(1)).createRequestBody((File) Mockito.any());
		Mockito.verify(s3cli, Mockito.times(1)).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

	}

	@Test
	public void test14() throws BPException {

		String key = "key";
		expectedException.expect(
				new BPExceptionMatcher("Exception uploading file with key " + key, BPException.ERROR_CODES.AWS_S3_PUT_ERROR));
		File file = Mockito.mock(File.class);

		String fname = "fname";
		Mockito.doReturn(fname).when(file).getName();

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				File rf = (File) invocationOnMock.getArguments()[0];

				if (!fname.equalsIgnoreCase(rf.getName()))
					throw new Exception("Bad file name requested " + rf.getName());

				return Mockito.mock(RequestBody.class);
			}
		}).when(client).createRequestBody((File) Mockito.any());

		AwsServiceException ex = Mockito.mock(AwsServiceException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(s3cli).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

		client.upload(file, key);

	}

	@Test
	public void test15() throws BPException {

		String key = "key";
		expectedException.expect(
				new BPExceptionMatcher("Client Exception uploading file with key " + key, BPException.ERROR_CODES.AWS_S3_PUT_ERROR));
		File file = Mockito.mock(File.class);

		String fname = "fname";
		Mockito.doReturn(fname).when(file).getName();

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				File rf = (File) invocationOnMock.getArguments()[0];

				if (!fname.equalsIgnoreCase(rf.getName()))
					throw new Exception("Bad file name requested " + rf.getName());

				return Mockito.mock(RequestBody.class);
			}
		}).when(client).createRequestBody((File) Mockito.any());

		Mockito.doThrow(SdkClientException.class).when(s3cli).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

		client.upload(file, key);

	}

	@Test
	public void test16() throws BPException, IOException {
		String key = "key";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		InputStream stream = Mockito.mock(InputStream.class);

		PutObjectResponse putObjectResult = Mockito.mock(PutObjectResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				InputStream rf = (InputStream) invocationOnMock.getArguments()[0];

				if (null == rf)
					throw new Exception("Expected not null input stream");

				return Mockito.mock(RequestBody.class);
			}
		}).when(client).createRequestBody((InputStream) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				PutObjectRequest request = (PutObjectRequest) invocation.getArguments()[0];

				String rb = request.bucket();

				String rk = request.key();

				if (!bucket.equalsIgnoreCase(rb))
					throw new Exception("Bad bucket requested " + rb);

				if (!key.equalsIgnoreCase(rk))
					throw new Exception("Bad object requested " + rk);

				ObjectCannedACL acl = request.acl();

				if (acl == null)
					throw new Exception("Expected ACL in put object request");

				if (ObjectCannedACL.PUBLIC_READ.compareTo(acl) != 0)
					throw new Exception("Expected public read permission");

				return putObjectResult;

			}

		}).when(s3cli).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

		client.upload(stream, key, true);

		Mockito.verify(client, Mockito.times(1)).createRequestBody((InputStream) Mockito.any());
		Mockito.verify(s3cli, Mockito.times(1)).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

	}

	@Test
	public void test18() throws BPException, IOException {
		String key = "key";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		InputStream stream = Mockito.mock(InputStream.class);

		PutObjectResponse putObjectResult = Mockito.mock(PutObjectResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				InputStream rf = (InputStream) invocationOnMock.getArguments()[0];

				if (null == rf)
					throw new Exception("Expected not null input stream");

				return Mockito.mock(RequestBody.class);
			}
		}).when(client).createRequestBody((InputStream) Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				PutObjectRequest request = (PutObjectRequest) invocation.getArguments()[0];

				String rb = request.bucket();

				String rk = request.key();

				if (!bucket.equalsIgnoreCase(rb))
					throw new Exception("Bad bucket requested " + rb);

				if (!key.equalsIgnoreCase(rk))
					throw new Exception("Bad object requested " + rk);

				ObjectCannedACL acl = request.acl();

				if (acl != null)
					throw new Exception("Expected no ACL in put object request");

				return putObjectResult;

			}

		}).when(s3cli).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

		client.upload(stream, key, false);
		Mockito.verify(client, Mockito.times(1)).createRequestBody((InputStream) Mockito.any());
		Mockito.verify(s3cli, Mockito.times(1)).putObject((PutObjectRequest) Mockito.any(), (RequestBody) Mockito.any());

	}

	@Test
	public void test19() throws BPException {
		String key = "key";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		InputStream stream = Mockito.mock(InputStream.class);

		S3Object putObjectResult = Mockito.mock(S3Object.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String k = (String) invocation.getArguments()[1];

				if (!key.equalsIgnoreCase(k))
					throw new Exception("Bad key " + k);

				Boolean request = (Boolean) invocation.getArguments()[2];

				if (request)
					throw new Exception("Expected default not public");

				Mockito.doReturn(k).when(putObjectResult).key();

				return putObjectResult;

			}

		}).when(client).upload(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

		WebStorageObject wso = client.upload(stream, key);

		assertEquals(key, wso.getKey());

	}

	private ListObjectsResponse createListing(List<String> keys) {

		List<S3Object> list = new ArrayList<>();

		keys.forEach(k -> {
			S3Object o1 = Mockito.mock(S3Object.class);

			Mockito.doReturn(k).when(o1).key();

			list.add(o1);

		});

		ListObjectsResponse response = Mockito.mock(ListObjectsResponse.class);
		Mockito.doReturn(list).when(response).contents();

		return response;
	}

	@Test
	public void test20() throws BPException {
		String dir = "dir";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		Integer max = 3;

		List<String> expectedkeys = Arrays.asList("k1", "k2");
		ListObjectsResponse listing = createListing(expectedkeys);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListObjectsRequest request = (ListObjectsRequest) invocation.getArguments()[0];

				String rb = request.bucket();

				String rd = request.prefix();

				if (!bucket.equalsIgnoreCase(rb))
					throw new Exception("Bad bucket requested " + rb);

				if (!dir.equalsIgnoreCase(rd))
					throw new Exception("Bad dir requested " + rd);

				if (request.maxKeys() != max)
					throw new Exception("Bad max found " + request.maxKeys());

				return listing;

			}

		}).when(s3cli).listObjects((ListObjectsRequest) Mockito.any());

		List<String> foundkeys = client.listSubOjects(0, max, dir);

		boolean match = foundkeys.stream().anyMatch(k -> {

			boolean found = expectedkeys.contains(k);

			if (!found)
				logger.error("Found unexpected key {} in result", k);

			return !found;

		});

		assertFalse(match);

		match = expectedkeys.stream().anyMatch(k -> {
			boolean found = foundkeys.contains(k);

			if (!found)
				logger.error("Expected key {} not found in result", k);

			return !found;
		});

		assertFalse(match);

		Mockito.verify(client, Mockito.never()).listSubOjects(Mockito.anyString());

	}

	@Test
	public void test21() throws BPException {
		String dir = "dir";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		Integer max = 3;

		List<String> expectedkeys = Arrays.asList("k1", "k2/");
		ListObjectsResponse listing = createListing(expectedkeys);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListObjectsRequest request = (ListObjectsRequest) invocation.getArguments()[0];

				String rb = request.bucket();

				String rd = request.prefix();

				if (!bucket.equalsIgnoreCase(rb))
					throw new Exception("Bad bucket requested " + rb);

				if (!dir.equalsIgnoreCase(rd))
					throw new Exception("Bad dir requested " + rd);

				if (request.maxKeys() != max)
					throw new Exception("Bad max found " + request.maxKeys());

				return listing;

			}

		}).when(s3cli).listObjects((ListObjectsRequest) Mockito.any());

		List<String> subdirlist = Arrays.asList("sub1", "sub2");

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String request = (String) invocation.getArguments()[0];

				if (!"k2/".equalsIgnoreCase(request))
					throw new Exception("Bad dir requested " + request);

				return subdirlist;

			}

		}).when(client).listSubOjects(Mockito.anyString());

		List<String> foundkeys = client.listSubOjects(0, max, dir);

		List<String> finalList = new ArrayList<>();

		finalList.addAll(expectedkeys);
		finalList.addAll(subdirlist);

		boolean match = foundkeys.stream().anyMatch(k -> {

			boolean found = finalList.contains(k);

			if (!found)
				logger.error("Found unexpected key {} in result", k);

			return !found;

		});

		assertFalse(match);

		match = finalList.stream().anyMatch(k -> {
			boolean found = foundkeys.contains(k);

			if (!found)
				logger.error("Expected key {} not found in result", k);

			return !found;
		});

		assertFalse(match);

		Mockito.verify(client, Mockito.times(1)).listSubOjects(Mockito.anyString());

	}

	@Test
	public void test22() throws BPException {

		String dir = "dir";

		expectedException.expect(new BPExceptionMatcher("(aws code ", BPException.ERROR_CODES.AWS_S3_READ_ERROR));

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		Integer max = 3;

		AwsServiceException ex = Mockito.mock(AwsServiceException.class);
		AwsErrorDetails details = Mockito.mock(AwsErrorDetails.class);
		Mockito.doReturn(details).when(ex).awsErrorDetails();

		Mockito.doThrow(ex).when(s3cli).listObjects((ListObjectsRequest) Mockito.any());

		client.listSubOjects(0, max, dir);

	}

	@Test
	public void test23() throws BPException {

		String dir = "dir";

		expectedException.expect(new BPExceptionMatcher("Exception listing objects from s3 ", BPException.ERROR_CODES.AWS_S3_READ_ERROR));

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		Integer max = 3;

		Mockito.doThrow(SdkClientException.class).when(s3cli).listObjects((ListObjectsRequest) Mockito.any());

		client.listSubOjects(0, max, dir);

	}

	@Test
	public void test24() throws BPException {
		String dir = "dir";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<String> expectedkeys = Arrays.asList("k1", "k2");
		ListObjectsResponse listing = createListing(expectedkeys);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListObjectsRequest lor = (ListObjectsRequest) invocation.getArguments()[0];

				String buc = lor.bucket();
				String directory = lor.prefix();

				if (!bucket.equalsIgnoreCase(buc))
					throw new Exception("Bad bucket requested " + buc);

				if (!dir.equalsIgnoreCase(directory))
					throw new Exception("Bad dir requested " + directory);

				return listing;

			}

		}).when(s3cli).listObjects((ListObjectsRequest) Mockito.any());

		List<String> foundkeys = client.listSubOjects(dir);

		boolean match = foundkeys.stream().anyMatch(k -> {

			boolean found = expectedkeys.contains(k);

			if (!found)
				logger.error("Found unexpected key {} in result", k);

			return !found;

		});

		assertFalse(match);

		match = expectedkeys.stream().anyMatch(k -> {
			boolean found = foundkeys.contains(k);

			if (!found)
				logger.error("Expected key {} not found in result", k);

			return !found;
		});

		assertFalse(match);

		Mockito.verify(client, Mockito.times(1)).listSubOjects(Mockito.anyString());

	}

	@Test
	public void test25() throws BPException {

		String dir = "dir";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<String> expectedkeys = Arrays.asList("k1", "k2/");
		ListObjectsResponse listing = createListing(expectedkeys);

		List<String> subkeys = Arrays.asList("sub1", "sub2");
		ListObjectsResponse listing2 = createListing(subkeys);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListObjectsRequest lor = (ListObjectsRequest) invocation.getArguments()[0];

				String buc = lor.bucket();
				String directory = lor.prefix();

				if (!bucket.equalsIgnoreCase(buc))
					throw new Exception("Bad bucket requested " + buc);

				if (!dir.equalsIgnoreCase(directory))
					throw new Exception("Bad dir requested " + directory);

				return listing;

			}

		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListObjectsRequest lor = (ListObjectsRequest) invocation.getArguments()[0];

				String buc = lor.bucket();
				String directory = lor.prefix();

				if (!bucket.equalsIgnoreCase(buc))
					throw new Exception("Bad bucket requested " + buc);

				if (!"k2/".equalsIgnoreCase(directory))
					throw new Exception("Bad dir requested " + directory);

				return listing2;

			}

		}).when(s3cli).listObjects((ListObjectsRequest) Mockito.any());

		List<String> foundkeys = client.listSubOjects(dir);
		List<String> finalList = new ArrayList<>();

		finalList.add(expectedkeys.get(0));// k2/ is directory and must not be added to result
		finalList.addAll(subkeys);

		boolean match = foundkeys.stream().anyMatch(k -> {

			boolean found = finalList.contains(k);

			if (!found)
				logger.error("Found unexpected key {} in result", k);

			return !found;

		});

		assertFalse(match);

		match = finalList.stream().anyMatch(k -> {
			boolean found = foundkeys.contains(k);

			if (!found)
				logger.error("Expected key {} not found in result", k);

			return !found;
		});

		assertFalse(match);

		Mockito.verify(client, Mockito.times(2)).listSubOjects(Mockito.anyString());

	}

	@Test
	public void test26() throws BPException {

		String dir = "dir/";

		S3BucketClient client = Mockito.spy(new S3BucketClient(ak, sk, region, Optional.empty()));
		client.setBucket(bucket);

		S3Client s3cli = Mockito.mock(S3Client.class);

		Mockito.doReturn(s3cli).when(client).getClient();

		List<String> expectedkeys = Arrays.asList("k1", dir);
		ListObjectsResponse listing = createListing(expectedkeys);

		List<String> subkeys = Arrays.asList("sub1", "sub2");
		ListObjectsResponse listing2 = createListing(subkeys);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListObjectsRequest lor = (ListObjectsRequest) invocation.getArguments()[0];

				String buc = lor.bucket();
				String directory = lor.prefix();

				if (!bucket.equalsIgnoreCase(buc))
					throw new Exception("Bad bucket requested " + buc);

				if (!dir.equalsIgnoreCase(directory))
					throw new Exception("Bad dir requested " + directory);

				return listing;

			}

		}).doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ListObjectsRequest lor = (ListObjectsRequest) invocation.getArguments()[0];

				String buc = lor.bucket();
				String directory = lor.prefix();
				if (!bucket.equalsIgnoreCase(buc))
					throw new Exception("Bad bucket requested " + buc);

				if (!"dir/".equalsIgnoreCase(directory))
					throw new Exception("Bad dir requested " + directory);

				return listing2;

			}

		}).when(s3cli).listObjects((ListObjectsRequest) Mockito.any());

		List<String> foundkeys = client.listSubOjects(dir);
		List<String> finalList = new ArrayList<>();

		finalList.add(expectedkeys.get(0));// dir/ is directory and must not be added to result

		boolean match = foundkeys.stream().anyMatch(k -> {

			boolean found = finalList.contains(k);

			if (!found)
				logger.error("Found unexpected key {} in result", k);

			return !found;

		});

		assertFalse(match);

		match = finalList.stream().anyMatch(k -> {
			boolean found = foundkeys.contains(k);

			if (!found)
				logger.error("Expected key {} not found in result", k);

			return !found;
		});

		assertFalse(match);

		Mockito.verify(client, Mockito.times(1)).listSubOjects(Mockito.anyString());

	}
}