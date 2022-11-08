package eu.essi_lab.vlab.core.engine.conventions;

import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;

/**
 * @author Mattia Santoro
 */
public class PathConventionParser {

	private final String root;
	private final String runid;
	private static final String PARAMETER_FILE = "vlabparams.json";

	public PathConventionParser(String runid, String vlabRootFolder) {
		this.runid = runid;
		this.root = vlabRootFolder;

	}

	/**
	 * Creates the absolute path on the docker container where to write the input file.
	 *
	 * @param input
	 * @return the absolute path on the docker container where to write the input file.
	 */
	public String getDockerContainerAbsolutePath(BPInputDescription input) {

		return root + "/" + runid + "/" + input.getTarget();
	}

	/**
	 * Creates the absolute path on the docker container where to write the input, with the specified suffix.
	 *
	 * @param input
	 * @param suffix
	 * @return the absolute path on the docker container where to write the input file.
	 */
	public String getDockerContainerAbsolutePath(BPInputDescription input, String suffix) {

		return root + "/" + runid + "/" + input.getTarget() + "/" + suffix;
	}

	/**
	 * Creates the absolute path on the docker container where to write the poarameter file.
	 *
	 * @return the absolute path on the docker container where to write the parameter file.
	 */
	public String getDockerContainerAbsolutePathParameterFile() {

		return root + "/" + runid + "/" + PARAMETER_FILE;
	}

	/**
	 * Creates the absolute path on the docker container where to retrieve the output file.
	 *
	 * @param output
	 * @return the absolute path on the docker container where to retrieve the output file.
	 */
	public String getDockerContainerAbsolutePath(BPOutputDescription output) {

		return root + "/" + runid + "/" + output.getTarget();
	}

	/**
	 * Creates the absolute path on the docker container where to write the script file.
	 *
	 * @param input
	 * @return the absolute path on the docker container where to write the script file.
	 */
	public String getDockerContainerScriptAbsolutePath(Script input) {

		return root + "/" + runid + "/" + input.getTargetPath();
	}

	/**
	 * Returns the run root folder on the Docker container
	 *
	 * @return
	 */
	public String getRunFolder() {

		return root + "/" + runid;

	}

}
