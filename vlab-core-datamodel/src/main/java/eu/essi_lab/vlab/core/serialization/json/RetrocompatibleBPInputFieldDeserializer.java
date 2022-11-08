package eu.essi_lab.vlab.core.serialization.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class RetrocompatibleBPInputFieldDeserializer extends StdDeserializer<BPInputDescription> {

	public RetrocompatibleBPInputFieldDeserializer() {
		this(null);
	}

	public RetrocompatibleBPInputFieldDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public BPInputDescription deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

		JsonNode node = jsonParser.getCodec().readTree(jsonParser);
		JsonNode target = node.get("target");
		JsonNode dv = node.get("defaultValue");
		JsonNode dva = node.get("defaultValueArray");

		BPInputDescription desc = new BPInputDescription();
		BPInput in;
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk8Module());
		if (node.hasNonNull("input")) {

			JsonNode inputNode = node.get("input");

			in = mapper.treeToValue(inputNode, BPInput.class);

		} else {

			in = mapper.treeToValue(node, BPInput.class);

		}

		desc.setInput(in);

		if (dv != null)
			desc.setDefaultValue(dv.asText());

		if (dva != null) {

			Iterator<JsonNode> iterator = dva.iterator();
			List<Object> list = new ArrayList<>();
			while (iterator.hasNext())
				list.add(iterator.next().asText());

			desc.setDefaultValueArray(list);
		}

		if (target != null)
			desc.setTarget(target.asText());

		return desc;
	}
}
