package eu.essi_lab.vlab.controller;

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
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import java.util.Optional;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class BPRunAgentTest {
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
	public void testTryToExecuteSuccess() throws BPException {

		BPRun run = new BPRun();

		run.setRunid("runid");

		BPRunner runner = Mockito.mock(BPRunner.class);
		Mockito.when(runner.getBPRun()).thenReturn(run);
		BPRunStatus status = new BPRunStatus();

		status.setStatus(BPRunStatuses.RESERVING_RESOURCES.toString());

		Mockito.doReturn(status).when(runner).runBusinessProcess();

		BPRunAgent agent = Mockito.spy(new BPRunAgent());

		IBPRunRegistry bpRunRegistry = Mockito.mock(IBPRunRegistry.class);
		agent.setRegistry(bpRunRegistry);

		Mockito.doNothing().when(bpRunRegistry).moveToTriggered(Mockito.any());

		BPRunPullResult result = agent.tryToExecute(runner);

		Mockito.verify(runner, Mockito.atLeastOnce()).setExRegistry(Mockito.any());
		Mockito.verify(runner, Mockito.atLeastOnce()).setStatusRegistry(Mockito.any());
		Mockito.verify(runner, Mockito.times(0)).releaseResources();
		assertEquals(PullResult.EXECUTION_TRIGGERED, result.getResult());
		Mockito.verify(bpRunRegistry, Mockito.times(1)).moveToTriggered(Mockito.any());
		Mockito.verify(agent, Mockito.times(1)).scheduleVisibilityTimeoutUpdate(Mockito.any());
		Mockito.verify(agent, Mockito.times(1)).stopVisibilityTimeoutUpdate();

	}

	@Test
	public void testTryToExecuteNoResources() throws BPException {

		BPRun run = new BPRun();

		run.setRunid("runid");

		BPRunner runner = Mockito.mock(BPRunner.class);

		Mockito.when(runner.getBPRun()).thenReturn(run);

		Mockito.doNothing().when(runner).requireResources();

		BPRunStatus status = Mockito.spy(new BPRunStatus());

		status.setStatus(BPRunStatuses.QUEUED.toString());

		Mockito.doReturn(status).when(runner).runBusinessProcess();

		BPRunAgent a = new BPRunAgent();

		BPRunAgent agent = Mockito.spy(a);

		IBPRunRegistry bpRunRegistry = Mockito.mock(IBPRunRegistry.class);
		agent.setRegistry(bpRunRegistry);

		Mockito.doNothing().when(bpRunRegistry).moveToTriggered(Mockito.any());

		BPRunPullResult result = agent.tryToExecute(runner);

		Mockito.verify(runner, Mockito.atLeastOnce()).setExRegistry(Mockito.any());
		Mockito.verify(runner, Mockito.atLeastOnce()).setStatusRegistry(Mockito.any());

		assertEquals(PullResult.NO_RESOUCE_AVAILABLE, result.getResult());

		Mockito.verify(bpRunRegistry, Mockito.times(0)).moveToTriggered(Mockito.any());
		Mockito.verify(runner, Mockito.times(0)).releaseResources();
		Mockito.verify(status, Mockito.times(0)).setExecutionInfrastructureLabel(Mockito.any());
		Mockito.verify(status, Mockito.times(0)).setExecutionInfrastructureId(Mockito.any());
		Mockito.verify(agent, Mockito.times(1)).scheduleVisibilityTimeoutUpdate(Mockito.any());
		Mockito.verify(agent, Mockito.times(1)).stopVisibilityTimeoutUpdate();

	}

	@Test
	public void testTryToExecuteReservedOkMoveToTriggeredError() throws BPException {

		BPRun run = new BPRun();

		run.setRunid("runid");

		BPRunner runner = Mockito.mock(BPRunner.class);

		Mockito.when(runner.getBPRun()).thenReturn(run);

		Mockito.doNothing().when(runner).requireResources();

		BPRunStatus status = Mockito.spy(new BPRunStatus());

		status.setStatus(BPRunStatuses.INGESTING_INPUTS.toString());

		Mockito.doReturn(status).when(runner).runBusinessProcess();

		BPRunAgent a = new BPRunAgent();

		BPRunAgent agent = Mockito.spy(a);

		IBPRunRegistry bpRunRegistry = Mockito.mock(IBPRunRegistry.class);
		agent.setRegistry(bpRunRegistry);

		Mockito.doThrow(BPException.class).when(bpRunRegistry).moveToTriggered(Mockito.any());

		Mockito.doReturn(runner).when(agent).getBPRunner(Mockito.any());

		BPRunPullResult result = null;
		try {
			result = agent.execute(run);
		} catch (BPException e) {
			e.printStackTrace();
		}

		Mockito.verify(runner, Mockito.atLeastOnce()).setExRegistry(Mockito.any());
		Mockito.verify(runner, Mockito.atLeastOnce()).setStatusRegistry(Mockito.any());
		Mockito.verify(runner, Mockito.times(1)).releaseResources();

		Mockito.verify(bpRunRegistry, Mockito.times(2)).moveToTriggered(Mockito.any());

		Mockito.verify(status, Mockito.times(0)).setExecutionInfrastructureLabel(Mockito.any());
		Mockito.verify(status, Mockito.times(0)).setExecutionInfrastructureId(Mockito.any());
		Mockito.verify(agent, Mockito.times(1)).scheduleVisibilityTimeoutUpdate(Mockito.any());
		Mockito.verify(agent, Mockito.times(2)).stopVisibilityTimeoutUpdate();

	}

	@Test
	public void testTryToExecuteThrowable() throws BPException {

		BPRun run = new BPRun();

		run.setRunid("runid");

		BPRunner runner = Mockito.mock(BPRunner.class);

		Mockito.when(runner.getBPRun()).thenReturn(run);

		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		Mockito.doReturn(adapter).when(runner).getAdapter();

		BPRunStatus status = new BPRunStatus();

		status.setStatus(BPRunStatuses.QUEUED.toString());

		Mockito.doThrow(BPException.class).when(runner).runBusinessProcess();

		BPRunAgent agent = Mockito.spy(new BPRunAgent());

		Mockito.when(agent.getBPRunner(Mockito.any())).thenReturn(runner);

		IBPRunRegistry bpRunRegistry = Mockito.mock(IBPRunRegistry.class);
		agent.setRegistry(bpRunRegistry);
		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		Mockito.when(statusRegistry.getBPRunStatus(Mockito.any())).thenReturn(status);

		agent.setStatusRegistry(statusRegistry);

		Mockito.when(bpRunRegistry.nextBPRun()).thenReturn(Optional.of(run));

		Mockito.doReturn(run).when(bpRunRegistry).get(Mockito.any(), Mockito.any());

		BPRunPullResult result = agent.pullRun();

		assertEquals(PullResult.EXECUTION_EXCEPTION, result.getResult());

		assertEquals(BPRunStatuses.COMPLETED.toString(), status.getStatus());
		assertEquals(BPRunResult.FAIL.toString(), status.getResult());

		assertNotNull(status.getMessage());
		Mockito.verify(runner, Mockito.times(1)).releaseResources();
		System.out.println(status.getMessage());
		Mockito.verify(runner, Mockito.times(1)).runBusinessProcess();
		Mockito.verify(agent, Mockito.times(1)).scheduleVisibilityTimeoutUpdate(Mockito.any());
		Mockito.verify(agent, Mockito.times(1)).stopVisibilityTimeoutUpdate();

	}

	@Test
	public void testTryToExecuteBPExceptio() throws BPException {

		BPRun run = new BPRun();

		run.setRunid("runid");

		BPRunner runner = Mockito.mock(BPRunner.class);

		Mockito.when(runner.getBPRun()).thenReturn(run);

		BPRunStatus status = new BPRunStatus();

		status.setStatus(BPRunStatuses.QUEUED.toString());

		Mockito.doThrow(BPException.class).when(runner).runBusinessProcess();

		BPRunAgent agent = Mockito.spy(new BPRunAgent());

		Mockito.when(agent.getBPRunner(Mockito.any())).thenReturn(runner);

		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		Mockito.doReturn(adapter).when(runner).getAdapter();

		IBPRunRegistry bpRunRegistry = Mockito.mock(IBPRunRegistry.class);
		agent.setRegistry(bpRunRegistry);
		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		Mockito.when(statusRegistry.getBPRunStatus(Mockito.any())).thenReturn(status);

		agent.setStatusRegistry(statusRegistry);

		Mockito.when(bpRunRegistry.nextBPRun()).thenReturn(Optional.of(run));

		Mockito.doReturn(run).when(bpRunRegistry).get(Mockito.any(), Mockito.any());

		BPRunPullResult result = agent.pullRun();

		assertEquals(PullResult.EXECUTION_EXCEPTION, result.getResult());

		assertEquals(BPRunStatuses.COMPLETED.toString(), status.getStatus());
		assertEquals(BPRunResult.FAIL.toString(), status.getResult());

		assertNotNull(status.getMessage());
		Mockito.verify(runner, Mockito.times(1)).releaseResources();
		System.out.println(status.getMessage());
		Mockito.verify(runner, Mockito.times(1)).runBusinessProcess();
		Mockito.verify(agent, Mockito.times(1)).scheduleVisibilityTimeoutUpdate(Mockito.any());
		Mockito.verify(agent, Mockito.times(1)).stopVisibilityTimeoutUpdate();
	}

	@Test
	public void testNoRunAvailable() throws BPException {

		BPRunAgent agent = new BPRunAgent();

		IBPRunRegistry bpRunRegistry = Mockito.mock(IBPRunRegistry.class);

		agent.setRegistry(bpRunRegistry);

		Mockito.doReturn(Optional.ofNullable(null)).when(bpRunRegistry).nextBPRun();

		BPRunPullResult result = agent.pullRun();

		assertEquals(PullResult.NO_QUEQUED_RUN, result.getResult());

	}

	@Test
	public void testFetch() throws BPException {

		BPRunAgent agent = Mockito.spy(new BPRunAgent());

		IBPRunRegistry bpRunRegistry = Mockito.mock(IBPRunRegistry.class);
		agent.setRegistry(bpRunRegistry);

		BPRun run = new BPRun();
		run.setRunid("id");
		Mockito.doReturn(Optional.of(run)).when(bpRunRegistry).nextBPRun();

		Optional<BPRun> fetched = agent.fetchNextRun();

		assertTrue(fetched.isPresent());

	}

}