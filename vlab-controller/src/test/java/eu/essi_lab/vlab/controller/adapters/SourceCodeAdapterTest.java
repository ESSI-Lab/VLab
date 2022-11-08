package eu.essi_lab.vlab.controller.adapters;

import eu.essi_lab.vlab.controller.ISourceCodeConventionFileLoaderMock;
import eu.essi_lab.vlab.controller.executors.SourceCodeExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;

import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import java.io.File;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class SourceCodeAdapterTest {

	@Test
	public void testGetRequiredResources() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		SourceCodeAdapter adapter = Mockito.spy(new SourceCodeAdapter());

		ISourceCodeConnector codeConnector = Mockito.mock(ISourceCodeConnector.class);

		Mockito.doReturn(new File(dirUrl.getPath())).when(codeConnector).getDir();

		adapter.setConnector(codeConnector);

		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(adapter)
				.getSourceCodeConventionFileLoader();

		VLabDockerResources required = adapter.getRequiredResources();

		assertEquals("200", required.getMemory_mb());
	}

	@Test
	public void testAcquire() throws BPException {

		SourceCodeAdapter adapter = Mockito.spy(new SourceCodeAdapter());

		SourceCodeExecutor executor = Mockito.mock(SourceCodeExecutor.class);

		Mockito.doReturn(executor).when(adapter).getSourceCodeExecutor();

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(true).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(executor).acquireResources();

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		assertTrue(adapter.acquireResources(status).isAcquired());

		Mockito.verify(executor, Mockito.times(1)).setBPStatus(Mockito.any());

	}

	@Test
	public void testAcquire2() throws BPException {

		SourceCodeAdapter adapter = Mockito.spy(new SourceCodeAdapter());

		SourceCodeExecutor executor = Mockito.mock(SourceCodeExecutor.class);

		Mockito.doReturn(executor).when(adapter).getSourceCodeExecutor();

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(false).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(executor).acquireResources();

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		assertFalse(adapter.acquireResources(status).isAcquired());

		Mockito.verify(executor, Mockito.times(1)).setBPStatus(Mockito.any());

	}

	@Test
	public void testrelease() throws BPException {

		SourceCodeAdapter adapter = Mockito.spy(new SourceCodeAdapter());

		SourceCodeExecutor executor = Mockito.mock(SourceCodeExecutor.class);

		Mockito.doReturn(executor).when(adapter).getSourceCodeExecutor();

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(true).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(executor).acquireResources();

		adapter.releaseResources(reservationResult);

		Mockito.verify(executor, Mockito.times(1)).getDockerExecutor();
		Mockito.verify(executor, Mockito.times(1)).releaseReservation(Mockito.any(), Mockito.any());

	}

	@Test
	public void testrInitExecutor() throws BPException {

		SourceCodeAdapter adapter = Mockito.spy(new SourceCodeAdapter());
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		adapter.setConnector(connector);

		Mockito.doReturn(Mockito.mock(ISourceCodeConventionFileLoader.class)).when(adapter).getSourceCodeConventionFileLoader();

		IContainerOrchestratorManager manager = Mockito.mock(IContainerOrchestratorManager.class);

		Mockito.doReturn(manager).when(adapter).initIContainerOrchestratorManager();

		Mockito.doReturn(Mockito.mock(IContainerOrchestratorCommandExecutor.class)).when(manager).getExecutor();

		SourceCodeExecutor ex = adapter.getSourceCodeExecutor();

		assertNotNull(ex.getUploader());
		assertNotNull(ex.getDockerExecutorManager());
		assertNotNull(ex.getDockerExecutor());
		assertNotNull(ex.getConventionFileLoader());

	}

	@Test
	public void testExecute() throws InterruptedException, BPException {

		SourceCodeAdapter adapter = Mockito.spy(new SourceCodeAdapter());
		SourceCodeExecutor executor = Mockito.mock(SourceCodeExecutor.class);

		Mockito.doReturn(executor).when(adapter).getSourceCodeExecutor();

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Boolean b = (Boolean) invocation.getArguments()[0];

				if (!b)
					throw new Exception("Expected true delete on exit");

				return null;
			}
		}).when(executor).setDeleteSourceCodeDironExit(Mockito.anyBoolean());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Boolean b = (Boolean) invocation.getArguments()[0];

				if (!b)
					throw new Exception("Expected true delete container on exit");

				return null;
			}
		}).when(executor).setDeleteDockerContainerFolerOnExit(Mockito.anyBoolean());

		adapter.execute(status);

		Thread.sleep(1000L);

		Mockito.verify(executor, Mockito.times(1)).setDeleteSourceCodeDironExit(Mockito.anyBoolean());
		Mockito.verify(executor, Mockito.times(1)).setDeleteDockerContainerFolerOnExit(Mockito.anyBoolean());

		Mockito.verify(executor, Mockito.times(1)).setBPStatus(Mockito.any());
		Mockito.verify(executor, Mockito.times(1)).setInputs(Mockito.any());
		Mockito.verify(executor, Mockito.times(1)).call();
	}

	@Test
	public void testCleanResources() throws BPException {

		SourceCodeAdapter adapter = Mockito.spy(new SourceCodeAdapter());

		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		adapter.setConnector(connector);

		String runid = "runid";

		Mockito.doNothing().when(adapter).doCleanSourceExecutorResources(Mockito.any());

		adapter.cleanBPRunResources(runid);

		Mockito.verify(connector, Mockito.times(1)).deleteCodeFolder();
		Mockito.verify(adapter, Mockito.times(1)).doCleanSourceExecutorResources(Mockito.argThat(new ArgumentMatcher<String>() {
			@Override
			public boolean matches(String argument) {
				return runid.equalsIgnoreCase(argument);
			}
		}));

	}

}