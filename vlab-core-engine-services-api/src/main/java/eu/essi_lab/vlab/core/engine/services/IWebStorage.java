package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public interface IWebStorage extends IBPConfigurableService {

	/**
	 * Uploads the stream to a WebStorage with the provided key. Returns a {@link WebStorageObject} with key of the stored object.
	 *
	 * @param stream
	 * @return the uploaded {@link WebStorageObject}
	 */
	WebStorageObject upload(InputStream stream, String key) throws BPException;

	/**
	 * Uploads the file to AWS S3 bucket. Returns an {@link WebStorageObject} with bucket name and key which were used to upoload this file.
	 *
	 * @param file
	 * @return the uploaded {@link WebStorageObject}
	 */
	WebStorageObject upload(File file, String key) throws BPException;

	/**
	 * Returns the keys of all objects in the provided directory.
	 *
	 * @param directory
	 * @return
	 */
	List<String> listSubOjects(int start, int count, String directory) throws BPException;

	/**
	 * Returns the keys of all objects in the provided directory.
	 *
	 * @param directory
	 * @return
	 */
	List<String> listSubOjects(String directory) throws BPException;

	/**
	 * Reads the Web Object with key from the Web Storage. Returns the content {@link InputStream} or null if no object with the provided
	 * key is present in the bucket.
	 *
	 * @param key
	 * @return
	 */
	InputStream read(String key) throws BPException;

	/**
	 * Deletes the object identified by key from the bucket.
	 *
	 * @param key
	 * @return true if operation succeeds, false otherwise
	 */
	Boolean remove(String key);

	void setBucket(String bucketName);

}
