package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BPRunLogRegistryTest {

	@Test
	public void test() throws BPException {
		BPRunLogRegistry registry = new BPRunLogRegistry();

		IBPRunLogStorage storage = Mockito.mock(IBPRunLogStorage.class);

		registry.setBPRunLogStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);
		String next = "next";
		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(null).when(status).getModelTaskId();
		LogMessagesResponse resp = registry.getLogs(status, false, next, user);

		Assert.assertEquals(1, resp.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", resp.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(0)).readLog(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	public void test2() throws BPException {
		BPRunLogRegistry registry = new BPRunLogRegistry();

		IBPRunLogStorage storage = Mockito.mock(IBPRunLogStorage.class);

		registry.setBPRunLogStorage(storage);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);
		String next = "next";
		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn("").when(status).getModelTaskId();
		LogMessagesResponse resp = registry.getLogs(status, false, next, user);

		Assert.assertEquals(1, resp.getEvents().size());
		Assert.assertEquals("Model Execution has not produced any log yet.", resp.getEvents().get(0).getMessage());

		Mockito.verify(storage, Mockito.times(0)).readLog(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	public void test3() throws BPException {
		BPRunLogRegistry registry = new BPRunLogRegistry();

		IBPRunLogStorage storage = Mockito.mock(IBPRunLogStorage.class);

		registry.setBPRunLogStorage(storage);
		

		BPRunStatus status = Mockito.mock(BPRunStatus.class);
		String next = "next";
		BPUser user = Mockito.mock(BPUser.class);

		String taskid = "taskid";

		Mockito.doReturn(taskid).when(status).getModelTaskId();

		LogMessagesResponse response = Mockito.mock(LogMessagesResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				Boolean head = (Boolean) invocationOnMock.getArguments()[1];

				String nt = (String) invocationOnMock.getArguments()[2];

				if (!taskid.equals(id))
					throw new Exception("Bad task id");

				if (head)
					throw new Exception(" head");
				if (!next.equals(nt))
					throw new Exception("Bad next token");

				return response;
			}
		}).when(storage).readLog(Mockito.any(), Mockito.any(), Mockito.any());

		LogMessagesResponse resp = registry.getLogs(status, false, next, user);

		Assert.assertNotNull(resp);
		Mockito.verify(storage, Mockito.times(1)).readLog(Mockito.any(), Mockito.any(), Mockito.any());
	}
}