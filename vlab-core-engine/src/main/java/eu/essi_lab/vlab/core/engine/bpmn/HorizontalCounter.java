package eu.essi_lab.vlab.core.engine.bpmn;

/**
 * @author Mattia Santoro
 */
public class HorizontalCounter {

    private double current;
    private double row;

    public HorizontalCounter(double start) {

	current = start;
    }

    /**
     * Returns X of the right side of the right element.
     *
     * @return
     */
    public double getCurrent() {
	return current;
    }

    /**
     * The Y value for the vertical center of this row
     *
     * @return
     */
    public double getRow() {
	return row;
    }

    /**
     * The Y value for the vertical center of this row
     *
     * @return
     */
    public void setRow(double row) {
	this.row = row;
    }

    public void incr(double w) {
	current = current + w;
    }
}
