package eu.essi_lab.vlab.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author mattia
 */
public class URLReader {

	public InputStream read(String urlString) throws IOException {

		URL url = new URL(urlString);

		return url.openStream();

	}
}
