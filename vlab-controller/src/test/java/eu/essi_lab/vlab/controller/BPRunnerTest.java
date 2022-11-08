package eu.essi_lab.vlab.controller;

import eu.essi_lab.vlab.controller.services.IBPAdapter;
import eu.essi_lab.vlab.controller.services.IResourceAllocator;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.ResourceRequestResponse;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.controller.services.IBPComputeInfraProvider;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BPRunnerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testRunBpResourcesAcquireFalse() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		BPRunStatus status = Mockito.spy(new BPRunStatus());
		Mockito.doReturn(status).when(statusRegistry).getBPRunStatus(Mockito.any());

		runner.setStatusRegistry(statusRegistry);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);
		AtomicExecutableBP exBP = Mockito.mock(AtomicExecutableBP.class);

		BPRealization realization = Mockito.mock(BPRealization.class);
		Mockito.doReturn(realization).when(exBP).getRealization();

		Mockito.doReturn(exBP).when(executableBPRegistry).getExecutable(Mockito.any(), Mockito.any());

		runner.setExRegistry(executableBPRegistry);
		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		Mockito.doReturn(adapter).when(runner).adapterFromFactory(Mockito.any());

		Mockito.doNothing().when(adapter).execute(Mockito.any());

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(false).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(adapter).acquireResources(Mockito.any());

		IBPComputeInfraProvider computeInfraProvider = Mockito.mock(IBPComputeInfraProvider.class);
		Mockito.doReturn(computeInfraProvider).when(runner).instantiateIBPComputeInfraProvider();

		Mockito.doReturn(Mockito.mock(BPComputeInfrastructure.class)).when(computeInfraProvider).selectComputeInfrastructure(Mockito.any(),
				Mockito.any());

		BPRunStatus afterRunStatus = runner.runBusinessProcess();

		Assert.assertEquals(BPRunStatuses.QUEUED.toString(), afterRunStatus.getStatus());

		Mockito.verify(status, Mockito.times(2)).setExecutionInfrastructureLabel(Mockito.any());
		Mockito.verify(status, Mockito.times(2)).setExecutionInfrastructureId(Mockito.any());

	}

	@Test
	public void testRunBpResourcesAcquireOk() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		BPRunStatus status = Mockito.spy(new BPRunStatus());
		Mockito.doReturn(status).when(statusRegistry).getBPRunStatus(Mockito.any());

		runner.setStatusRegistry(statusRegistry);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);
		AtomicExecutableBP exBP = Mockito.mock(AtomicExecutableBP.class);

		BPRealization realization = Mockito.mock(BPRealization.class);
		Mockito.doReturn(realization).when(exBP).getRealization();

		Mockito.doReturn(exBP).when(executableBPRegistry).getExecutable(Mockito.any(), Mockito.any());

		runner.setExRegistry(executableBPRegistry);
		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		Mockito.doReturn(adapter).when(runner).adapterFromFactory(Mockito.any());

		Mockito.doNothing().when(adapter).execute(Mockito.any());

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(true).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(adapter).acquireResources(Mockito.any());

		IBPComputeInfraProvider computeInfraProvider = Mockito.mock(IBPComputeInfraProvider.class);
		Mockito.doReturn(computeInfraProvider).when(runner).instantiateIBPComputeInfraProvider();

		Mockito.doReturn(Mockito.mock(BPComputeInfrastructure.class)).when(computeInfraProvider).selectComputeInfrastructure(Mockito.any(),
				Mockito.any());

		BPRunStatus afterRunStatus = runner.runBusinessProcess();

		Assert.assertNotEquals(BPRunStatuses.QUEUED.toString(), afterRunStatus.getStatus());

		Mockito.verify(status, Mockito.times(1)).setExecutionInfrastructureLabel(Mockito.any());
		Mockito.verify(status, Mockito.times(1)).setExecutionInfrastructureId(Mockito.any());

	}

	@Test
	public void testRelease() throws BPException {
		BPRunner runner = Mockito.spy(new BPRunner(new BPRun()));

		runner.releaseResources();

		Mockito.verify(runner, Mockito.times(0)).getAdapter();
	}

	@Test
	public void testRelease2() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		BPRunStatus status = new BPRunStatus();
		Mockito.doReturn(status).when(statusRegistry).getBPRunStatus(Mockito.any());

		runner.setStatusRegistry(statusRegistry);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);
		AtomicExecutableBP exBP = Mockito.mock(AtomicExecutableBP.class);

		BPRealization realization = Mockito.mock(BPRealization.class);
		Mockito.doReturn(realization).when(exBP).getRealization();

		Mockito.doReturn(exBP).when(executableBPRegistry).getExecutable(Mockito.any(), Mockito.any());

		runner.setExRegistry(executableBPRegistry);
		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		Mockito.doReturn(adapter).when(runner).adapterFromFactory(Mockito.any());

		Mockito.doNothing().when(adapter).execute(Mockito.any());
		Mockito.doNothing().when(adapter).releaseResources(Mockito.any());

		ContainerOrchestratorReservationResult reservationResult = Mockito.mock(ContainerOrchestratorReservationResult.class);
		Mockito.doReturn(true).when(reservationResult).isAcquired();

		Mockito.doReturn(reservationResult).when(adapter).acquireResources(Mockito.any());

		IBPComputeInfraProvider computeInfraProvider = Mockito.mock(IBPComputeInfraProvider.class);
		Mockito.doReturn(computeInfraProvider).when(runner).instantiateIBPComputeInfraProvider();

		Mockito.doReturn(Mockito.mock(BPComputeInfrastructure.class)).when(computeInfraProvider).selectComputeInfrastructure(Mockito.any(),
				Mockito.any());

		BPRunStatus afterRunStatus = runner.runBusinessProcess();

		Assert.assertNotEquals(BPRunStatuses.QUEUED.toString(), afterRunStatus.getStatus());

		runner.releaseResources();

		Mockito.verify(adapter, Mockito.times(1)).releaseResources(Mockito.any());
	}

	@Test
	public void testRunBpNoAdapterFound() throws BPException {

		expectedException.expect(new BaseMatcher<Object>() {
			@Override
			public boolean matches(Object o) {
				return BPException.ERROR_CODES.NO_ADAPTER_AVAILABLE.getCode() == ((BPException) o).getErroCode();
			}

			@Override
			public void describeMismatch(Object o, Description description) {

				description.appendText("Found " + ((BPException) o).getErroCode());

			}

			@Override
			public void describeTo(Description description) {

				description.appendText("Expected exception code is " + BPException.ERROR_CODES.NO_ADAPTER_AVAILABLE.getCode());

			}
		});

		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		BPRunStatus status = new BPRunStatus();
		Mockito.doReturn(status).when(statusRegistry).getBPRunStatus(Mockito.any());

		runner.setStatusRegistry(statusRegistry);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);

		AtomicExecutableBP exBP = Mockito.mock(AtomicExecutableBP.class);

		BPRealization realization = Mockito.mock(BPRealization.class);
		Mockito.doReturn(realization).when(exBP).getRealization();

		Mockito.doReturn(exBP).when(executableBPRegistry).getExecutable(Mockito.any(), Mockito.any());

		runner.setExRegistry(executableBPRegistry);

		runner.runBusinessProcess();

	}

	@Test
	public void testRunBpNoRealizationFound() throws BPException {

		expectedException.expect(new BaseMatcher<Object>() {
			@Override
			public boolean matches(Object o) {
				return BPException.ERROR_CODES.NO_BP_REALIZATION.getCode() == ((BPException) o).getErroCode();
			}

			@Override
			public void describeMismatch(Object o, Description description) {

				description.appendText("Found " + ((BPException) o).getErroCode());

			}

			@Override
			public void describeTo(Description description) {

				description.appendText("Expected exception code is " + BPException.ERROR_CODES.NO_BP_REALIZATION.getCode());

			}
		});

		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		BPRunStatus status = new BPRunStatus();
		Mockito.doReturn(status).when(statusRegistry).getBPRunStatus(Mockito.any());

		runner.setStatusRegistry(statusRegistry);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);
		AtomicExecutableBP exBP = Mockito.mock(AtomicExecutableBP.class);

		Mockito.doReturn(exBP).when(executableBPRegistry).getExecutable(Mockito.any(), Mockito.any());

		runner.setExRegistry(executableBPRegistry);

		runner.runBusinessProcess();

	}

	@Test
	public void testAcquireResources() throws BPException {
		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		Mockito.doReturn(resources).when(adapter).getRequiredResources();

		runner.setAdapter(adapter);

		IResourceAllocator allocator = Mockito.mock(IResourceAllocator.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				VLabDockerResources r = (VLabDockerResources) invocation.getArguments()[0];

				if (!r.equals(resources))
					throw new Exception();

				return null;
			}
		}).when(allocator).request(Mockito.any());

		Mockito.doReturn(allocator).when(runner).instantiateAllocator();
		runner.requireResources();
	}

	@Test
	public void testAcquireResourcesExceptionGettingResources() throws BPException {
		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		Mockito.doThrow(new BPException()).when(adapter).getRequiredResources();

		runner.setAdapter(adapter);

		IResourceAllocator allocator = Mockito.mock(IResourceAllocator.class);

		ResourceRequestResponse response = Mockito.mock(ResourceRequestResponse.class);
		Mockito.doReturn(response).when(allocator).request(Mockito.any());

		Mockito.doReturn(allocator).when(runner).instantiateAllocator();
		runner.requireResources();

		Mockito.verify(allocator, Mockito.times(0)).request(Mockito.any());
	}

	@Test
	public void testCleanResources() throws BPException {
		BPRun run = Mockito.mock(BPRun.class);

		BPRunner runner = Mockito.spy(new BPRunner(run));

		IBPAdapter adapter = Mockito.mock(IBPAdapter.class);

		Mockito.doNothing().when(adapter).cleanBPRunResources(Mockito.any());

		runner.setAdapter(adapter);

		runner.cleanBPResources();

		Mockito.verify(adapter, Mockito.times(1)).cleanBPRunResources(Mockito.any());
	}
}