package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class VLabDockerContainer {

	@JsonInclude
	private List<String> entryPoint;

	@JsonInclude
	private List<String> command;

	public List<String> getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(List<String> entryPoint) {
		this.entryPoint = entryPoint;
	}

	public List<String> getCommand() {
		return command;
	}

	public void setCommand(List<String> command) {
		this.command = command;
	}

}
