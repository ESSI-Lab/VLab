package eu.essi_lab.vlab.core.serialization.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import eu.essi_lab.vlab.core.datamodel.BPException;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class JSONSerializer {

	Logger logger = LogManager.getLogger(JSONSerializer.class);

	public String serialize(Object object) throws BPException {
		Writer jsonWriter = new StringWriter();

		try (JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new Jdk8Module());


			mapper.writer().writeValue(jsonGenerator, object);

			jsonGenerator.flush();

			return jsonWriter.toString();

		} catch (Exception e) {

			String err = "Error serializing object " + object.toString() + " with exception " + e.getMessage();

			logger.warn(err, e);

			throw new BPException(err, BPException.ERROR_CODES.SERIALIZATION_ERR);

		}
	}
}
