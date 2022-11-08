package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class URLIngestorTest {

	@Test
	public void test() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		assertTrue(new URLIngestor().canIngest(input));

	}

	@Test
	public void test3() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		Mockito.doReturn("url").when(i).getValueSchema();

		assertTrue(new URLIngestor().canIngest(input));

	}

	@Test
	public void test4() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		Mockito.doReturn("number_parameter").when(i).getValueSchema();

		assertFalse(new URLIngestor().canIngest(input));

	}

	@Test
	public void test5() {

		BPInputDescription input = Mockito.mock(BPInputDescription.class);
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn(i).when(input).getInput();
		Mockito.doReturn("unknown").when(i).getValueSchema();

		assertFalse(new URLIngestor().canIngest(input));

	}

	@Test
	public void test2() {

		BPInputDescription inputDescription = Mockito.mock(BPInputDescription.class);

		String source = "dafaultValue";

		execTest(new URLIngestor(), inputDescription, source);

	}

	private void execTest(URLIngestor urlIngestor, BPInputDescription defaultInput, String expectedValueTodownload) {
		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		String expected_dest = "expected_dest";

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPInputDescription defin = ((BPInputDescription) invocation.getArguments()[0]);

				return expected_dest;
			}

		}).when(parser).getDockerContainerAbsolutePath(Mockito.isA(BPInputDescription.class));

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String source = ((String) invocation.getArguments()[0]);

				if (source == null || "".equalsIgnoreCase(source))
					throw new Exception("Found null source to download.");

				String dest = ((String) invocation.getArguments()[1]);

				if (dest == null || "".equalsIgnoreCase(dest))
					throw new Exception("Found null dest to download.");

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(source.equalsIgnoreCase(expectedValueTodownload) && dest.equalsIgnoreCase(expected_dest));
				return res;
			}

		}).when(dockerHost).downloadFileTo(Mockito.any(), Mockito.any(), Mockito.any());

		assertTrue(urlIngestor.ingest(defaultInput, expectedValueTodownload, dockerHost, parser).isSuccess());

	}
}