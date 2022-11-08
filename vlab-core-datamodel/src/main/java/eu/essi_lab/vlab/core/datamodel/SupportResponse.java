package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class SupportResponse {

	private boolean canRead;

	private boolean conventionsOk;

	private String conventionError;

	public boolean isCanRead() {
		return canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isConventionsOk() {
		return conventionsOk;
	}

	public void setConventionsOk(boolean conventionsOk) {
		this.conventionsOk = conventionsOk;
	}

	public String getConventionError() {
		return conventionError;
	}

	public void setConventionError(String conventionError) {
		this.conventionError = conventionError;
	}
}
