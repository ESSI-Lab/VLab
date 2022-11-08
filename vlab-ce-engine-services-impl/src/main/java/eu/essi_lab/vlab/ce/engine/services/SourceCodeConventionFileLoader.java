package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPIOObject;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.CONVENTION_FOLDER;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.IO_FILE_NAME;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.SCRIPTS_FILE_NAME;
import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.datamodel.Scripts;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class SourceCodeConventionFileLoader implements ISourceCodeConventionFileLoader {

	private String rootPath;

	private Logger logger = LogManager.getLogger(SourceCodeConventionFileLoader.class);

	public SourceCodeConventionFileLoader() {
	}

	public SourceCodeConventionFileLoader(String sourceCodeRootPath) {
		this();
		rootPath = sourceCodeRootPath;
	}

	@Override
	public BPIOObject loadIOFile() throws BPException {

		File iofile = new File(rootPath + File.separator + CONVENTION_FOLDER + File.separator + IO_FILE_NAME);

		try (FileInputStream stream = openFileStream(iofile)) {

			return new JSONDeserializer().deserialize(stream, BPIOObject.class);

		} catch (FileNotFoundException e) {

			logger.error("FileNotFoundException reading vlab I/O file", e);
			throw new BPException("Unable to find vlab I/O file", BPException.ERROR_CODES.IO_FILE_NOT_FOUND);

		} catch (IOException e) {

			logger.error("IOException reading vlab I/O file", e);
			throw new BPException("Unable to read vlab I/O file", BPException.ERROR_CODES.IO_FILE_READ_ERROR);

		}

	}

	@Override
	public List<Script> loadScripts() throws BPException {

		File iofile = new File(rootPath + File.separator + CONVENTION_FOLDER + File.separator + SCRIPTS_FILE_NAME);

		try (FileInputStream stream = openFileStream(iofile)) {

			return new JSONDeserializer().deserialize(stream, Scripts.class).getScripts();

		} catch (FileNotFoundException e) {

			logger.error("Can't find scripts file", e);

			throw new BPException("Unable to find scripts file", BPException.ERROR_CODES.SCRIPTS_FILE_NOT_FOUND);
		} catch (IOException e) {

			logger.error("IOException reading vlab scripts file", e);
			throw new BPException("Unable to read scripts file", BPException.ERROR_CODES.SCRIPTS_FILE_READ_ERROR);

		}

	}

	FileInputStream openFileStream(File iofile) throws IOException {
		return new FileInputStream(iofile);
	}

	@Override
	public List<File> getScriptFiles(Script script) {

		File rootFile = new File(rootPath + File.separator + script.getRepoPath());

		return getScriptFiles(rootFile);

	}

	private List<File> getScriptFiles(File file) {

		List<File> list = new ArrayList<>();

		if (checkDirectory(file)) {

			File[] subFiles = file.listFiles();

			for (File f : subFiles)
				list.addAll(getScriptFiles(f));

		} else {

			list.add(file);
		}

		return list;

	}

	boolean checkDirectory(File file) {
		return file.isDirectory();
	}

	@Override
	public VLabDockerImage loadDockerImage() throws BPException {

		File iofile = new File(rootPath + File.separator + CONVENTION_FOLDER + File.separator + DOCKER_IMAGE_FILE_NAME);

		try (FileInputStream stream = openFileStream(iofile)) {

			return new JSONDeserializer().deserialize(stream, VLabDockerImage.class);

		} catch (FileNotFoundException e) {

			logger.error("Can't find docker image file", e);

			throw new BPException("Unable to find docker image file", BPException.ERROR_CODES.NO_DOCKER_IMAGE_FILE_FUOND);

		} catch (IOException e) {

			logger.error("IOException reading docker image file", e);
			throw new BPException("Unable to read docker image file", BPException.ERROR_CODES.DOCKER_IMAGE_READ_ERROR);

		}

	}

	@Override
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getRootPath() {
		return this.rootPath;
	}
}
