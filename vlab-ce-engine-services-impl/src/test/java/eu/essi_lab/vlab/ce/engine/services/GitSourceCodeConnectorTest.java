package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class GitSourceCodeConnectorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testSupports() throws BPException, URISyntaxException {

		GitSourceCodeConnector adapter = Mockito.spy(new GitSourceCodeConnector());

		BPRealization realization = Mockito.mock(BPRealization.class);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		Mockito.doReturn("").when(realization).getRealizationURI();

		Mockito.doReturn(new File(dirUrl.getFile())).when(adapter).getDir();
		Mockito.doNothing().when(adapter).cloneRepository();

		assertTrue(adapter.supports(realization).isConventionsOk());

	}

	@Test
	public void testSupports2() throws BPException, URISyntaxException {

		GitSourceCodeConnector adapter = Mockito.spy(new GitSourceCodeConnector());

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn("").when(realization).getRealizationURI();

		Mockito.doThrow(BPException.class).when(adapter).cloneRepository();

		assertFalse(adapter.supports(realization).isConventionsOk());
		assertFalse(adapter.supports(realization).isCanRead());

	}

	@Test
	public void testSupports3() throws BPException, URISyntaxException, IOException {

		GitSourceCodeConnector adapter = Mockito.spy(new GitSourceCodeConnector());

		BPRealization realization = Mockito.mock(BPRealization.class);

		Mockito.doReturn("").when(realization).getRealizationURI();

		String targetDirectory = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + File.separator;
		File f = new File(targetDirectory + "file.txt");
		f.getParentFile().mkdirs();
		f.createNewFile();
		new ByteArrayInputStream("hello".getBytes()).transferTo(new FileOutputStream(new File(targetDirectory + "file.txt")));

		Mockito.doReturn(f.getParentFile()).when(adapter).getDir();

		Mockito.doNothing().when(adapter).cloneRepository();

		SupportResponse r = adapter.supports(realization);
		assertFalse(r.isConventionsOk());
		assertTrue(r.isCanRead());

	}

	private GitSourceCodeConnector initCOnnector(boolean direxists, String p, String git) {
		GitSourceCodeConnector connector = Mockito.spy(new GitSourceCodeConnector());

		File dir = Mockito.mock(File.class);

		Mockito.doReturn(direxists).when(dir).exists();

		Mockito.doReturn(p).when(dir).getAbsolutePath();

		Mockito.doReturn(dir).when(connector).getDir();

		Mockito.doNothing().when(connector).deleteFolder(Mockito.any());

		connector.setGitURL(git);
		return connector;
	}

	@Test
	public void testClone() throws BPException, GitAPIException {

		String path = "path";

		String git = "http://eexample.git.com";

		GitSourceCodeConnector connector = initCOnnector(true, path, git);

		CloneCommand cloneCommand = Mockito.mock(CloneCommand.class);

		Git g = Mockito.mock(Git.class);
		Mockito.doReturn(g).when(cloneCommand).call();

		Mockito.doReturn(cloneCommand).when(connector).initCloneCommand();

		connector.cloneRepository();

	}

	@Test
	public void testClone2() throws BPException, GitAPIException {

		String path = "path";

		String git = "http://eexample.git.com";

		GitSourceCodeConnector connector = initCOnnector(false, path, git);

		CloneCommand cloneCommand = Mockito.mock(CloneCommand.class);

		Git g = Mockito.mock(Git.class);
		Mockito.doReturn(g).when(cloneCommand).call();

		Mockito.doReturn(cloneCommand).when(connector).initCloneCommand();

		connector.cloneRepository();

	}

	@Test
	public void testClone3() throws BPException, GitAPIException {

		String path = "path";

		String git = "ssh://eexample.git.com";

		GitSourceCodeConnector connector = initCOnnector(false, path, git);

		CloneCommand cloneCommand = Mockito.mock(CloneCommand.class);

		Git g = Mockito.mock(Git.class);
		Mockito.doReturn(g).when(cloneCommand).call();

		Mockito.doReturn(cloneCommand).when(connector).initCloneCommand();

		connector.cloneRepository();

	}

	@Test
	public void testClone4() throws BPException, GitAPIException {

		expectedException.expect(new BPExceptionMatcher("Exception cloning from ssh://eexample.git.com [other message] ",
				BPException.ERROR_CODES.ERROR_GIT_CLONE));
		String path = "path";

		String git = "ssh://eexample.git.com";

		GitSourceCodeConnector connector = initCOnnector(false, path, git);

		CloneCommand cloneCommand = Mockito.mock(CloneCommand.class);

		GitAPIException gitex = Mockito.mock(GitAPIException.class);

		Mockito.doReturn("other message").when(gitex).getMessage();

		Mockito.doThrow(gitex).when(connector).executeCloneCommand(Mockito.any());

		Mockito.doReturn(cloneCommand).when(connector).initCloneCommand();

		connector.cloneRepository();

	}

	@Test
	public void testClone5() throws BPException, GitAPIException {

		expectedException.expect(new BPExceptionMatcher("Exception cloning from ssh://eexample.git.com [No space left on device] ",
				BPException.ERROR_CODES.ERROR_GIT_CLONE_NO_SPACE_LEFT));
		String path = "path";

		String git = "ssh://eexample.git.com";

		GitSourceCodeConnector connector = initCOnnector(false, path, git);

		CloneCommand cloneCommand = Mockito.mock(CloneCommand.class);

		GitAPIException gitex = Mockito.mock(GitAPIException.class);

		Mockito.doReturn("No space left on device").when(gitex).getMessage();

		Mockito.doThrow(gitex).when(connector).executeCloneCommand(Mockito.any());

		Mockito.doReturn(cloneCommand).when(connector).initCloneCommand();

		connector.cloneRepository();

	}
}