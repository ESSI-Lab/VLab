package eu.essi_lab.vlab.core.datamodel;

import org.camunda.bpm.model.bpmn.instance.ScriptTask;

/**
 * @author Mattia Santoro
 */
public class PlaceHolderProperty {

	private final ScriptTask t;
	private final BPInput i;

	public PlaceHolderProperty(BPInput input, ScriptTask task) {
		i = input;
		t = task;
	}

	public String getId() {
		return i.getId() + t.getId() + "propertyid";
	}
}
