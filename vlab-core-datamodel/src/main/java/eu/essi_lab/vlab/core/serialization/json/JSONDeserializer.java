package eu.essi_lab.vlab.core.serialization.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class JSONDeserializer {

	Logger logger = LogManager.getLogger(JSONDeserializer.class);

	public <T> T deserialize(String serialized, Class<T> valueType) throws BPException {

		return deserialize(serialized, valueType, true);
	}

	public <T> T deserialize(InputStream serialized, Class<T> valueType) throws BPException {
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new Jdk8Module());

			return mapper.readValue(serialized, valueType);

		} catch (Exception e) {

			logger.error("Unparsable", e);

			throw new BPException("Unparsable: " + e.getMessage(), BPException.ERROR_CODES.UNPARSABLE_JSON.getCode());

		}
	}

	public <T> T deserialize(String serialized, Class<T> valueType, boolean printStackTrace) throws BPException {
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new Jdk8Module());

			return mapper.readValue(serialized, valueType);

		} catch (Exception e) {

			if (printStackTrace)
				logger.error("Unparsable: {}", serialized, e);
			else
				logger.warn("Unparsable: {} with error {}", serialized, e.getMessage());

			throw new BPException("Error parsing: " + serialized + " with message " + e.getMessage(),
					BPException.ERROR_CODES.UNPARSABLE_JSON.getCode());

		}
	}
}
