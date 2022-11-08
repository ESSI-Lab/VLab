package eu.essi_lab.vlab.core.datamodel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.SourceRef;
import org.camunda.bpm.model.bpmn.impl.instance.TargetRef;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.DataInputAssociation;
import org.camunda.bpm.model.bpmn.instance.DataObject;
import org.camunda.bpm.model.bpmn.instance.DataObjectReference;
import org.camunda.bpm.model.bpmn.instance.DataOutputAssociation;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Property;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

/**
 * This class reads and writes a BPMN which is assumed to be atomic, i.e. the model has:
 * <ul>
 *     <li> only one realization annotation (linked to the bpmn:process element)</li>
 *     <li> 0..* inputs which refer to the unique realization</li>
 *     <li> 0..* outputs which refer to the unique realization</li>
 * </ul>
 * <p>
 * Note: this class does not add the diagram elements.
 *
 * @author Mattia Santoro
 */
public class AtomicExecutableBP {

	private Logger logger = LogManager.getLogger(AtomicExecutableBP.class);

	private final BpmnModelInstance modelInstance;

	private BPMNConventionParser conventionParser;

	public AtomicExecutableBP(BPMNConventionParser parser) {
		modelInstance = Bpmn.createEmptyModel();
		conventionParser = parser;

		Definitions definitions = modelInstance.newInstance(Definitions.class);
		definitions.setTargetNamespace("http://essi-lab.eu/vlab");

		modelInstance.setDefinitions(definitions);

		String id = "autogenerated-" + new Date().getTime();
		// create the process
		Process process = createElement(definitions, id + "-process", Process.class);

		// create start event and end event
		StartEvent startEvent = createElement(process, "start", StartEvent.class);
		EndEvent endEvent = createElement(process, "end", EndEvent.class);

		ScriptTask task1 = createElement(process, id + "-task", ScriptTask.class);
		task1.setName("Task");

		createSequenceFlow(process, startEvent, task1);

		createSequenceFlow(process, task1, endEvent);

	}

	public AtomicExecutableBP(BpmnModelInstance instance, BPMNConventionParser parser) {

		modelInstance = instance;
		conventionParser = parser;

	}

	public AtomicExecutableBP(File file, BPMNConventionParser parser) {

		this(Bpmn.readModelFromFile(file), parser);

	}

	public AtomicExecutableBP(InputStream stream, BPMNConventionParser parser) {

		this(Bpmn.readModelFromStream(stream), parser);

	}

	protected SequenceFlow createSequenceFlow(org.camunda.bpm.model.bpmn.instance.Process process, FlowNode from, FlowNode to) {
		String identifier = from.getId() + "-" + to.getId();
		SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
		process.addChildElement(sequenceFlow);
		sequenceFlow.setSource(from);
		from.getOutgoing().add(sequenceFlow);
		sequenceFlow.setTarget(to);
		to.getIncoming().add(sequenceFlow);
		return sequenceFlow;
	}

	public Collection<ScriptTask> getExecutableTasks() {

		return modelInstance.getModelElementsByType(ScriptTask.class);

	}

	public DataObjectReference getDataObjectReference(String id) {
		Iterator<DataObjectReference> it = modelInstance.getModelElementsByType(DataObjectReference.class).iterator();

		while (it.hasNext()) {
			DataObjectReference ref = it.next();
			DataObject obj = ref.getDataObject();

			String objid = obj.getId();
			if (id.equalsIgnoreCase(objid))
				return ref;

		}

		return null;
	}

	public Collection<BPInput> getInputs() {

		Collection<DataObject> objects = modelInstance.getModelElementsByType(DataObject.class);

		List<BPInput> inputs = new ArrayList<>();

		for (DataObject obj : objects) {

			BPInput input = conventionParser.toBPInput(obj);

			if (input != null)
				inputs.add(input);

		}

		return inputs;

	}

	public Process getExecutableProcess() {
		return conventionParser.getExecutableProcess(modelInstance);
	}

	public DataInputAssociation getDataInputAssociation(BPInput input, ScriptTask task) {

		String sourceRef = getDataObjectReference(input.getId()).getId();

		String targetRef = new PlaceHolderProperty(input, task).getId();

		Iterator<DataInputAssociation> it = task.getChildElementsByType(DataInputAssociation.class).iterator();

		while (it.hasNext()) {

			DataInputAssociation association = it.next();

			String sRef = association.getChildElementsByType(SourceRef.class).iterator().next().getTextContent();

			String tRef = association.getChildElementsByType(TargetRef.class).iterator().next().getTextContent();

			if (sourceRef.equalsIgnoreCase(sRef) && targetRef.equalsIgnoreCase(tRef))
				return association;
		}

		return null;

	}

	public DataOutputAssociation getDataOutputAssociation(BPOutput output, ScriptTask task) {

		String targetRef = getDataObjectReference(output.getId()).getId();

		Iterator<DataOutputAssociation> it = task.getChildElementsByType(DataOutputAssociation.class).iterator();

		while (it.hasNext()) {

			DataOutputAssociation association = it.next();

			String tRef = association.getChildElementsByType(TargetRef.class).iterator().next().getTextContent();

			if (targetRef.equalsIgnoreCase(tRef))
				return association;
		}

		return null;

	}

	public DataObject addInput(BPInputDescription input, ScriptTask task) throws BPException {

		BPMNConventionParser parser = conventionParser;

		Process process = parser.getExecutableProcess(modelInstance);

		try {

			DataObject bpmnInput = parser.createInputDataObject(modelInstance, input);

			DataObjectReference reference = createElement(process, input.getInput().getId() + "-ref", DataObjectReference.class);

			reference.setName(input.getInput().getName());

			reference.setDataObject(bpmnInput);

			DataInputAssociation association = modelInstance.newInstance(DataInputAssociation.class);

			SourceRef sourceRef = modelInstance.newInstance(SourceRef.class);
			sourceRef.setTextContent(reference.getId());

			association.addChildElement(sourceRef);

			Property property = modelInstance.newInstance(Property.class);

			String pid = new PlaceHolderProperty(input.getInput(), task).getId();

			property.setId(pid);

			task.addChildElement(property);

			TargetRef targetRef = modelInstance.newInstance(TargetRef.class);
			targetRef.setTextContent(pid);

			association.addChildElement(targetRef);

			task.getDataInputAssociations().add(association);

			return bpmnInput;

		} catch (BPException e) {
			logger.error("Exception adding input {} with id {} with error {} and code {}", input.getInput().getName(),
					input.getInput().getId(), e.getMessage(), e.getErroCode());

			e.setUserMessage("Could not add input " + input.getInput().getName() + " (error code " + e.getErroCode() + ")");

			throw e;

		}

	}

	public void setExecutableTaskLabel(String name) {
		getExecutableTasks().iterator().next().setName(name);
	}

	public DataObject addOutput(BPOutputDescription output, ScriptTask task) throws BPException {

		BPMNConventionParser parser = conventionParser;

		Process process = parser.getExecutableProcess(modelInstance);

		try {

			DataObject bpmnOutput = parser.createOutputDataObject(modelInstance, output);

			DataObjectReference reference = createElement(process, output.getOutput().getId() + "-ref", DataObjectReference.class);

			reference.setName(output.getOutput().getName());

			reference.setDataObject(bpmnOutput);

			DataOutputAssociation association = modelInstance.newInstance(DataOutputAssociation.class);

			TargetRef targetRef = modelInstance.newInstance(TargetRef.class);
			targetRef.setTextContent(reference.getId());

			association.addChildElement(targetRef);

			task.getDataOutputAssociations().add(association);

			return bpmnOutput;

		} catch (BPException e) {

			logger.error("Exception adding output {} with id {} with error {} and code {}", output.getOutput().getName(),
					output.getOutput().getId(), e.getMessage(), e.getErroCode());

			e.setUserMessage("Could not add output " + output.getOutput().getName() + " (error code " + e.getErroCode() + ")");

			throw e;
		}

	}

	protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id,
			Class<T> elementClass) {

		T element = modelInstance.newInstance(elementClass);

		element.setAttributeValue("id", id, true);

		parentElement.addChildElement(element);

		return element;
	}

	public Collection<BPOutput> getOutputs() {
		Collection<DataObject> objects = modelInstance.getModelElementsByType(DataObject.class);

		List<BPOutput> outputs = new ArrayList<>();

		for (DataObject obj : objects) {

			BPOutput output = conventionParser.toBPOutput(obj);

			if (output != null)
				outputs.add(output);

		}

		return outputs;

	}

	public BPRealization getRealization() {

		Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);

		for (Process process : processes) {

			BPRealization realization = conventionParser.toBPRealization(process);

			if (realization != null)
				return realization;

		}

		return null;

	}

	public InputStream asStream() {

		return new ByteArrayInputStream(Bpmn.convertToString(modelInstance).getBytes(StandardCharsets.UTF_8));

	}

	public void setBPRealization(BPRealization realization) throws BPException {

		conventionParser.setRealization(modelInstance, realization);

	}

	public BpmnModelInstance getModelInstace() {
		return modelInstance;
	}

	public StartEvent getStartEvent() {
		return modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
	}

	public EndEvent getEndEvent() {
		return modelInstance.getModelElementsByType(EndEvent.class).iterator().next();
	}

	public SequenceFlow getSequenceFlow(FlowNode sourceNode, FlowNode tasgetNode) {
		Collection<SequenceFlow> flows = modelInstance.getModelElementsByType(SequenceFlow.class);

		for (SequenceFlow flow : flows) {
			FlowNode source = flow.getSource();

			FlowNode target = flow.getTarget();

			if (source.getId().equalsIgnoreCase(sourceNode.getId()) && target.getId().equalsIgnoreCase(tasgetNode.getId())) {
				return flow;
			}

		}

		return null;
	}

	public String getId() {
		return getExecutableProcess().getId();
	}

	public void setConventionParser(BPMNConventionParser conventionParser) {
		this.conventionParser = conventionParser;
	}
}
