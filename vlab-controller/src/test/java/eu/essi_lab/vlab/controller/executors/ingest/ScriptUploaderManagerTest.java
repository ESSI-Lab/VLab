package eu.essi_lab.vlab.controller.executors.ingest;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.PathType;
import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class ScriptUploaderManagerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	/**
	 * This tests the upload of a single script file
	 */
	@Test
	public void testScriptFile() throws IOException, BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ScriptUploaderManager manager = Mockito.spy(new ScriptUploaderManager());

		Script script = Mockito.mock(Script.class);

		String scriptFile = "scriptFile";

		String scriptTargetPath = "folder/scriptFile";
		Mockito.when(script.getPathType()).thenReturn(PathType.FILE);
		Mockito.when(script.getTargetPath()).thenReturn(scriptTargetPath);

		Mockito.when(script.getRepoPath()).thenReturn(scriptFile);

		execTest(manager, script, Arrays.asList(new String[] { scriptFile }), Arrays.asList(new String[] { scriptTargetPath }));

	}

	/**
	 * This tests the upload of a single script file, failing due to file not found
	 */
	@Test
	public void testScriptFileNotFound() throws FileNotFoundException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ScriptUploaderManager manager = new ScriptUploaderManager();

		Script script = Mockito.mock(Script.class);

		String scriptFile = "scriptFile";

		String scriptTargetPath = "folder/scriptFile";
		Mockito.when(script.getPathType()).thenReturn(PathType.FILE);
		Mockito.when(script.getTargetPath()).thenReturn(scriptTargetPath);

		Mockito.when(script.getRepoPath()).thenReturn(scriptFile);

		execTestFileNotFound(manager, script, Arrays.asList(new String[] { scriptFile }), Arrays.asList(new String[] { scriptTargetPath }));

	}

	/**
	 * This tests the upload of a single script file, failing due to an error by docker host
	 */
	@Test
	public void testScriptFileDockerFailure() throws FileNotFoundException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ScriptUploaderManager manager = new ScriptUploaderManager();

		Script script = Mockito.mock(Script.class);

		String scriptFile = "scriptFile";

		String scriptTargetPath = "folder/scriptFile";
		Mockito.when(script.getPathType()).thenReturn(PathType.FILE);
		Mockito.when(script.getTargetPath()).thenReturn(scriptTargetPath);

		Mockito.when(script.getRepoPath()).thenReturn(scriptFile);

		execTestDockerHostFailure(manager, script, Arrays.asList(new String[] { scriptFile }),
				Arrays.asList(new String[] { scriptTargetPath }));

	}

	private void execTestDockerHostFailure(ScriptUploaderManager manager, Script script, List<String> expectedFilesTocopy,
			List<String> expectedDestinations) throws FileNotFoundException {

		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		List<String> expectedValueTocopy = copyList(expectedFilesTocopy);

		List<String> expectedDestinationsCopy = copyList(expectedDestinations);

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Script defin = ((Script) invocation.getArguments()[0]);

				return defin.getTargetPath();
			}

		}).when(parser).getDockerContainerScriptAbsolutePath(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(false);
				return res;

			}

		}).when(dockerHost).copyFileTo(Mockito.any(), Mockito.any(), Mockito.any());

		ISourceCodeConventionFileLoader loader = Mockito.mock(ISourceCodeConventionFileLoader.class);

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Script inscript = ((Script) invocation.getArguments()[0]);

				if (inscript == null)
					throw new Exception("Found null destination script file.");

				List<File> list = new ArrayList<>();

				for (String s : expectedFilesTocopy)
					list.add(new File(s));

				return list;

			}

		}).when(loader).getScriptFiles(Mockito.any());

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);
		manager.setLoader(loader);

		ContainerOrchestratorCommandResult result = manager.ingest(script);

		assertFalse("Expected false result in response to docker host failure", result.isSuccess());

	}

	private void execTestFileNotFound(ScriptUploaderManager manager, Script script, List<String> expectedFilesTocopy,
			List<String> expectedDestinations) throws FileNotFoundException {

		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		ISourceCodeConventionFileLoader loader = Mockito.mock(ISourceCodeConventionFileLoader.class);

		Mockito.doThrow(FileNotFoundException.class).when(loader).getScriptFiles(Mockito.any());

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);
		manager.setLoader(loader);

		ContainerOrchestratorCommandResult result = manager.ingest(script);

		assertFalse("Expected false result in response to FileNotFound", result.isSuccess());

	}

	/**
	 * This tests the upload of a directory script file
	 */
	@Test
	public void testScriptDir() throws IOException, BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ScriptUploaderManager manager = Mockito.spy(new ScriptUploaderManager());

		Script script = Mockito.mock(Script.class);

		String scriptFile = "scriptFile";

		Mockito.when(script.getRepoPath()).thenReturn(scriptFile);

		String scriptTargetPath = "scriptTargetPath";
		Mockito.when(script.getTargetPath()).thenReturn(scriptTargetPath);

		Mockito.when(script.getPathType()).thenReturn(PathType.DIRECTORY);

		List<String> subfiles = new ArrayList<>();

		subfiles.add("sub1");
		subfiles.add("sub2.py");

		List<String> destinations = new ArrayList<>();

		destinations.add(scriptTargetPath + "/sub1");
		destinations.add(scriptTargetPath + "/sub2.py");

		execTest(manager, script, subfiles, destinations);

	}

	private String pathToString(Path path) {
		return path.toString();
	}

	private void execTest(ScriptUploaderManager manager, Script script, List<String> expectedFilesTocopy, List<String> expectedDestinations)
			throws IOException, BPException {

		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		List<String> expectedValueTocopy = copyList(expectedFilesTocopy);

		List<String> expectedDestinationsCopy = copyList(expectedDestinations);

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Script defin = ((Script) invocation.getArguments()[0]);

				return defin.getTargetPath();
			}

		}).when(parser).getDockerContainerScriptAbsolutePath(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Path sourcePath = ((Path) invocation.getArguments()[0]);

				Path targetPath = ((Path) invocation.getArguments()[1]);

				if (targetPath == null)
					throw new Exception("Found null destination script file.");

				boolean found = false;

				for (String expected : expectedFilesTocopy) {
					if (expected.equalsIgnoreCase(pathToString(sourcePath)))
						found = true;
				}

				System.out.println(pathToString(sourcePath) + " was " + (found ? "" : "not ") + "found");

				if (found)
					expectedValueTocopy.remove(pathToString(sourcePath));

				boolean foundDest = false;

				for (String expected : expectedDestinationsCopy) {
					if (expected.equalsIgnoreCase(pathToString(targetPath)))
						foundDest = true;
				}

				System.out.println(pathToString(targetPath) + " was " + (foundDest ? "" : "not ") + "found");

				if (foundDest)
					expectedDestinationsCopy.remove(pathToString(targetPath));

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(found && foundDest);
				return res;

			}

		}).when(manager).doCopyFile(Mockito.any(), Mockito.any());

		ISourceCodeConventionFileLoader loader = Mockito.mock(ISourceCodeConventionFileLoader.class);

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Script inscript = ((Script) invocation.getArguments()[0]);

				if (inscript == null)
					throw new Exception("Found null destination script file.");

				List<File> list = new ArrayList<>();

				for (String s : expectedFilesTocopy)
					list.add(new File(s));

				return list;

			}

		}).when(loader).getScriptFiles(Mockito.any());

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);
		manager.setLoader(loader);

		ContainerOrchestratorCommandResult result = manager.ingest(script);

		assertTrue(result.isSuccess());
		assertTrue(expectedValueTocopy.isEmpty());
		assertTrue(expectedDestinationsCopy.isEmpty());

	}

	private List<String> copyList(List<String> expectedValuesTodownload) {
		List<String> ret = new ArrayList<>();

		for (String s : expectedValuesTodownload)
			ret.add(s);

		return ret;
	}
}