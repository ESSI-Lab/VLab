package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.ESQueryObject;
import java.io.InputStream;

/**
 * @author Mattia Santoro
 */
public interface IESClient<T> {

	Boolean store(String key, InputStream stream) throws BPException;

	IESRequestSubmitter getSubmitter();

	void setSubmitter(IESRequestSubmitter submitter);

	Boolean remove(String key) throws BPException;

	Boolean exists(String key) throws BPException;

	InputStream read(String key) throws BPException;

	String getBaseUrl();

	T search(BPUser user, ESQueryObject query) throws BPException;

}
