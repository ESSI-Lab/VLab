package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class ESQueryBPRuns extends ESQueryObject {

	private String text;
	private int start;
	private int count;
	private String wfid;

	public String getWfid() {
		return wfid;
	}

	public void setWfid(String wfid) {
		this.wfid = wfid;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}
}
