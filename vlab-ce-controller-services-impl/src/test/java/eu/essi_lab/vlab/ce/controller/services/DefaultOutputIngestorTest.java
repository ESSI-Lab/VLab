package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class DefaultOutputIngestorTest {
	@Test
	public void test0() throws BPException {

		BPOutputDescription o2 = new BPOutputDescription();
		o2.getOutput().setValueSchema("url");
		BPOutputDescription o3 = new BPOutputDescription();
		o3.getOutput().setValueSchema("notexists");
		BPOutputDescription o4 = new BPOutputDescription();
		o4.getOutput().setValueSchema("wms");
		assertTrue(new DefaultOutputIngestor().canIngest(new BPOutputDescription()));
		assertTrue(new DefaultOutputIngestor().canIngest(o2));
		assertFalse(new DefaultOutputIngestor().canIngest(o3));
		assertTrue(new DefaultOutputIngestor().canIngest(o4));

	}

	@Test
	public void test1() throws BPException {

		DefaultOutputIngestor ingestor = Mockito.spy(new DefaultOutputIngestor());

		String name = "oname";
		String o1id = "o1id";

		String runid = "runid";

		BPOutputDescription out = Mockito.mock(BPOutputDescription.class);
		BPOutput o = Mockito.mock(BPOutput.class);
		Mockito.doReturn(o).when(out).getOutput();
		Mockito.doReturn(o1id).when(o).getId();

		Mockito.doReturn(name).when(o).getName();

		Mockito.doReturn("individual").when(o).getOutputType();

		IContainerOrchestratorCommandExecutor executor = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		PathConventionParser parser = new PathConventionParser(runid, "/vlab");
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String o1 = (String) invocation.getArguments()[0];
				String o2 = (String) invocation.getArguments()[2];

				if (!o1.startsWith("/vlab/" + runid))
					throw new Exception("Bad path " + o1);

				if (!o2.equalsIgnoreCase(runid + "/" + o1id))
					throw new Exception("Bad key " + o2);

				return saved;
			}
		}).when(executor).saveFileToWebStorage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		IBPOutputWebStorage outputWebStorage = Mockito.mock(IBPOutputWebStorage.class);
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				BPOutput o1 = (BPOutput) invocation.getArguments()[0];
				String r = (String) invocation.getArguments()[1];

				if (!o1.getId().equalsIgnoreCase(o1id))
					throw new Exception("Bad id " + o1.getId());

				if (!r.equalsIgnoreCase(runid))
					throw new Exception("Bad runid " + r);
				return runid + "/" + o1id;
			}
		}).when(outputWebStorage).getKey(Mockito.any(), Mockito.any());

		Mockito.doReturn(outputWebStorage).when(ingestor).getBPOutputWebStorage(Mockito.any());

		ContainerOrchestratorCommandResult result = ingestor.ingest(out, runid, executor, parser);

		assertTrue(result.isSuccess());

		Mockito.verify(executor, Mockito.times(1)).saveFileToWebStorage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.verify(executor, Mockito.times(0)).saveFolderToWebStorage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	@Test
	public void test2() throws BPException {

		DefaultOutputIngestor ingestor = Mockito.spy(new DefaultOutputIngestor());

		String name = "oname";
		String o1id = "o1id";

		String runid = "runid";

		BPOutputDescription out = Mockito.mock(BPOutputDescription.class);
		BPOutput o = Mockito.mock(BPOutput.class);
		Mockito.doReturn(o).when(out).getOutput();
		Mockito.doReturn(o1id).when(o).getId();

		Mockito.doReturn(name).when(o).getName();

		Mockito.doReturn("individual").when(o).getOutputType();

		IContainerOrchestratorCommandExecutor executor = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		PathConventionParser parser = new PathConventionParser(runid, "/vlab");
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String o1 = (String) invocation.getArguments()[0];
				String o2 = (String) invocation.getArguments()[2];

				if (!o1.startsWith("/vlab/" + runid))
					throw new Exception("Bad path " + o1);

				if (!o2.equalsIgnoreCase(runid + "/" + o1id))
					throw new Exception("Bad key " + o2);

				return saved;
			}
		}).when(executor).saveFolderToWebStorage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		IBPOutputWebStorage outputWebStorage = Mockito.mock(IBPOutputWebStorage.class);
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				BPOutput o1 = (BPOutput) invocation.getArguments()[0];
				String r = (String) invocation.getArguments()[1];

				if (!o1.getId().equalsIgnoreCase(o1id))
					throw new Exception("Bad id " + o1.getId());

				if (!r.equalsIgnoreCase(runid))
					throw new Exception("Bad runid " + r);
				return runid + "/" + o1id;
			}
		}).when(outputWebStorage).getKey(Mockito.any(), Mockito.any());

		Mockito.doReturn(outputWebStorage).when(ingestor).getBPOutputWebStorage(Mockito.any());

		ContainerOrchestratorCommandResult result = ingestor.ingestArray(out, runid, executor, parser);

		assertTrue(result.isSuccess());

		Mockito.verify(executor, Mockito.times(1)).saveFolderToWebStorage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.verify(executor, Mockito.times(0)).saveFileToWebStorage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}
}