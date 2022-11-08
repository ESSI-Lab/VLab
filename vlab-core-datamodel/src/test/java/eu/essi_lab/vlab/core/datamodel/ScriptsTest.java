package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class ScriptsTest {

	@Test
	public void testDeserialize() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test_script.json");

		assertNotNull(stream);

		Scripts scripts = new JSONDeserializer().deserialize(stream, Scripts.class);

		assertEquals(1, scripts.getScripts().size());

		PathType pathType = scripts.getScripts().get(0).getPathType();

		assertEquals(PathType.DIRECTORY, pathType);

		String repoPath = scripts.getScripts().get(0).getRepoPath();

		assertEquals("P1Scripts", repoPath);

	}
}