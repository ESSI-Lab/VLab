package eu.essi_lab.vlab.core.engine.services;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mattia Santoro
 */
public interface IESHttpResponseReader {
	InputStream readStream() throws IOException;

	int readCode();
}
