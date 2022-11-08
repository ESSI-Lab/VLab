package eu.essi_lab.vlab.core.serialization.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import java.io.IOException;

/**
 * @author Mattia Santoro
 */
public class RetrocompatibleBPOutputFieldDeserializer extends StdDeserializer<BPOutputDescription> {

	public RetrocompatibleBPOutputFieldDeserializer() {
		this(null);
	}

	public RetrocompatibleBPOutputFieldDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public BPOutputDescription deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

		JsonNode node = jsonParser.getCodec().readTree(jsonParser);
		JsonNode target = node.get("target");

		BPOutputDescription desc = new BPOutputDescription();
		BPOutput out;
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk8Module());
		if (node.hasNonNull("output")) {

			JsonNode inputNode = node.get("output");

			out = mapper.treeToValue(inputNode, BPOutput.class);

		} else {


			out = mapper.treeToValue(node, BPOutput.class);

		}

		desc.setOutput(out);

		if (target != null)
			desc.setTarget(target.asText());

		return desc;
	}
}