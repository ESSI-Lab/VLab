package eu.essi_lab.vlab.core.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class TrustedHttpClientBuilder {

	private static Logger logger = LogManager.getLogger(TrustedHttpClientBuilder.class);

	public HttpClient build() throws KeyManagementException, NoSuchAlgorithmException {

		TrustManager[] trustAllCerts = createTrustManagerCerts();

		SSLContext sc = installTrustingManager(trustAllCerts);

		return HttpClients.custom().setSSLContext(sc).build();

	}

	public static TrustManager[] createTrustManagerCerts() throws NoSuchAlgorithmException {

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		// Using null here initialises the TMF with the default trust store.
		try {
			tmf.init((KeyStore) null);
		} catch (KeyStoreException e) {
			logger.warn("KeyStoreException", e);
		}

		// Get hold of the default trust manager
		X509TrustManager x509Tm = null;
		for (TrustManager tm : tmf.getTrustManagers()) {
			if (tm instanceof X509TrustManager) {
				x509Tm = (X509TrustManager) tm;
				break;
			}
		}

		// Wrap it in your own class.
		final X509TrustManager finalTm = x509Tm;

		// Create a trust manager that does not validate certificate chains
		return new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return finalTm.getAcceptedIssuers();
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {

				try {
					finalTm.checkClientTrusted(certs, authType);
				} catch (CertificateException e) {

					logException("Client certificate exception", e, certs, authType);
				}
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {

				try {
					finalTm.checkClientTrusted(certs, authType);
				} catch (CertificateException e) {

					logException("Server certificate exception", e, certs, authType);
				}
			}
		} };
	}

	private static void logException(String msg, CertificateException e, java.security.cert.X509Certificate[] certs, String authType) {

		String domain = "No Domain found";

		if (certs != null && certs.length > 0 && certs[0].getSubjectX500Principal() != null)
			domain = certs[0].getSubjectX500Principal().getName();

		logger.warn("{} - with domain {} and authType {}", msg, domain, authType, e);

	}

	public static SSLContext installTrustingManager(TrustManager[] trustAllCerts) throws KeyManagementException, NoSuchAlgorithmException {
		// Install the all-trusting trust manager

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		return sc;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, IOException {

		String urlok = "https://essi-lab.eu/git/";
		String urlnotvalid = "https://ulisse.essi-lab.eu/teamcity/login.html";

		HttpResponse response = new TrustedHttpClientBuilder().build().execute(new HttpGet(urlnotvalid));

		logger.info("URL {} Code {}", urlnotvalid, response.getStatusLine().getStatusCode());

		response = new TrustedHttpClientBuilder().build().execute(new HttpGet(urlok));

		logger.info("URL {} Code {}", urlok, response.getStatusLine().getStatusCode());

	}
}
