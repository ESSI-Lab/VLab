package eu.essi_lab.vlab.core.engine.conventions;

import eu.essi_lab.vlab.core.datamodel.BPIOObject;
import eu.essi_lab.vlab.core.datamodel.Scripts;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.CONVENTION_FOLDER;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.IO_FILE_NAME;
import static eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions.SCRIPTS_FILE_NAME;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				FileInputStream stream = openFileStream(
						new File(sourceCodeRootPath + File.separator + CONVENTION_FOLDER + File.separator + IO_FILE_NAME));
				return new JSONDeserializer().deserialize(stream, BPIOObject.class);

			}
		}).when(mock).loadIOFile();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				FileInputStream stream = openFileStream(
						new File(sourceCodeRootPath + File.separator + CONVENTION_FOLDER + File.separator + SCRIPTS_FILE_NAME));
				return new JSONDeserializer().deserialize(stream, Scripts.class).getScripts();

			}
		}).when(mock).loadScripts();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				FileInputStream stream = openFileStream(
						new File(sourceCodeRootPath + File.separator + CONVENTION_FOLDER + File.separator + DOCKER_IMAGE_FILE_NAME));
				return new JSONDeserializer().deserialize(stream, VLabDockerImage.class);

			}
		}).when(mock).loadDockerImage();

		return mock;

	}

	private static FileInputStream openFileStream(File iofile) throws IOException {
		return new FileInputStream(iofile);
	}
}
