package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPIOObject;
import eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions;
import eu.essi_lab.vlab.core.datamodel.PathType;
import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class SourceCodeConventionFileLoaderTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testGetScriptFiles() throws FileNotFoundException {

		String sourceCodeRootPath = "/codeRoot";
		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(sourceCodeRootPath));

		Script script = Mockito.mock(Script.class);

		String dirPath = "dirPath";
		Mockito.when(script.getRepoPath()).thenReturn(dirPath);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				File f = ((File) invocation.getArguments()[0]);

				String path = f.getAbsolutePath();

				if (path.contains(BPSourceCodeConventions.CONVENTION_FOLDER))
					throw new Exception("Bad source coode path was generated");

				return false;
			}
		}).when(loader).checkDirectory(Mockito.any());

		List<File> files = loader.getScriptFiles(script);
	}

	@Test
	public void testNoSciptFile() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Unable to find scripts file", BPException.ERROR_CODES.SCRIPTS_FILE_NOT_FOUND));
		URL dirUrl = this.getClass().getClassLoader().getResource("noScriptFile/");

		SourceCodeConventionFileLoader loader = new SourceCodeConventionFileLoader(dirUrl.getPath());

		loader.loadScripts();
	}

	@Test
	public void testIOExSciptFile() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("Unable to read scripts file", BPException.ERROR_CODES.SCRIPTS_FILE_READ_ERROR));
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(dirUrl.getPath()));

		Mockito.doThrow(IOException.class).when(loader).openFileStream(Mockito.any());

		loader.loadScripts();
	}

	@Test
	public void testSciptFile() throws BPException, FileNotFoundException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(dirUrl.getPath()));

		List<Script> scripts = loader.loadScripts();

		Assert.assertEquals(1, scripts.size());

		Assert.assertEquals("sample.sh", scripts.get(0).getRepoPath());

		Assert.assertEquals("sample.sh", scripts.get(0).getTargetPath());

		Assert.assertEquals(PathType.FILE, scripts.get(0).getPathType());
	}

	@Test
	public void testNoDockerFile() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Unable to find docker image file", BPException.ERROR_CODES.NO_DOCKER_IMAGE_FILE_FUOND));
		URL dirUrl = this.getClass().getClassLoader().getResource("noDockerFile/");

		SourceCodeConventionFileLoader loader = new SourceCodeConventionFileLoader(dirUrl.getPath());

		loader.loadDockerImage();
	}

	@Test
	public void testIOExDockerFile() throws BPException, IOException {

		expectedException.expect(
				new BPExceptionMatcher("Unable to read docker image file", BPException.ERROR_CODES.DOCKER_IMAGE_READ_ERROR));
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(dirUrl.getPath()));

		Mockito.doThrow(IOException.class).when(loader).openFileStream(Mockito.any());

		loader.loadDockerImage();
	}

	@Test
	public void testDockerFile() throws BPException, FileNotFoundException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(dirUrl.getPath()));

		VLabDockerImage docker = loader.loadDockerImage();

		Assert.assertEquals("repository/alpine:3.5-sftp-ssl", docker.getImage());

		Assert.assertEquals("200", docker.getResources().getMemory_mb());

		Assert.assertEquals(1, docker.getContainer().getCommand().size());

		Assert.assertEquals("./sample.sh", docker.getContainer().getCommand().get(0));

		Assert.assertEquals(2, docker.getContainer().getEntryPoint().size());
		Assert.assertEquals("/bin/sh", docker.getContainer().getEntryPoint().get(0));
		Assert.assertEquals("-c", docker.getContainer().getEntryPoint().get(1));
	}

	@Test
	public void testNoIOFile() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Unable to find vlab I/O file", BPException.ERROR_CODES.IO_FILE_NOT_FOUND));
		URL dirUrl = this.getClass().getClassLoader().getResource("noIOFile/");

		SourceCodeConventionFileLoader loader = new SourceCodeConventionFileLoader(dirUrl.getPath());

		loader.loadIOFile();
	}

	@Test
	public void testIOExIOFile() throws BPException, IOException {

		expectedException.expect(new BPExceptionMatcher("Unable to read vlab I/O file", BPException.ERROR_CODES.IO_FILE_READ_ERROR));
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(dirUrl.getPath()));

		Mockito.doThrow(IOException.class).when(loader).openFileStream(Mockito.any());

		loader.loadIOFile();
	}

	@Test
	public void testIOFile() throws BPException, FileNotFoundException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(dirUrl.getPath()));

		BPIOObject iofile = loader.loadIOFile();

		Assert.assertEquals(1, iofile.getInputs().size());

		Assert.assertEquals(1, iofile.getOutputs().size());

		Assert.assertEquals("DataObject_1wb6a70", iofile.getInputs().get(0).getInput().getId());

		Assert.assertEquals("iDataObject_0loj2kk", iofile.getOutputs().get(0).getOutput().getId());
	}

	@Test
	public void testSciptFileDir() throws BPException, FileNotFoundException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelDirectoryScripts/");

		SourceCodeConventionFileLoader loader = Mockito.spy(new SourceCodeConventionFileLoader(dirUrl.getPath()));

		List<Script> scripts = loader.loadScripts();

		Assert.assertEquals(3, scripts.size());

		Assert.assertEquals("sample.sh", scripts.get(0).getRepoPath());
		Assert.assertEquals("sample.sh", scripts.get(0).getTargetPath());
		Assert.assertEquals(PathType.FILE, scripts.get(0).getPathType());

		Assert.assertEquals("sample1.sh", scripts.get(1).getRepoPath());
		Assert.assertEquals("sample1_t.sh", scripts.get(1).getTargetPath());
		Assert.assertEquals(PathType.FILE, scripts.get(1).getPathType());

		Assert.assertEquals("dir1", scripts.get(2).getRepoPath());
		Assert.assertEquals("dir1_t", scripts.get(2).getTargetPath());
		Assert.assertEquals(PathType.DIRECTORY, scripts.get(2).getPathType());

		List<File> files = loader.getScriptFiles(scripts.get(2));
		Assert.assertEquals(2, files.size());

		Assert.assertTrue(files.stream().anyMatch(file -> file.getName().contains("sample.sh")));
		Assert.assertTrue(files.stream().anyMatch(file -> file.getName().contains("second.sh")));

		Assert.assertFalse(files.stream().anyMatch(file -> file.getName().contains("pp.sh")));

	}
}