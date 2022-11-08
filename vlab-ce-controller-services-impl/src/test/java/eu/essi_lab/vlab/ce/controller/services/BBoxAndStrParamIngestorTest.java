package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BBoxAndStrParamIngestorTest {

	@Test
	public void test() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		assertFalse(new BBoxAndStrParamIngestor().canIngest(input));

	}

	@Test
	public void test3() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		Mockito.doReturn("url").when(i).getValueSchema();

		assertFalse(new BBoxAndStrParamIngestor().canIngest(input));

	}

	@Test
	public void test4() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		Mockito.doReturn("string_parameter").when(i).getValueSchema();

		assertTrue(new BBoxAndStrParamIngestor().canIngest(input));

	}

	@Test
	public void test5() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		Mockito.doReturn("bbox").when(i).getValueSchema();

		assertTrue(new BBoxAndStrParamIngestor().canIngest(input));

	}

	@Test
	public void test2() throws BPException {

		BPInputDescription inputDescription = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(inputDescription).getInput();
		Mockito.doReturn("id").when(i).getId();

		String source = "value";

		execTest(new BBoxAndStrParamIngestor(), inputDescription, source);

	}

	private void execTest(BBoxAndStrParamIngestor ingestor, BPInputDescription input, String expectedValueTodownload) throws BPException {
		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		String absPath = "/absPath/vlabparams.json";

		Mockito.doReturn(absPath).when(parser).getDockerContainerAbsolutePathParameterFile();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String key = ((String) invocation.getArguments()[0]);

				if (key == null || "".equalsIgnoreCase(key))
					throw new Exception("Found null key to append.");

				String value = (String) invocation.getArguments()[1];

				if (value == null || "".equalsIgnoreCase(value.toString()))
					throw new Exception("Found null value to append.");

				String filePath = ((String) invocation.getArguments()[2]);

				if (filePath == null || "".equalsIgnoreCase(filePath))
					throw new Exception("Found null filePath to append.");

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(input.getInput().getId().equals(key) && filePath.equals(absPath) && value.equalsIgnoreCase(expectedValueTodownload));

				return res;
			}

		}).when(dockerHost).appendParamTo(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyLong());//String key, Number value, String filePath, Long maxwait

		assertTrue(ingestor.ingest(input, expectedValueTodownload, dockerHost, parser).isSuccess());

	}

}