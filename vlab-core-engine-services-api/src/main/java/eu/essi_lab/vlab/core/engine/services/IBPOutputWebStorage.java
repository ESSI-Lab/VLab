package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPOutput;

/**
 * @author Mattia Santoro
 */
public interface IBPOutputWebStorage extends IBPConfigurableService {

	String getKey(BPOutput output, String runid);

	String getBucket();

	void setBucket(String bucket);

	void addValue(BPOutput output, String runid, IWebStorage webStorage) throws BPException;

	void setRegion(String region);

	String getRegion();

	void setAccessKey(String accessKey);

	String getAccessKey();

	void setSecretKey(String secretKey);

	String getSecretKey();

	void setServiceUrl(String serviceUrl);

	String getServiceUrl();

}
