package eu.essi_lab.vlab.core.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class ConfigurationLoader {

	private static String confFile = "vlab.properties";

	private static Properties properties;
	private static Logger logger = LogManager.getLogger(ConfigurationLoader.class);

	private ConfigurationLoader() {
	}

	public static void setConfFilePath(String filePath) {

		try (InputStream stream = new FileInputStream(Paths.get(filePath).toFile())) {

			setConfFileStream(stream);

		} catch (IOException e) {
			logger.error("Can't load {}", filePath, e);

		}

	}

	public static void setConfFileStream(InputStream stream) throws IOException {

		properties = new Properties();
		properties.load(stream);

	}

	public static String loadConfigurationParameter(ConfigurationParameter parameter) {

		if (null == parameter)
			return null;

		String v = ConfigurationLoader.load(parameter.getKey(), confFile);

		if (null == v)
			v = parameter.getDefaultValue();

		return v;
	}

	private static String load(String conf, String confFile) {

		String env = System.getenv(conf);

		if (env != null) {

			return env;
		}

		env = System.getenv(conf.replace(".", ""));

		if (env != null) {

			return env;
		}

		if (properties == null) {
			properties = new Properties();

			try (InputStream stream = ConfigurationLoader.class.getClassLoader().getResourceAsStream(confFile)) {

				if (null == stream)
					return null;
				properties.load(stream);

			} catch (IOException e) {

				logger.warn("Unable to load properties from {}", confFile, e);

			}
		}
		return properties.getProperty(conf);

	}
}
