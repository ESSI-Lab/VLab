package eu.essi_lab.vlab.core.engine.bpmn;

import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import java.util.Iterator;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.DataInputAssociation;
import org.camunda.bpm.model.bpmn.instance.DataObjectReference;
import org.camunda.bpm.model.bpmn.instance.DataOutputAssociation;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPDiagramGenerator {

	private final BpmnModelInstance modelInstance;
	private final AtomicExecutableBP model;

	private Logger logger = LogManager.getLogger(BPDiagramGenerator.class);

	public BPDiagramGenerator(AtomicExecutableBP m) {
		model = m;
		modelInstance = model.getModelInstace();
	}

	public AtomicExecutableBP createDiagram() {

		logger.debug("Initializaing Diagram elements");
		Process process = model.getExecutableProcess();

		BpmnPlane bpmnPlane = init(process);

		double startX = 200;
		double startRow = 200;

		HorizontalCounter hconuter = new HorizontalCounter(startX);

		hconuter.setRow(startRow);

		//start
		logger.trace("Start Shape");
		createEventShape(model.getStartEvent(), bpmnPlane, hconuter);

		//task
		logger.trace("Task Shape");
		ScriptTask task = model.getExecutableTasks().iterator().next();

		BpmnShape taskShape = createTaskShape(task, bpmnPlane, hconuter);

		logger.trace("Start to Task Edge");
		SequenceFlow sf1 = model.getSequenceFlow(model.getStartEvent(), task);

		BpmnEdge sf1Edge = new ProcessBuilder(modelInstance, process).createBpmnEdge(sf1);
		bpmnPlane.addChildElement(sf1Edge);

		//end
		logger.trace("End Shape");
		createEventShape(model.getEndEvent(), bpmnPlane, hconuter);

		logger.trace("Task to End Edge");
		SequenceFlow sf2 = model.getSequenceFlow(task, model.getEndEvent());

		BpmnEdge sf2Edge = new ProcessBuilder(modelInstance, process).createBpmnEdge(sf2);
		bpmnPlane.addChildElement(sf2Edge);

		Iterator<BPInput> inputs = model.getInputs().iterator();

		HorizontalCounter hinputCounter = new HorizontalCounter(startX + 50);
		hinputCounter.setRow(startRow - 150);

		while (inputs.hasNext()) {

			BPInput input = inputs.next();
			logger.trace("Input {}", input.getId());

			addInput(input, hinputCounter, bpmnPlane, taskShape, task);
		}

		Iterator<BPOutput> outputs = model.getOutputs().iterator();

		HorizontalCounter houtputCounter = new HorizontalCounter(startX + 50);

		houtputCounter.setRow(startRow + 150);

		while (outputs.hasNext()) {

			BPOutput output = outputs.next();
			logger.trace("Output {}", output.getId());

			addOutput(output, houtputCounter, bpmnPlane, taskShape, task);
		}

		logger.debug("Completed Diagram");

		return model;

	}

	private void addOutput(BPOutput output, HorizontalCounter houtputCounter, BpmnPlane bpmnPlane, BpmnShape taskShape, ScriptTask task) {

		DataObjectReference datObj = model.getDataObjectReference(output.getId());

		BpmnShape outputShape = createDataOutputShape(datObj, bpmnPlane, houtputCounter);

		createBpmnEdge(model.getDataOutputAssociation(output, task), taskShape, outputShape, bpmnPlane);

	}

	private BpmnShape createDataOutputShape(DataObjectReference datObj, BpmnPlane bpmnPlane, HorizontalCounter hinputCounter) {

		return createDataShape(datObj, bpmnPlane, hinputCounter, 50.00, 60);

	}

	private BpmnShape createDataShape(DataObjectReference datObj, BpmnPlane bpmnPlane, HorizontalCounter hinputCounter, Double h,
			Integer hincr) {

		BpmnShape bpmnShape = modelInstance.newInstance(BpmnShape.class);

		bpmnShape.setBpmnElement(datObj);

		Bounds nodeBounds = modelInstance.newInstance(Bounds.class);

		double w = 36.0D;

		double leftSpacing = 30.0D;

		nodeBounds.setWidth(w);
		nodeBounds.setHeight(h);

		nodeBounds.setX(hinputCounter.getCurrent() + leftSpacing);
		nodeBounds.setY(calculateY(h, hinputCounter));

		BpmnLabel label = modelInstance.newInstance(BpmnLabel.class);

		Bounds labelBounds = modelInstance.newInstance(Bounds.class);
		double labelW = 50.0D;

		labelBounds.setWidth(labelW);
		labelBounds.setHeight(15.0D);

		labelBounds.setX(hinputCounter.getCurrent() + leftSpacing - ((labelW - w) / 2));
		labelBounds.setY(calculateY(h, hinputCounter) + hincr);

		label.addChildElement(labelBounds);

		bpmnShape.addChildElement(label);
		bpmnShape.addChildElement(nodeBounds);
		bpmnPlane.addChildElement(bpmnShape);

		hinputCounter.incr(leftSpacing + labelW);

		return bpmnShape;

	}

	private void addInput(BPInput input, HorizontalCounter hinputCounter, BpmnPlane bpmnPlane, BpmnShape taskShape, ScriptTask task) {

		DataObjectReference datObj = model.getDataObjectReference(input.getId());

		BpmnShape inputShape = createDataInputShape(datObj, bpmnPlane, hinputCounter);

		createBpmnEdge(model.getDataInputAssociation(input, task), inputShape, taskShape, bpmnPlane);

	}

	public void createBpmnEdge(DataOutputAssociation association, BpmnShape source, BpmnShape target, BpmnPlane bpmnPlane) {

		BpmnEdge edge = modelInstance.newInstance(BpmnEdge.class);

		edge.setBpmnElement(association);

		setWaypoints(edge, source, target);

		bpmnPlane.addChildElement(edge);

	}

	public void createBpmnEdge(DataInputAssociation association, BpmnShape source, BpmnShape target, BpmnPlane bpmnPlane) {

		BpmnEdge edge = modelInstance.newInstance(BpmnEdge.class);

		edge.setBpmnElement(association);

		setWaypoints(edge, source, target);

		bpmnPlane.addChildElement(edge);

	}

	private void setWaypoints(BpmnEdge edge, BpmnShape source, BpmnShape target) {

		Bounds sourceBounds = source.getBounds();
		Bounds targetBounds = target.getBounds();
		double sourceX = sourceBounds.getX();
		double sourceY = sourceBounds.getY();
		double sourceWidth = sourceBounds.getWidth();
		double sourceHeight = sourceBounds.getHeight();
		double targetX = targetBounds.getX();
		double targetY = targetBounds.getY();

		double targetWidth = targetBounds.getWidth();

		Waypoint w1 = modelInstance.newInstance(Waypoint.class);
		w1.setX(sourceX + sourceWidth / 2.0D);

		w1.setY(sourceY + sourceHeight);

		edge.addChildElement(w1);

		Waypoint w3 = modelInstance.newInstance(Waypoint.class);
		w3.setX(targetX + targetWidth / 2.0D);

		w3.setY(targetY);

		edge.addChildElement(w3);

	}

	private BpmnShape createDataInputShape(DataObjectReference datObj, BpmnPlane bpmnPlane, HorizontalCounter hinputCounter) {

		return createDataShape(datObj, bpmnPlane, hinputCounter, 50.00, -20);

	}

	public BpmnShape createTaskShape(Activity event, BpmnPlane bpmnPlane, HorizontalCounter counter) {

		BpmnShape bpmnShape = modelInstance.newInstance(BpmnShape.class);

		bpmnShape.setBpmnElement(event);

		int nameLength = event.getName().length();

		Bounds nodeBounds = modelInstance.newInstance(Bounds.class);

		double w = 7.0D * nameLength;

		w = Math.max(w, 100.0D);

		double h = 80.0D;

		return finalizeShape(bpmnPlane, bpmnShape, nodeBounds, counter, h, w);

	}

	private BpmnShape finalizeShape(BpmnPlane bpmnPlane, BpmnShape bpmnShape, Bounds nodeBounds, HorizontalCounter counter, Double h,
			Double w) {

		nodeBounds.setWidth(w);
		nodeBounds.setHeight(h);

		double x = counter.getCurrent();

		double leftSpacing = 50.0D;

		nodeBounds.setX(x + leftSpacing);

		nodeBounds.setY(calculateY(h, counter));

		counter.incr(w + leftSpacing);

		bpmnShape.addChildElement(nodeBounds);
		bpmnPlane.addChildElement(bpmnShape);

		return bpmnShape;
	}

	public BpmnShape createEventShape(Event event, BpmnPlane bpmnPlane, HorizontalCounter counter) {

		BpmnShape bpmnShape = modelInstance.newInstance(BpmnShape.class);

		bpmnShape.setBpmnElement(event);

		Bounds nodeBounds = modelInstance.newInstance(Bounds.class);

		double w = 36.0D;
		double h = 36.0D;

		return finalizeShape(bpmnPlane, bpmnShape, nodeBounds, counter, h, w);

	}

	private double calculateY(double h, HorizontalCounter counter) {

		double rowCenter = counter.getRow();

		double h2 = h / 2;

		return rowCenter - h2;

	}

	private BpmnPlane init(Process process) {

		BpmnDiagram bpmnDiagram = modelInstance.newInstance(BpmnDiagram.class);

		BpmnPlane bpmnPlane = modelInstance.newInstance(BpmnPlane.class);

		bpmnPlane.setBpmnElement(process);

		bpmnDiagram.addChildElement(bpmnPlane);

		Definitions definitions = modelInstance.getModelElementsByType(Definitions.class).iterator().next();

		definitions.addChildElement(bpmnDiagram);

		return bpmnPlane;
	}

}

