package eu.essi_lab.vlab.controller.executable;

import eu.essi_lab.vlab.controller.BPExceptionMatcher;
import eu.essi_lab.vlab.controller.BPRunAgent;
import eu.essi_lab.vlab.controller.BPRunner;
import eu.essi_lab.vlab.controller.services.IBPAdapter;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunPullResult;
import eu.essi_lab.vlab.core.datamodel.BPRunResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.PullResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.engine.factory.BPRegistriesFactory;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import java.util.Optional;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class BPEngineCommandLineExecutorTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private String defaultid;
	private String test_defaultid = "tdef";

	@Before
	public void before() {
		defaultid = ConfigurationLoader.loadConfigurationParameter(
				BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_ID.getParameter());

		environmentVariables.set(BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_ID.getParameter().getKey(), test_defaultid);
	}

	@After
	public void after() {
		environmentVariables.set(BPStaticConfigurationParameters.COMPUTE_INFRASTRUCTURE_ID.getParameter().getKey(), defaultid);
	}

	@Test
	public void testNoArgument() throws BPException {

		expectedException.expect(BPException.class);
		BPEngineCommandLineExecutor executor = new BPEngineCommandLineExecutor();

		executor.execCmd(new String[] {});

	}

	@Test
	public void test() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Exception decoding command", BPException.ERROR_CODES.INVALID_REQUEST));

		BPEngineCommandLineExecutor executor = new BPEngineCommandLineExecutor();

		executor.execCmd(new String[] { "pulloo" });

	}

	@Test
	public void testNullFactory() throws BPException {
		expectedException.expect(new BPExceptionMatcher("", BPException.ERROR_CODES.BAD_CONFIGURATION));

		BPEngineCommandLineExecutor executor = new BPEngineCommandLineExecutor();

		executor.execCmd(new String[] { "pull" });

	}

	@Test
	public void testExceptionPullingNextRun() throws BPException {

		BPEngineCommandLineExecutor executor = Mockito.spy(new BPEngineCommandLineExecutor());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry runRegistry = Mockito.mock(IBPRunRegistry.class);
		BPRun run = new BPRun();

		String runid = "testrunid";
		run.setRunid(runid);
		Mockito.when(runRegistry.nextBPRun()).thenReturn(Optional.of(run));
		Mockito.when(runRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);
		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(runRegistry);

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);
		BPRunStatus status = new BPRunStatus();
		Mockito.when(statusRegistry.getBPRunStatus(Mockito.any())).thenReturn(status);
		Mockito.when(mockFactory.getBPRunStatusRegistry()).thenReturn(statusRegistry);

		IExecutableBPRegistry executableRegistry = Mockito.mock(IExecutableBPRegistry.class);
		Mockito.when(executableRegistry.getExecutable(Mockito.any(), Mockito.any())).thenThrow(BPException.class);
		Mockito.when(mockFactory.getExecutableRegistry()).thenReturn(executableRegistry);

		executor.setBPRegistriesFactory(mockFactory);

		BPRunAgent agent = Mockito.spy(new BPRunAgent());

		agent.setStatusRegistry(statusRegistry);

		agent.setRegistry(runRegistry);

		agent.setExRegistry(executableRegistry);

		Mockito.doReturn(agent).when(executor).initAgent();

		BPRunner runner = Mockito.mock(BPRunner.class);

		Mockito.doNothing().when(runner).cleanBPResources();
		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		VLabDockerResources res = Mockito.mock(VLabDockerResources.class);
		Mockito.doReturn(res).when(adapter).getRequiredResources();

		Mockito.doReturn(adapter).when(runner).getAdapter();

		Mockito.doReturn(run).when(runner).getBPRun();
		Mockito.doReturn(runner).when(agent).getBPRunner(Mockito.any());

		executor.execCmd(new String[] { "pull" });

		assertEquals(BPRunStatuses.COMPLETED.toString(), status.getStatus());
		assertEquals(BPRunResult.FAIL.toString(), status.getResult());

	}

	@Test
	public void testexecPull() throws BPException {

		BPEngineCommandLineExecutor executor = Mockito.spy(new BPEngineCommandLineExecutor());

		BPRunAgent agent = Mockito.mock(BPRunAgent.class);
		Mockito.doReturn(agent).when(executor).initAgent();

		executor.execPull();

		Mockito.verify(agent, Mockito.times(1)).pullRun();
	}

	@Test
	public void testExceptionInExecPull() throws BPException {
		expectedException.expect(new BPExceptionMatcher("Exception pulling next run", BPException.ERROR_CODES.BPRUN_PULL_ERROR));

		BPEngineCommandLineExecutor executor = Mockito.spy(new BPEngineCommandLineExecutor());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		executor.setBPRegistriesFactory(mockFactory);

		Mockito.doThrow(BPException.class).when(executor).execPull();

		executor.execCmd(new String[] { "pull" });

	}

	@Test
	public void testExceptionInWaitPull() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Exception handling pull result", BPException.ERROR_CODES.BPRUN_PULL_RESULT_HANDLING_ERROR));

		BPEngineCommandLineExecutor executor = Mockito.spy(new BPEngineCommandLineExecutor());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		executor.setBPRegistriesFactory(mockFactory);
		BPRunPullResult pullResult = Mockito.mock(BPRunPullResult.class);

		Mockito.doReturn(pullResult).when(executor).execPull();
		Mockito.doThrow(BPException.class).when(executor).wait(Mockito.any());

		executor.execCmd(new String[] { "pull" });

	}

	@Test
	public void testWait() throws BPException {

		BPEngineCommandLineExecutor executor = Mockito.spy(new BPEngineCommandLineExecutor());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		executor.setBPRegistriesFactory(mockFactory);

		BPRunPullResult pullResult = Mockito.mock(BPRunPullResult.class);

		PullResult status = PullResult.EXECUTION_TRIGGERED;

		Mockito.doReturn(status).when(pullResult).getResult();

		Mockito.doReturn(pullResult).when(executor).execPull();

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		BPRunStatus runStatus = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(BPRunStatuses.EXECUTING.toString(), BPRunStatuses.COMPLETED.toString()).when(runStatus).getStatus();

		Mockito.doReturn(runStatus).when(statusRegistry).getBPRunStatus(Mockito.any());

		Mockito.when(mockFactory.getBPRunStatusRegistry()).thenReturn(statusRegistry);

		executor.execCmd(new String[] { "pull" });

	}

	@Test
	public void testWaitExceptioninLoop() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Exception handling pull result", BPException.ERROR_CODES.BPRUN_PULL_RESULT_HANDLING_ERROR));

		BPEngineCommandLineExecutor executor = Mockito.spy(new BPEngineCommandLineExecutor());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		executor.setBPRegistriesFactory(mockFactory);

		BPRunPullResult pullResult = Mockito.mock(BPRunPullResult.class);

		PullResult status = PullResult.EXECUTION_TRIGGERED;

		Mockito.doReturn(status).when(pullResult).getResult();

		Mockito.doReturn(pullResult).when(executor).execPull();

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		Mockito.doThrow(BPException.class).when(statusRegistry).getBPRunStatus(Mockito.any());

		Mockito.when(mockFactory.getBPRunStatusRegistry()).thenReturn(statusRegistry);

		executor.execCmd(new String[] { "pull" });

	}
}