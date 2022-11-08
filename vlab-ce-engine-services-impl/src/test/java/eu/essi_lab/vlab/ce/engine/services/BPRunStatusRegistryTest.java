package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusStorage;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BPRunStatusRegistryTest {

	@Test
	public void test() {
		BPRunStatusRegistry statusRegistry = new BPRunStatusRegistry();
		IBPRunStatusStorage storage = Mockito.mock(IBPRunStatusStorage.class);
		statusRegistry.setBPRunStatusStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(true).when(storage).store(Mockito.any());

		Assert.assertTrue(statusRegistry.createBPRunStatus(status));

		Mockito.verify(storage, Mockito.times(1)).store(Mockito.any());
		Mockito.verify(status, Mockito.times(1)).addObserver(Mockito.any());
	}

	@Test
	public void test2() {
		BPRunStatusRegistry statusRegistry = new BPRunStatusRegistry();
		IBPRunStatusStorage storage = Mockito.mock(IBPRunStatusStorage.class);
		statusRegistry.setBPRunStatusStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(false).when(storage).store(Mockito.any());

		Assert.assertFalse(statusRegistry.createBPRunStatus(status));

		Mockito.verify(storage, Mockito.times(1)).store(Mockito.any());
		Mockito.verify(status, Mockito.times(0)).addObserver(Mockito.any());
	}

	@Test
	public void test3() {
		BPRunStatusRegistry statusRegistry = new BPRunStatusRegistry();
		IBPRunStatusStorage storage = Mockito.mock(IBPRunStatusStorage.class);
		statusRegistry.setBPRunStatusStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(true).when(storage).store(Mockito.any());

		Assert.assertTrue(statusRegistry.updateBPRunStatus(status));

		Mockito.verify(storage, Mockito.times(1)).store(Mockito.any());
		Mockito.verify(status, Mockito.times(0)).addObserver(Mockito.any());
	}

	@Test
	public void test4() throws BPException {
		BPRunStatusRegistry statusRegistry = new BPRunStatusRegistry();
		IBPRunStatusStorage storage = Mockito.mock(IBPRunStatusStorage.class);
		statusRegistry.setBPRunStatusStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		String runid = "runid";

		Mockito.doReturn(runid).when(status).getRunid();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!runid.equalsIgnoreCase(id))
					throw new Exception("Bad id");

				return true;
			}
		}).when(storage).remove(Mockito.any());

		Assert.assertTrue(statusRegistry.deleteBPRunStatus(status));

		Mockito.verify(storage, Mockito.times(1)).remove(Mockito.any());
		Mockito.verify(status, Mockito.times(0)).addObserver(Mockito.any());
	}

	@Test
	public void test5() throws BPException {
		BPRunStatusRegistry statusRegistry = new BPRunStatusRegistry();
		IBPRunStatusStorage storage = Mockito.mock(IBPRunStatusStorage.class);
		statusRegistry.setBPRunStatusStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		String runid = "runid";

		Mockito.doReturn(runid).when(status).getRunid();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!runid.equalsIgnoreCase(id))
					throw new Exception("Bad id");

				return status;
			}
		}).when(storage).get(Mockito.any());

		Assert.assertEquals(runid, statusRegistry.getBPRunStatus(runid).getRunid());

		Mockito.verify(storage, Mockito.times(1)).get(Mockito.any());
		Mockito.verify(status, Mockito.times(1)).addObserver(Mockito.any());
	}

	@Test
	public void test6() throws BPException {
		BPRunStatusRegistry statusRegistry = Mockito.spy(new BPRunStatusRegistry());
		IBPRunStatusStorage storage = Mockito.mock(IBPRunStatusStorage.class);
		statusRegistry.setBPRunStatusStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		String runid = "runid";

		Mockito.doReturn(runid).when(status).getRunid();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				BPRunStatus id = (BPRunStatus) invocationOnMock.getArguments()[0];

				if (!runid.equalsIgnoreCase(id.getRunid()))
					throw new Exception("Bad id");

				return true;
			}
		}).when(statusRegistry).updateBPRunStatus(Mockito.any());

		statusRegistry.update(status, new Object());

		Mockito.verify(statusRegistry, Mockito.times(1)).updateBPRunStatus(Mockito.any());

	}

}