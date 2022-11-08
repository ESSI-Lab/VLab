package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VLabDockerImage {

	@JsonInclude
	private String image;

	@JsonInclude
	private VLabDockerResources resources;

	@JsonInclude
	private VLabDockerContainer container;

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public VLabDockerResources getResources() {
		return resources;
	}

	public void setResources(VLabDockerResources resources) {
		this.resources = resources;
	}

	public VLabDockerContainer getContainer() {
		return container;
	}

	public void setContainer(VLabDockerContainer container) {
		this.container = container;
	}
}
