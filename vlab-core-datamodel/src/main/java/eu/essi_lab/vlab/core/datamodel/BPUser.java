package eu.essi_lab.vlab.core.datamodel;

/**
 * @author Mattia Santoro
 */
public class BPUser {
	private String email;
	private String name;

	public void setEmail(String s) {
		email = s;
	}

	public String getEmail() {
		return email;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
