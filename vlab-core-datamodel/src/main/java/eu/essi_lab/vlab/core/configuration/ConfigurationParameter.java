package eu.essi_lab.vlab.core.configuration;

/**
 * @author Mattia Santoro
 */
public class ConfigurationParameter {

	private final String key;
	private String defaultValue;

	public ConfigurationParameter(String key) {
		this.key = key;
	}

	public ConfigurationParameter(String key, String firstParamValue, String... otherParamsValues) {

		if (firstParamValue != null) {
			key = key.replaceFirst("PARAM", firstParamValue);
			for (String v : otherParamsValues) {
				key = key.replaceFirst("PARAM", v);
			}
		}
		this.key = key;

	}

	public String getKey() {
		return this.key;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
