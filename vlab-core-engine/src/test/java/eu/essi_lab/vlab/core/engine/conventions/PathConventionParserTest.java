package eu.essi_lab.vlab.core.engine.conventions;

import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class PathConventionParserTest {

	@Test
	public void test() {

		String vlabRoot = "/vlabroot";
		String runid = "runid-test";

		PathConventionParser parser = new PathConventionParser(runid, vlabRoot);

		BPInputDescription input = Mockito.mock(BPInputDescription.class);

		String relativePath = "path/relative/data.dat";

		Mockito.when(input.getTarget()).thenReturn(relativePath);

		String parsed = parser.getDockerContainerAbsolutePath(input);

		assertEquals(vlabRoot + "/" + runid + "/" + relativePath, parsed);

		Script script = Mockito.mock(Script.class);

		String targetPath = "path/to/script.py";

		Mockito.when(script.getTargetPath()).thenReturn(targetPath);

		String scriptParsed = parser.getDockerContainerScriptAbsolutePath(script);
		assertEquals(vlabRoot + "/" + runid + "/" + targetPath, scriptParsed);

	}

	@Test
	public void getOutputIndividualAbsolutePath() {

		String vlabRoot = "/vlabroot";

		String runid = "runid-test";

		PathConventionParser parser = new PathConventionParser(runid, vlabRoot);

		BPOutputDescription outout = Mockito.mock(BPOutputDescription.class);

		String relativePath = "path/relative/data.dat";

		Mockito.when(outout.getTarget()).thenReturn(relativePath);

		String parsed = parser.getDockerContainerAbsolutePath(outout);

		assertEquals(vlabRoot + "/" + runid + "/" + relativePath, parsed);

	}

	@Test
	public void getOutputArrayAbsolutePath() {

		String vlabRoot = "/vlabroot";

		String runid = "runid-test";

		PathConventionParser parser = new PathConventionParser(runid, vlabRoot);

		BPOutputDescription outout = Mockito.mock(BPOutputDescription.class);

		String relativePath = "path/relative/";

		Mockito.when(outout.getTarget()).thenReturn(relativePath);

		String parsed = parser.getDockerContainerAbsolutePath(outout);

		assertEquals(vlabRoot + "/" + runid + "/" + relativePath, parsed);

	}
}