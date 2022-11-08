package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import eu.essi_lab.vlab.core.engine.services.IESRequestSubmitter;
import java.io.IOException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @author Mattia Santoro
 */
public class ESRequestSubmitter implements IESRequestSubmitter {

	private String user;

	private String pwd;

	protected HttpClient authenticatedClient() {
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		Credentials credentials = new UsernamePasswordCredentials(getUser(), getPwd());
		credentialsProvider.setCredentials(AuthScope.ANY, credentials);

		return HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

	}

	@Override
	public IESHttpResponseReader submit(HttpRequestBase request) throws IOException {

		return new HttpResponseReader(authenticatedClient().execute(request));

	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String getPwd() {
		return pwd;
	}

	@Override
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
