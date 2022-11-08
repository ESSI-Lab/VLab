package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public enum BPRunStatuses {

    QUEUED,
	MOVING,
    RESERVING_RESOURCES,
    INGESTING_INPUTS,
    EXECUTING,
    SAVING_OUTPUTS,
    COMPLETED;
}
