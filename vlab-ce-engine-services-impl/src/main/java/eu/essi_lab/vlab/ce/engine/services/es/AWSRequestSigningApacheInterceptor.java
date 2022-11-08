package eu.essi_lab.vlab.ce.engine.services.es;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import static org.apache.http.protocol.HttpCoreContext.HTTP_TARGET_HOST;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

/**
 * @author Mattia Santoro
 */

/**
 * An {@link HttpRequestInterceptor} that signs requests using any AWS {@link Aws4Signer} and {@link AwsCredentialsProvider}.
 */
public class AWSRequestSigningApacheInterceptor implements HttpRequestInterceptor {

	private Logger logger = LogManager.getLogger(AWSRequestSigningApacheInterceptor.class);

	/**
	 * The service that we're connecting to. Technically not necessary. Could be used by a future Signer, though.
	 */
	private final String service;

	/**
	 * The particular signer implementation.
	 */
	private final Aws4Signer signer;

	/**
	 * The source of AWS credentials for signing.
	 */
	private final AwsCredentialsProvider awsCredentialsProvider;
	private final String region;

	/**
	 * @param region                 region that we're connecting to
	 * @param signer                 particular signer implementation
	 * @param awsCredentialsProvider source of AWS credentials for signing
	 */
	public AWSRequestSigningApacheInterceptor(final String service, final String region, final Aws4Signer signer,
			final AwsCredentialsProvider awsCredentialsProvider) {
		this.service = service;
		this.region = region;
		this.signer = signer;
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {

		URIBuilder uriBuilder;
		try {
			uriBuilder = new URIBuilder(request.getRequestLine().getUri());
		} catch (URISyntaxException e) {
			throw new IOException("Invalid URI", e);
		}

		// Copy Apache HttpRequest to AWS DefaultRequest
		SdkHttpFullRequest.Builder signableRequestBuilder = SdkHttpFullRequest.builder();

		HttpHost host = (HttpHost) context.getAttribute(HTTP_TARGET_HOST);
		if (host != null) {
			signableRequestBuilder.uri(URI.create(host.toURI()));
		}

		signableRequestBuilder.method(SdkHttpMethod.valueOf(request.getRequestLine().getMethod()));

		try {
			signableRequestBuilder.encodedPath(uriBuilder.build().getRawPath());
		} catch (URISyntaxException e) {
			throw new IOException("Invalid URI", e);
		}

		if (request instanceof HttpEntityEnclosingRequest httpEntityEnclosingRequest && httpEntityEnclosingRequest.getEntity() != null) {
			signableRequestBuilder.contentStreamProvider(() -> {
				try {
					return httpEntityEnclosingRequest.getEntity().getContent();
				} catch (IOException e) {

					logger.error("Error setting content stream provider in signer request builder", e);

					return null;
				}
			});

		}

		signableRequestBuilder.rawQueryParameters(nvpToMapParams(uriBuilder.getQueryParams()));
		signableRequestBuilder.headers(headerArrayToMap(request.getAllHeaders()));

		Aws4SignerParams params = Aws4SignerParams.builder().awsCredentials(awsCredentialsProvider.resolveCredentials()).signingRegion(
				Region.of(region)).signingName(this.service).build();

		// Sign it
		SdkHttpFullRequest signed = signer.sign(signableRequestBuilder.build(), params);

		// Now copy everything back
		request.setHeaders(mapToHeaderArray2(signed.headers()));
		if (request instanceof HttpEntityEnclosingRequest httpEntityEnclosingRequest) {

			Optional<ContentStreamProvider> csp = signed.contentStreamProvider();
			if (httpEntityEnclosingRequest.getEntity() != null && csp.isPresent()) {
				BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
				basicHttpEntity.setContent(csp.get().newStream());
				httpEntityEnclosingRequest.setEntity(basicHttpEntity);
			}
		}
	}

	/**
	 * @param params list of HTTP query params as NameValuePairs
	 * @return a multimap of HTTP query params
	 */
	private static Map<String, List<String>> nvpToMapParams(final List<NameValuePair> params) {
		Map<String, List<String>> parameterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (NameValuePair nvp : params) {
			List<String> argsList = parameterMap.computeIfAbsent(nvp.getName(), k -> new ArrayList<>());
			argsList.add(nvp.getValue());
		}
		return parameterMap;
	}

	/**
	 * @param headers modeled Header objects
	 * @return a Map of header entries
	 */
	private static Map<String, List<String>> headerArrayToMap(final Header[] headers) {
		Map<String, List<String>> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (Header header : headers) {
			if (!skipHeader(header)) {
				headersMap.put(header.getName(), Arrays.asList(header.getValue()));
			}
		}
		return headersMap;
	}

	/**
	 * @param header header line to check
	 * @return true if the given header should be excluded when signing
	 */
	private static boolean skipHeader(final Header header) {
		return ("content-length".equalsIgnoreCase(header.getName()) && "0".equals(header.getValue())) // Strip Content-Length: 0
				|| "host".equalsIgnoreCase(header.getName()); // Host comes from endpoint
	}

	private static Header[] mapToHeaderArray2(final Map<String, List<String>> mapHeaders) {
		Header[] headers = new Header[mapHeaders.size()];
		int i = 0;
		for (Map.Entry<String, List<String>> headerEntry : mapHeaders.entrySet()) {
			headers[i++] = new BasicHeader(headerEntry.getKey(), headerEntry.getValue().get(0));
		}
		return headers;
	}
}