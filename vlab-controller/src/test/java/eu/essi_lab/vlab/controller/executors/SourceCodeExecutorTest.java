package eu.essi_lab.vlab.controller.executors;

import eu.essi_lab.vlab.controller.BPExceptionMatcher;
import eu.essi_lab.vlab.controller.ISourceCodeConventionFileLoaderMock;
import eu.essi_lab.vlab.controller.executors.ingest.ArrayInputManager;
import eu.essi_lab.vlab.controller.executors.ingest.IndividualInputManager;
import eu.essi_lab.vlab.controller.executors.ingest.ScriptUploaderManager;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.BPRunResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
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
public class SourceCodeExecutorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testSaveOutputs() throws BPException {

		//		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		File file = Mockito.mock(File.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		Mockito.doNothing().when(dockerEx).release(Mockito.any());
		executor.setDockerExecutor(dockerEx);

		List<BPOutputDescription> outputsToSave = new ArrayList<>();

		BPOutputDescription out1 = Mockito.mock(BPOutputDescription.class);
		String o1id = "o1id";
		BPOutput o1 = Mockito.mock(BPOutput.class);
		Mockito.doReturn(o1id).when(o1).getId();
		Mockito.doReturn(o1).when(out1).getOutput();
		outputsToSave.add(out1);

		BPOutputDescription out2 = Mockito.mock(BPOutputDescription.class);
		String o2id = "o2id";
		BPOutput o2 = Mockito.mock(BPOutput.class);
		Mockito.doReturn(o2id).when(o2).getId();
		Mockito.doReturn(o2).when(out2).getOutput();
		outputsToSave.add(out2);

		Mockito.doReturn(outputsToSave).when(executor).readOutputs();

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				BPOutputDescription o = (BPOutputDescription) invocation.getArguments()[0];

				if (!o.getOutput().getId().equalsIgnoreCase(o1id) && !o.getOutput().getId().equalsIgnoreCase(o2id))
					throw new Exception("Bad output id " + o.getOutput().getId());

				return saved;
			}
		}).when(executor).saveOutput(Mockito.any());

		executor.saveOputputs();

		Mockito.verify(executor, Mockito.times(2)).saveOutput(Mockito.any());

	}

	@Test
	public void testSaveOutputs2() throws BPException {

		String name = "oname";
		String o1id = "o1id";

		expectedException.expect(
				new BPExceptionMatcher("Can't save output " + name + " [" + o1id + "]", BPException.ERROR_CODES.SAVE_OUTPUT_ERROR));

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		Mockito.doNothing().when(dockerEx).release(Mockito.any());
		executor.setDockerExecutor(dockerEx);

		List<BPOutputDescription> outputsToSave = new ArrayList<>();

		BPOutputDescription out1 = Mockito.mock(BPOutputDescription.class);

		BPOutput o1 = Mockito.mock(BPOutput.class);
		Mockito.doReturn(o1id).when(o1).getId();
		Mockito.doReturn(name).when(o1).getName();
		Mockito.doReturn(o1).when(out1).getOutput();

		outputsToSave.add(out1);

		Mockito.doReturn(outputsToSave).when(executor).readOutputs();

		ContainerOrchestratorCommandResult failed = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(failed).isSuccess();
		Mockito.doReturn("error").when(failed).getMessage();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				BPOutputDescription o = (BPOutputDescription) invocation.getArguments()[0];

				if (!o.getOutput().getId().equalsIgnoreCase(o1id))
					throw new Exception("Bad output id " + o.getOutput().getId());

				return failed;
			}
		}).when(executor).saveOutput(Mockito.any());

		executor.saveOputputs();

	}

	@Test
	public void testSaveOutput() throws BPException {

		String name = "oname";
		String o1id = "o1id";

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		BPOutputDescription out1 = Mockito.mock(BPOutputDescription.class);

		BPOutput o1 = Mockito.mock(BPOutput.class);
		Mockito.doReturn(o1id).when(o1).getId();
		Mockito.doReturn(name).when(o1).getName();
		Mockito.doReturn(o1).when(out1).getOutput();

		Mockito.doReturn("individual").when(o1).getOutputType();

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		IndividualInputManager im = Mockito.spy(new IndividualInputManager(runid));
		Mockito.doReturn(im).when(executor).getInputManager();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPOutputDescription o = (BPOutputDescription) invocation.getArguments()[0];

				if (!o1id.equalsIgnoreCase(o.getOutput().getId()))
					throw new Exception("Bad output id");

				if (!name.equalsIgnoreCase(o.getOutput().getName()))
					throw new Exception("Bad output name");

				return saved;
			}

		}).when(im).save(Mockito.any());

		ContainerOrchestratorCommandResult result = executor.saveOutput(out1);

		assertTrue(result.isSuccess());

		Mockito.verify(im, Mockito.times(1)).setDockerHost(Mockito.any());
		Mockito.verify(im, Mockito.times(1)).setPathParser(Mockito.any());

	}

	@Test
	public void testSaveOutput2() throws BPException {

		String name = "oname";
		String o1id = "o1id";

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		BPOutputDescription out1 = Mockito.mock(BPOutputDescription.class);

		BPOutput o1 = Mockito.mock(BPOutput.class);
		Mockito.doReturn(o1id).when(o1).getId();
		Mockito.doReturn(name).when(o1).getName();
		Mockito.doReturn(o1).when(out1).getOutput();

		Mockito.doReturn("array").when(o1).getOutputType();

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		ArrayInputManager im = Mockito.spy(new ArrayInputManager(runid));
		Mockito.doReturn(im).when(executor).getArrayManager();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPOutputDescription o = (BPOutputDescription) invocation.getArguments()[0];

				if (!o1id.equalsIgnoreCase(o.getOutput().getId()))
					throw new Exception("Bad output id");

				if (!name.equalsIgnoreCase(o.getOutput().getName()))
					throw new Exception("Bad output name");

				return saved;
			}

		}).when(im).save(Mockito.any());

		ContainerOrchestratorCommandResult result = executor.saveOutput(out1);

		assertTrue(result.isSuccess());

	}

	@Test
	public void testAcquire() throws BPException {

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid("runid");
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);

		Mockito.doReturn(true).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(dockerEx).reserveResources(Mockito.any());

		boolean result = executor.doAcquireResources(resources, dockerEx).isAcquired();

		assertTrue(result);

	}

	@Test
	public void testAcquire2() throws BPException {

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid("runid");
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);

		Mockito.doReturn(false).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(dockerEx).reserveResources(Mockito.any());

		boolean result = executor.doAcquireResources(resources, dockerEx).isAcquired();

		assertFalse(result);

	}

	@Test
	public void testAcquire3() throws BPException {

		expectedException.expect(new BPExceptionMatcher("o resources specified in", BPException.ERROR_CODES.NO_REQUIRED_RESOURCES_FOUND));

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid("runid");
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.doAcquireResources(null, dockerEx);

	}

	@Test
	public void testAcquire4() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(true).when(reservationResult).isAcquired();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerResources o1 = (VLabDockerResources) invocation.getArguments()[0];

				if (!o1.getMemory_mb().equalsIgnoreCase("200"))
					throw new Exception("Bad required mem " + o1.getMemory_mb());

				return reservationResult;
			}
		}).when(executor).doAcquireResources(Mockito.any(), Mockito.any());

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		assertTrue(executor.acquireResources().isAcquired());

	}

	@Test
	public void testAcquire5() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(false).when(reservationResult).isAcquired();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerResources o1 = (VLabDockerResources) invocation.getArguments()[0];

				if (!o1.getMemory_mb().equalsIgnoreCase("200"))
					throw new Exception("Bad required mem " + o1.getMemory_mb());

				return reservationResult;
			}
		}).when(executor).doAcquireResources(Mockito.any(), Mockito.any());

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		assertFalse(executor.acquireResources().isAcquired());

	}

	@Test
	public void testAcquire6() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Resource acquisition failed due to Error loading docker image description", null));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.acquireResources();

	}

	@Test
	public void testreadoutputs() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();

		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		List<BPOutputDescription> outs = executor.readOutputs();

		assertEquals("iDataObject_0loj2kk", outs.get(0).getOutput().getId());
		assertEquals("Output Name", outs.get(0).getOutput().getName());
		assertEquals("output.txt", outs.get(0).getTarget());

	}

	@Test
	public void testexec() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Error loading docker image description", null));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.execute();

	}

	@Test
	public void testexec2() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		ContainerOrchestratorCommandResult ok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(ok).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerImage o1 = (VLabDockerImage) invocation.getArguments()[0];

				if (!o1.getImage().equalsIgnoreCase("repository/alpine:3.5-sftp-ssl"))
					throw new Exception("Bad image " + o1.getImage());

				return ok;
			}
		}).when(dockerEx).runImage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.execute();

	}

	@Test
	public void testexec3() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Error executing model: ", BPException.ERROR_CODES.MODEL_EXECUTION_ERROR));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		ContainerOrchestratorCommandResult ok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(ok).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerImage o1 = (VLabDockerImage) invocation.getArguments()[0];

				if (!o1.getImage().equalsIgnoreCase("repository/alpine:3.5-sftp-ssl"))
					throw new Exception("Bad image " + o1.getImage());

				return ok;
			}
		}).when(dockerEx).runImage(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.execute();

	}

	@Test
	public void test() throws BPException {

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		Mockito.doNothing().when(dockerEx).release(Mockito.any());
		executor.setDockerExecutor(dockerEx);

		IContainerOrchestratorManager dockerExManager = Mockito.mock(IContainerOrchestratorManager.class);

		Mockito.doReturn(dockerExManager).when(executor).getDockerExecutorManager();

		Mockito.doNothing().when(executor).copyInputs();
		Mockito.doNothing().when(executor).ingestInputs();
		Mockito.doNothing().when(executor).ingestScripts();
		Mockito.doNothing().when(executor).prepareOutputFolder();
		Mockito.doNothing().when(executor).execute();
		Mockito.doNothing().when(executor).saveOputputs();
		Mockito.doNothing().when(connector).deleteCodeFolder();

		BPRunStatus status = executor.call();

		assertEquals(BPRunStatuses.COMPLETED.toString(), status.getStatus());
		assertEquals(BPRunResult.SUCCESS.toString(), status.getResult());

		Mockito.verify(initialStatus, Mockito.times(5)).setStatus(Mockito.anyString());
		Mockito.verify(executor, Mockito.times(1)).copyInputs();

		Mockito.verify(executor, Mockito.times(1)).ingestInputs();
		Mockito.verify(executor, Mockito.times(1)).ingestScripts();
		Mockito.verify(executor, Mockito.times(1)).prepareOutputFolder();
		Mockito.verify(executor, Mockito.times(1)).execute();
		Mockito.verify(executor, Mockito.times(1)).saveOputputs();
		Mockito.verify(executor, Mockito.times(1)).cleanSourceCodeFolder();
		Mockito.verify(connector, Mockito.times(1)).deleteCodeFolder();
		Mockito.verify(executor, Mockito.times(1)).releaseReservation(Mockito.any(), Mockito.any());

	}

	@Test
	public void test2() throws BPException {

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		Mockito.doNothing().when(dockerEx).release(Mockito.any());
		executor.setDockerExecutor(dockerEx);

		IContainerOrchestratorManager dockerExManager = Mockito.mock(IContainerOrchestratorManager.class);
		Mockito.doReturn(dockerExManager).when(executor).getDockerExecutorManager();

		Mockito.doNothing().when(executor).copyInputs();
		Mockito.doNothing().when(executor).ingestInputs();
		Mockito.doNothing().when(executor).ingestScripts();
		Mockito.doNothing().when(executor).prepareOutputFolder();
		Mockito.doNothing().when(executor).execute();
		Mockito.doThrow(BPException.class).when(executor).saveOputputs();
		Mockito.doNothing().when(connector).deleteCodeFolder();
		Mockito.doNothing().when(executor).releaseReservation(Mockito.any(), Mockito.any());

		BPRunStatus status = executor.call();

		assertEquals(BPRunStatuses.COMPLETED.toString(), status.getStatus());
		assertEquals(BPRunResult.FAIL.toString(), status.getResult());

		Mockito.verify(initialStatus, Mockito.times(5)).setStatus(Mockito.anyString());
		Mockito.verify(executor, Mockito.times(1)).copyInputs();

		Mockito.verify(executor, Mockito.times(1)).ingestInputs();
		Mockito.verify(executor, Mockito.times(1)).ingestScripts();
		Mockito.verify(executor, Mockito.times(1)).prepareOutputFolder();
		Mockito.verify(executor, Mockito.times(1)).execute();
		Mockito.verify(executor, Mockito.times(1)).saveOputputs();
		Mockito.verify(executor, Mockito.times(1)).cleanSourceCodeFolder();
		Mockito.verify(connector, Mockito.times(1)).deleteCodeFolder();
		Mockito.verify(executor, Mockito.times(1)).releaseReservation(Mockito.any(), Mockito.any());

	}

	@Test
	public void testException() throws Exception {

		File directory = Mockito.mock(File.class);

		Object dirName = "testDirName";

		Mockito.doReturn(dirName).when(directory).getName();

		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);

		Mockito.doReturn(directory).when(connector).getDir();

		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setUploader(new ScriptUploaderManager());

		executor.setDeleteSourceCodeDironExit(true);

		IContainerOrchestratorManager manager = Mockito.mock(IContainerOrchestratorManager.class);
		executor.setDockerExecutorManager(manager);

		BPRunStatus status = new BPRunStatus();

		String runid = "runiod";
		status.setRunid(runid);

		executor.setBPStatus(status);

		Mockito.doThrow(BPException.class).when(executor).ingestInputs();

		executor.call();

		assertEquals(BPRunStatuses.COMPLETED.toString(), status.getStatus());

		assertEquals(BPRunResult.FAIL.toString(), status.getResult());

		Mockito.verify(manager, Mockito.times(1)).cleanResources();
		Mockito.verify(executor, Mockito.times(1)).cleanSourceCodeFolder();
		Mockito.verify(connector, Mockito.times(1)).deleteCodeFolder();
	}

	@Test
	public void copyScripts() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		String runid = "tstrunid";

		ISourceCodeConnector codeConnector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(new File(dirUrl.getFile())).when(codeConnector).getDir();

		SourceCodeExecutor executor = new SourceCodeExecutor(codeConnector);
		executor.setUploader(new ScriptUploaderManager());

		executor.setTestRoot("/tmp");

		BPRunStatus status = new BPRunStatus();

		status.setRunid(runid);

		executor.setBPStatus(status);

		IContainerOrchestratorCommandExecutor mockEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		ContainerOrchestratorCommandResult okRes = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(okRes).isSuccess();

		Mockito.doReturn(okRes).when(mockEx).copyFileTo(Mockito.any(), Mockito.any(), Mockito.any());

		executor.setDockerExecutor(mockEx);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(codeConnector.getDir().getAbsolutePath()));

		executor.ingestScripts();

		Mockito.verify(mockEx, Mockito.times(1)).copyFileTo(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void copyScripts2() throws BPException, IOException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelMultiScripts/");

		String runid = "tstrunidmulti";

		ISourceCodeConnector codeConnector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(new File(dirUrl.getFile())).when(codeConnector).getDir();

		SourceCodeExecutor executor = new SourceCodeExecutor(codeConnector);
		executor.setUploader(new ScriptUploaderManager());

		IContainerOrchestratorCommandExecutor mockEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		ContainerOrchestratorCommandResult okRes = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(okRes).isSuccess();

		Mockito.doReturn(okRes).when(mockEx).copyFileTo(Mockito.any(), Mockito.any(), Mockito.any());

		executor.setDockerExecutor(mockEx);

		executor.setTestRoot("/tmp");

		emptyFolder(new File("/tmp/" + runid));

		BPRunStatus status = new BPRunStatus();

		status.setRunid(runid);

		executor.setBPStatus(status);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(codeConnector.getDir().getAbsolutePath()));
		executor.ingestScripts();

		Mockito.verify(mockEx, Mockito.times(5)).copyFileTo(Mockito.any(), Mockito.any(), Mockito.any());

	}

	private void emptyFolder(File dir) {

		File[] files = dir.listFiles();

		if (files == null)
			return;

		for (File file : files) {
			if (file.isDirectory())
				emptyFolder(file);

			file.delete();
		}

		dir.delete();

	}

	@Test
	public void testreadoutputs2() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Error reading outputs description", null));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.readOutputs();

	}

	@Test
	public void testingestinputs() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Error reading default inputs", null));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.ingestInputs();

	}

	@Test
	public void testingestinputs2() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		IndividualInputManager individualInputManager = Mockito.mock(IndividualInputManager.class);

		Mockito.doReturn(individualInputManager).when(executor).getInputManager();
		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);
		Mockito.doReturn(true).when(result).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPInputDescription inputDescription = (BPInputDescription) invocation.getArguments()[0];

				if (!inputDescription.getInput().getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inputDescription.getInput().getId());

				BPInput inp = (BPInput) invocation.getArguments()[1];

				if (!inputDescription.getInput().getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inputDescription.getInput().getId());

				if (!inp.getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inp.getId());

				return result;
			}
		}).when(individualInputManager).ingest(Mockito.any(), (BPInput) Mockito.any());

		List<BPInput> inputs = new ArrayList<>();
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn("DataObject_1wb6a70").when(i).getId();

		inputs.add(i);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.setInputs(inputs);
		executor.copyInputs();
		executor.ingestInputs();

		Mockito.verify(individualInputManager, Mockito.times(1)).setDockerHost(Mockito.any());
		Mockito.verify(individualInputManager, Mockito.times(1)).setPathParser(Mockito.any());
		Mockito.verify(individualInputManager, Mockito.times(1)).ingest(Mockito.any(), (BPInput) Mockito.any());

	}

	@Test
	public void testingestinputs3() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Can't ingest input Input name [DataObject_1wb6a70]",
				BPException.ERROR_CODES.ERROR_INGESTING_INPUT));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		IndividualInputManager individualInputManager = Mockito.mock(IndividualInputManager.class);

		Mockito.doReturn(individualInputManager).when(executor).getInputManager();
		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);
		Mockito.doReturn(false).when(result).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPInputDescription inputDescription = (BPInputDescription) invocation.getArguments()[0];

				if (!inputDescription.getInput().getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inputDescription.getInput().getId());

				BPInput inp = (BPInput) invocation.getArguments()[1];

				if (!inputDescription.getInput().getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inputDescription.getInput().getId());

				if (!inp.getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inp.getId());

				return result;
			}
		}).when(individualInputManager).ingest(Mockito.any(), (BPInput) Mockito.any());

		List<BPInput> inputs = new ArrayList<>();
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn("DataObject_1wb6a70").when(i).getId();

		inputs.add(i);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.setInputs(inputs);
		executor.copyInputs();
		executor.ingestInputs();

	}

	@Test
	public void testprepareOutputFolder() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		IndividualInputManager individualInputManager = Mockito.mock(IndividualInputManager.class);

		Mockito.doReturn(individualInputManager).when(executor).getInputManager();
		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);
		Mockito.doReturn(true).when(result).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPOutputDescription inputDescription = (BPOutputDescription) invocation.getArguments()[0];

				if (!inputDescription.getOutput().getId().equalsIgnoreCase("iDataObject_0loj2kk"))
					throw new Exception("Bad id " + inputDescription.getOutput().getId());

				return result;
			}
		}).when(individualInputManager).createFolder(Mockito.any());

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.prepareOutputFolder();

		Mockito.verify(individualInputManager, Mockito.times(1)).setDockerHost(Mockito.any());
		Mockito.verify(individualInputManager, Mockito.times(1)).setPathParser(Mockito.any());
		Mockito.verify(individualInputManager, Mockito.times(1)).createFolder(Mockito.any());

	}

	@Test
	public void testprepareOutputFolder2() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelOutputFolderCreate/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		IndividualInputManager individualInputManager = Mockito.mock(IndividualInputManager.class);

		Mockito.doReturn(individualInputManager).when(executor).getInputManager();

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));
		executor.prepareOutputFolder();

		Mockito.verify(individualInputManager, Mockito.never()).setDockerHost(Mockito.any());
		Mockito.verify(individualInputManager, Mockito.never()).setPathParser(Mockito.any());
		Mockito.verify(individualInputManager, Mockito.never()).createFolder(Mockito.any());

	}

	@Test
	public void testprepareOutputFolder3() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Can't create folder for output ", BPException.ERROR_CODES.CREATE_OUTPUT_FOLDER_ERROR));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		IndividualInputManager individualInputManager = Mockito.mock(IndividualInputManager.class);

		Mockito.doReturn(individualInputManager).when(executor).getInputManager();
		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);
		Mockito.doReturn(false).when(result).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPOutputDescription inputDescription = (BPOutputDescription) invocation.getArguments()[0];

				if (!inputDescription.getOutput().getId().equalsIgnoreCase("iDataObject_0loj2kk"))
					throw new Exception("Bad id " + inputDescription.getOutput().getId());

				return result;
			}
		}).when(individualInputManager).createFolder(Mockito.any());

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));
		executor.prepareOutputFolder();

	}

	@Test
	public void testingestinputsArray() throws BPException {

		String runid = "runid";

		File file = Mockito.mock(File.class);

		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);

		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelOutputFolderCreate/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		ArrayInputManager arrayInputManager = Mockito.mock(ArrayInputManager.class);

		Mockito.doReturn(arrayInputManager).when(executor).getArrayManager();
		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);
		Mockito.doReturn(true).when(result).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPInputDescription inputDescription = (BPInputDescription) invocation.getArguments()[0];

				if (!inputDescription.getInput().getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inputDescription.getInput().getId());

				BPInput inp = (BPInput) invocation.getArguments()[1];

				if (!inputDescription.getInput().getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inputDescription.getInput().getId());

				if (!inp.getId().equalsIgnoreCase("DataObject_1wb6a70"))
					throw new Exception("Bad id " + inp.getId());

				return result;
			}
		}).when(arrayInputManager).ingest(Mockito.any(), (BPInput) Mockito.any());

		List<BPInput> inputs = new ArrayList<>();
		BPInput i = Mockito.mock(BPInput.class);
		Mockito.doReturn("DataObject_1wb6a70").when(i).getId();

		inputs.add(i);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.setInputs(inputs);
		executor.copyInputs();
		executor.ingestInputs();

		Mockito.verify(arrayInputManager, Mockito.times(1)).setDockerHost(Mockito.any());
		Mockito.verify(arrayInputManager, Mockito.times(1)).setPathParser(Mockito.any());
		Mockito.verify(arrayInputManager, Mockito.times(1)).ingest(Mockito.any(), (BPInput) Mockito.any());

	}

	@Test
	public void testingestScripts() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Error ingesting scripts", BPException.ERROR_CODES.ERROR_INGESTING_SCRIPTS));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelOutputFolderCreate/");
		Mockito.doReturn(dirUrl.getFile()).when(file).getAbsolutePath();

		ScriptUploaderManager uploaderManager = Mockito.mock(ScriptUploaderManager.class);

		executor.setUploader(uploaderManager);
		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);
		Mockito.doReturn(false).when(result).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				Script inputDescription = (Script) invocation.getArguments()[0];

				if (!inputDescription.getRepoPath().equalsIgnoreCase("sample.sh"))
					throw new Exception("Bad repo path " + inputDescription.getRepoPath());

				return result;
			}
		}).when(uploaderManager).ingest(Mockito.any());

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.ingestScripts();

	}

	@Test
	public void testingestScripts2() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Error reading scripts to copy", null));

		String runid = "runid";

		File file = Mockito.mock(File.class);
		ISourceCodeConnector connector = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doReturn(file).when(connector).getDir();
		SourceCodeExecutor executor = Mockito.spy(new SourceCodeExecutor(connector));
		executor.setDeleteSourceCodeDironExit(true);
		executor.setDeleteDockerContainerFolerOnExit(true);

		BPRunStatus initialStatus = Mockito.spy(new BPRunStatus());
		initialStatus.setStatus(BPRunStatuses.QUEUED.toString());
		initialStatus.setRunid(runid);
		executor.setBPStatus(initialStatus);

		IContainerOrchestratorCommandExecutor dockerEx = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		executor.setDockerExecutor(dockerEx);

		executor.setConventionFileLoader(ISourceCodeConventionFileLoaderMock.mocked(connector.getDir().getAbsolutePath()));

		executor.ingestScripts();

	}

}