package eu.essi_lab.vlab.core.engine.services;

import java.io.IOException;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * @author Mattia Santoro
 */
public interface IESRequestSubmitter {

	IESHttpResponseReader submit(HttpRequestBase request) throws IOException;

	String getUser();

	void setUser(String user);

	String getPwd();

	void setPwd(String pwd);
}
