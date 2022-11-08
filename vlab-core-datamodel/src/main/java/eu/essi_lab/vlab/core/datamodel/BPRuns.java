package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class BPRuns {

    @JsonInclude
    private List<BPRun> runs;

    @JsonInclude
    private Integer total;

    public List<BPRun> getRuns() {
	return runs;
    }

    public void setRuns(List<BPRun> list) {
	this.runs = list;
    }

    public Integer getTotal() {
	return total;
    }

    public void setTotal(Integer total) {
	this.total = total;
    }
}
