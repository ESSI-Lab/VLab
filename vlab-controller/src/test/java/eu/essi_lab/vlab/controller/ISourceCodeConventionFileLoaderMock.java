package eu.essi_lab.vlab.controller;

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
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class ISourceCodeConventionFileLoaderMock {

	public static ISourceCodeConventionFileLoader mocked(String sourceCodeRootPath) throws BPException {

		ISourceCodeConventionFileLoader mock = Mockito.mock(ISourceCodeConventionFileLoader.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws BPException {

				FileInputStream stream = openFileStream(
						new File(sourceCodeRootPath + File.separator + CONVENTION_FOLDER + File.separator + IO_FILE_NAME));
				return new JSONDeserializer().deserialize(stream, BPIOObject.class);

			}
		}).when(mock).loadIOFile();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws BPException {

				FileInputStream stream = openFileStream(
						new File(sourceCodeRootPath + File.separator + CONVENTION_FOLDER + File.separator + SCRIPTS_FILE_NAME));
				return new JSONDeserializer().deserialize(stream, Scripts.class).getScripts();

			}
		}).when(mock).loadScripts();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws BPException {

				FileInputStream stream = openFileStream(
						new File(sourceCodeRootPath + File.separator + CONVENTION_FOLDER + File.separator + DOCKER_IMAGE_FILE_NAME));
				return new JSONDeserializer().deserialize(stream, VLabDockerImage.class);

			}
		}).when(mock).loadDockerImage();

		try {
			Mockito.doAnswer(new Answer() {
				@Override
				public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

					File rootFile = new File(sourceCodeRootPath + File.separator + ((Script) invocationOnMock.getArguments()[0]).getRepoPath());

					return getScriptFiles(rootFile);
				}
			}).when(mock).getScriptFiles(Mockito.any());
		} catch (FileNotFoundException e) {
			throw new BPException();
		}

		return mock;

	}

	private static List<File> getScriptFiles(File file) {

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

	private static boolean checkDirectory(File file) {
		return file.isDirectory();
	}

	private static FileInputStream openFileStream(File iofile) throws BPException {
		try {
			return new FileInputStream(iofile);
		} catch (FileNotFoundException e) {
			throw new BPException();
		}
	}
}
