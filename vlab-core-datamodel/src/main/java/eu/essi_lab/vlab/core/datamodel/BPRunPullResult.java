package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class BPRunPullResult {

    private PullResult result;

    private String runid;

    public PullResult getResult() {
	return result;
    }

    public void setResult(PullResult result) {
	this.result = result;
    }

    public String getRunid() {
	return runid;
    }

    public void setRunid(String runid) {
	this.runid = runid;
    }
}
