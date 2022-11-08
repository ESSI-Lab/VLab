package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.ce.engine.services.ESRequestSubmitter;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;

/**
 * @author Mattia Santoro
 */
public class AWSESRequestSubmitter extends ESRequestSubmitter {

	private static final String ES_SERVICE_NAME = "es";

	private String region;

	@Override
	protected HttpClient authenticatedClient() {

		Aws4Signer signer = Aws4Signer.create();

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(getUser(), getPwd());

		StaticCredentialsProvider staticCredentials = StaticCredentialsProvider.create(awsCreds);

		HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(ES_SERVICE_NAME, region, signer, staticCredentials);

		return HttpClientBuilder.create().addInterceptorLast(interceptor).build();

	}

	public void setRegion(String region) {
		this.region = region;
	}
}
