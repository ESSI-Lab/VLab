package eu.essi_lab.vlab.ce.engine.services.sourcecode;

import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.CONVENTION_FOLDER;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.IO_FILE_NAME;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.SCRIPTS_FILE_NAME;
import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import java.io.File;

/**
 * @author Mattia Santoro
 */
public class SourceCodeConventionParser {

	private static final String CANTFIND = "Can't find ";
	private static final String NO_IO_FILE_MSG = CANTFIND + CONVENTION_FOLDER + "/" + IO_FILE_NAME;
	private static final String NO_DOCKER_FILE_MSG = CANTFIND + CONVENTION_FOLDER + "/" + DOCKER_IMAGE_FILE_NAME;
	private static final String NO_SCRIPT_FILE_MSG = CANTFIND + CONVENTION_FOLDER + "/" + SCRIPTS_FILE_NAME;

	private static final String CANT_READ_SOURCE_CODE_FOLDER_MSG = "Can't read source code folder";
	private static final String CANT_FIND_VLAB_FOLDER_MSG = CANTFIND + CONVENTION_FOLDER;
	private static final String VLAB_NOT_FOLDER_MSG = CONVENTION_FOLDER + " is not a folder";

	public SupportResponse validate(File dir, SupportResponse resp) {

		String path = dir.getAbsolutePath();

		if (!new File(path).exists()) {
			resp.setCanRead(false);
			resp.setConventionsOk(false);
			resp.setConventionError(CANT_READ_SOURCE_CODE_FOLDER_MSG + " " + path);

			return resp;
		}

		resp.setCanRead(true);

		File vlabfolder = new File(path + File.separator + CONVENTION_FOLDER);

		if (!vlabfolder.exists()) {
			resp.setConventionsOk(false);
			resp.setConventionError(CANT_FIND_VLAB_FOLDER_MSG);
			return resp;
		}

		if (!vlabfolder.isDirectory()) {
			resp.setConventionsOk(false);
			resp.setConventionError(VLAB_NOT_FOLDER_MSG);
			return resp;
		}

		String[] files = vlabfolder.list();

		if (!hasFile(files, IO_FILE_NAME)) {
			resp.setConventionsOk(false);
			resp.setConventionError(NO_IO_FILE_MSG);
			return resp;
		}

		if (!hasFile(files, DOCKER_IMAGE_FILE_NAME)) {
			resp.setConventionsOk(false);
			resp.setConventionError(NO_DOCKER_FILE_MSG);
			return resp;
		}

		if (!hasFile(files, SCRIPTS_FILE_NAME)) {
			resp.setConventionsOk(false);
			resp.setConventionError(NO_SCRIPT_FILE_MSG);
			return resp;
		}

		resp.setConventionsOk(true);

		return resp;
	}

	private boolean hasFile(String[] files, String fileName) {

		for (String f : files) {
			if (f.equalsIgnoreCase(fileName)) {
				return true;

			}

		}

		return false;

	}

}
