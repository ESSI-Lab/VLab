package eu.essi_lab.vlab.core.datamodel;


import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.DataObject;
import org.camunda.bpm.model.bpmn.instance.Documentation;
import org.camunda.bpm.model.bpmn.instance.Process;


/**
 * @author Mattia Santoro
 */
public class BPMNConventionParser {

	private static final String BPINPUT_TEXT_FORMAT = "text/x-comments";

	private Logger logger = LogManager.getLogger(BPMNConventionParser.class);

	/**
	 * Parses a {@link DataObject} and returns the {@link BPInput}, or null if the {@link DataObject} is not a valid input representation.
	 * According to the conventions, a valid input object is as it follows:
	 * <pre>
	 * {@code
	 * <bpmn:dataObject id="DataObject_1ogs25p">
	 *     <bpmn:documentation textFormat="text/x-comments">
	 * 		<![CDATA[ {"inputObject" : {
	 * 				"inputType": "array",
	 * 				"valueType" : "keyValue",
	 * 				"description" : "Thematic Layers description",
	 * 				"name": "Thematic Layers",
	 * 				"obligation": true,
	 * 				"hasDefault": true,
	 * 				"valueSchema": "url" }}
	 * 		]]>
	 * 	   </bpmn:documentation>
	 * </bpmn:dataObject>
	 * }
	 * </pre>
	 *
	 * @param object
	 * @return the {@link BPInput}, or null if the {@link DataObject} is not a valid input representation.
	 */
	public BPInput toBPInput(DataObject object) {

		Collection<Documentation> documentaitons = object.getChildElementsByType(Documentation.class);

		if (documentaitons != null) {

			for (Documentation doc : documentaitons) {

				BPInputObject inputObj = parsedoc(doc, BPInputObject.class);

				if (inputObj != null) {

					BPInput input = inputObj.getInputObject();

					input.setId(object.getId());

					return input;
				}
			}

		}

		return null;

	}

	public DataObject createInputDataObject(BpmnModelInstance modelInstance, BPInputDescription input) throws BPException {

		DataObject element = modelInstance.newInstance(DataObject.class);

		element.setAttributeValue("id", input.getInput().getId(), true);

		Process process = getExecutableProcess(modelInstance);

		Documentation documentation = modelInstance.newInstance(Documentation.class);

		documentation.setTextFormat(BPINPUT_TEXT_FORMAT);

		BPInputObject object = new BPInputObject();

		object.setInputObject(input.getInput());

		documentation.setTextContent(new JSONSerializer().serialize(object));

		element.addChildElement(documentation);

		process.addChildElement(element);

		return element;

	}

	public DataObject createOutputDataObject(BpmnModelInstance modelInstance, BPOutputDescription output) throws BPException {

		DataObject element = modelInstance.newInstance(DataObject.class);

		element.setAttributeValue("id", output.getOutput().getId(), true);

		Process process = getExecutableProcess(modelInstance);

		Documentation documentation = modelInstance.newInstance(Documentation.class);

		documentation.setTextFormat(BPINPUT_TEXT_FORMAT);

		BPOutputObject object = new BPOutputObject();

		object.setOutputObject(output.getOutput());

		documentation.setTextContent(new JSONSerializer().serialize(object));

		element.addChildElement(documentation);

		process.addChildElement(element);

		return element;
	}

	/**
	 * Parses a {@link DataObject} and returns the {@link BPOutput}, or null if the {@link DataObject} is not a valid output representation.
	 * According to the conventions, a valid output object is as it follows:
	 * <pre>
	 * {@code
	 * <bpmn:dataObject id="DataObject_0loj2kk">
	 *     <bpmn:documentation textFormat="text/x-comments">
	 *         <![CDATA[ {"outputObject" : {
	 *         		"outputType": "individual",
	 * 			"valueType" : "value",
	 * 			"description" : "Thematic Layers description",
	 * 			"name": "Clumps L4 Period 2",
	 * 			"valueSchema": "url" }}
	 * 		]]>
	 * 	   </bpmn:documentation>
	 * </bpmn:dataObject>
	 * }
	 * </pre>
	 *
	 * @param object
	 * @return the {@link BPOutput}, or null if the {@link DataObject} is not a valid output representation.
	 */
	public BPOutput toBPOutput(DataObject object) {

		Collection<Documentation> documentaitons = object.getChildElementsByType(Documentation.class);

		if (documentaitons != null) {

			for (Documentation doc : documentaitons) {

				BPOutputObject outputObj = parsedoc(doc, BPOutputObject.class);

				if (outputObj != null) {

					BPOutput output = outputObj.getOutputObject();

					output.setId(object.getId());

					return output;
				}
			}

		}

		return null;

	}

	public <T> T parsedoc(Documentation doc, Class<T> valueType) {

		String textFormat = doc.getTextFormat();

		if (BPINPUT_TEXT_FORMAT.equalsIgnoreCase(textFormat)) {

			String content = doc.getTextContent();

			try {

				return new JSONDeserializer().deserialize(content, valueType, false);

			} catch (BPException e) {
				logger.warn("exception deserializing to {}", valueType.getSimpleName(), e);
			}

		}

		return null;

	}

	/**
	 * Parses a {@link Process} and returns its {@link BPRealization}, or null if the {@link Process} is not a valid realization
	 * representation. According to the conventions, a valid process realization is as it follows:
	 * <pre>
	 * {@code
	 * <bpmn:process id="Process_1" isExecutable="true">
	 * 		<bpmn:documentation textFormat="text/x-comments">
	 * 			<![CDATA[ {"realization" : {
	 * 				   	"realizationURI" : "http://example.com"}}
	 * 			]]>
	 * 		</bpmn:documentation>
	 * 	....
	 * </bpmn:process>
	 * }
	 * </pre>
	 *
	 * @param process
	 * @return the {@link BPRealization}, or null if the {@link Process} is not a valid process realization.
	 */
	public BPRealization toBPRealization(Process process) {

		Collection<Documentation> documentaitons = process.getChildElementsByType(Documentation.class);

		if (documentaitons != null) {

			for (Documentation doc : documentaitons) {

				BPRealizationObject realizationObject = parsedoc(doc, BPRealizationObject.class);

				if (realizationObject != null) {

					return realizationObject.getRealization();

				}
			}

		}

		return null;

	}

	public void setRealization(BpmnModelInstance modelInstance, BPRealization realization) throws BPException {

		Documentation documentation = modelInstance.newInstance(Documentation.class);

		documentation.setTextFormat(BPINPUT_TEXT_FORMAT);

		BPRealizationObject object = new BPRealizationObject();

		object.setRealization(realization);

		documentation.setTextContent(new JSONSerializer().serialize(object));

		Process process = getExecutableProcess(modelInstance);

		Collection<Documentation> documentaitons = process.getDocumentations();

		if (documentaitons != null) {

			for (Documentation doc : documentaitons) {

				BPRealizationObject realizationObject = parsedoc(doc, BPRealizationObject.class);

				if (realizationObject != null) {

					process.replaceChildElement(doc, documentation);

					return;

				}
			}

		}

		process.addChildElement(documentation);

	}

	public Process getExecutableProcess(BpmnModelInstance modelInstance) {

		Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);

		for (Process process : processes) {
			//First I try to replace the existing realization

			Collection<Documentation> documentaitons = process.getDocumentations();

			if (documentaitons != null) {

				for (Documentation doc : documentaitons) {

					BPRealizationObject realizationObject = parsedoc(doc, BPRealizationObject.class);

					if (realizationObject != null) {

						return process;

					}
				}

			}
		}

		for (Process process : processes) {
			//If no process has an existing realization, I add it to the first one

			if (process != null)
				return process;

		}

		return null;

	}

}
