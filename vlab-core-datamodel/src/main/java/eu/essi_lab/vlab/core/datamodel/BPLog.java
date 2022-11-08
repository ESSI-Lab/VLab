package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class BPLog implements Comparable {

	private String message;
	private final Long nanostamp;
	private final Long timestamp;

	public BPLog(Long dateMilli, Long nanoMilli) {
		timestamp = dateMilli;
		nanostamp = nanoMilli;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int compareTo(Object o) {

		Long ts = ((BPLog) o).getTimestamp();
		Long delta = getTimestamp() - ts;

		if (delta < 0L)
			return -1;
		if (delta > 0L)
			return 1;

		delta = getNanostamp() - ((BPLog) o).getNanostamp();

		if (delta < 0L)
			return -1;
		if (delta > 0L)
			return 1;

		return 0;

	}

	public Long getNanostamp() {
		return nanostamp;
	}
}
