package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPIOObject;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public interface ISourceCodeConventionFileLoader {
	VLabDockerImage loadDockerImage() throws BPException;

	BPIOObject loadIOFile() throws BPException;

	List<Script> loadScripts() throws BPException;

	List<File> getScriptFiles(Script script) throws FileNotFoundException;

	void setRootPath(String rootPath);
}
