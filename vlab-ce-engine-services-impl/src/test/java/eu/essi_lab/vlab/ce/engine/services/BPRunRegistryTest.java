package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
public class BPRunRegistryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testRegisterBPRun() throws BPException {

		BPRunRegistry registry = new BPRunRegistry();

		BPRunStatusRegistry mockStatusRegistry = Mockito.mock(BPRunStatusRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String status = ((BPRunStatus) invocation.getArguments()[0]).getStatus();

				if (status == null)
					throw new Exception("Found null status when creating BPRunStatus.");

				return true;
			}
		}).when(mockStatusRegistry).createBPRunStatus(Mockito.any());

		registry.setStatusRegistry(mockStatusRegistry);

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doReturn(true).when(mockStorage).queue(Mockito.any());

		registry.setBPRunStorage(mockStorage);

		BPRun run = new BPRun();
		run = registry.registerBPRun(run);

		assertNotNull(run.getRunid());

	}

	@Test
	public void testRegisterBPRun2() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Unable to queue new run", BPException.ERROR_CODES.BPRUN_REGISTRY_ERROR));

		BPRunRegistry registry = new BPRunRegistry();

		BPRunStatusRegistry mockStatusRegistry = Mockito.mock(BPRunStatusRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String status = ((BPRunStatus) invocation.getArguments()[0]).getStatus();

				if (status == null)
					throw new Exception("Found null status when creating BPRunStatus.");

				return true;
			}
		}).when(mockStatusRegistry).createBPRunStatus(Mockito.any());

		registry.setStatusRegistry(mockStatusRegistry);

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doReturn(false).when(mockStorage).queue(Mockito.any());

		registry.setBPRunStorage(mockStorage);

		BPRun run = new BPRun();
		run = registry.registerBPRun(run);

		assertNotNull(run.getRunid());

	}

	@Test
	public void testRegisterBPRun3() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Unable to queue status of new run", BPException.ERROR_CODES.STATUS_REGISTRY_ERROR));

		BPRunRegistry registry = new BPRunRegistry();

		BPRunStatusRegistry mockStatusRegistry = Mockito.mock(BPRunStatusRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String status = ((BPRunStatus) invocation.getArguments()[0]).getStatus();

				if (status == null)
					throw new Exception("Found null status when creating BPRunStatus.");

				return false;
			}
		}).when(mockStatusRegistry).createBPRunStatus(Mockito.any());

		registry.setStatusRegistry(mockStatusRegistry);

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doReturn(true).when(mockStorage).queue(Mockito.any());

		registry.setBPRunStorage(mockStorage);

		BPRun run = new BPRun();
		registry.registerBPRun(run);

	}

	@Test
	public void testGetNextBPRunUnregistered() throws BPException {

		String runid1 = "runid1";
		String runid2 = "runid2";
		String runid3 = "runid3";

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		BPRun run1 = Mockito.mock(BPRun.class);

		Mockito.when(run1.getRunid()).thenReturn(runid1);

		BPRun run2 = Mockito.mock(BPRun.class);

		Mockito.when(run2.getRunid()).thenReturn(runid2);

		BPRun run3 = Mockito.mock(BPRun.class);

		Mockito.when(run3.getRunid()).thenReturn(runid3);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String id = (String) invocation.getArguments()[0];

				if (id.equalsIgnoreCase(runid1))
					return false;

				if (id.equalsIgnoreCase(runid2))
					return false;

				return true;
			}
		}).when(mockStorage).exists(Mockito.any());

		Mockito.when(mockStorage.nextQuequedBPRun()).thenReturn(Optional.of(run1), Optional.of(run2), Optional.of(run3));

		registry.setBPRunStorage(mockStorage);

		Optional<BPRun> run = registry.nextBPRun();

		assertEquals(runid3, run.get().getRunid());

	}

	@Test
	public void testGetNextBPRunUnregistered2() throws BPException {

		String runid1 = "runid1";
		String runid2 = "runid2";
		String runid3 = "runid3";

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		BPRun run1 = Mockito.mock(BPRun.class);

		Mockito.when(run1.getRunid()).thenReturn(runid1);

		BPRun run2 = Mockito.mock(BPRun.class);

		Mockito.when(run2.getRunid()).thenReturn(runid2);

		BPRun run3 = Mockito.mock(BPRun.class);

		Mockito.when(run3.getRunid()).thenReturn(runid3);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				return false;

			}
		}).when(mockStorage).exists(Mockito.any());

		Mockito.when(mockStorage.nextQuequedBPRun()).thenReturn(Optional.of(run1), Optional.of(run2), Optional.of(run3), Optional.empty());

		registry.setBPRunStorage(mockStorage);

		Optional<BPRun> run = registry.nextBPRun();

		assertNotNull(run);
		assertTrue(run.isEmpty());

	}

	@Test
	public void testGetNextBPRunNoruninqueue() throws BPException {

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doReturn(Optional.empty()).when(mockStorage).nextQuequedBPRun();

		registry.setBPRunStorage(mockStorage);

		Optional<BPRun> run = registry.nextBPRun();

		assertNotNull(run);
		assertTrue(run.isEmpty());

		Mockito.verify(mockStorage, Mockito.times(0)).exists(Mockito.any());
	}

	@Test
	public void testRemoveInfo() throws BPException {

		String ownerEmail = "owner@mail.com";
		String requesterEmail = "requester@mail.com";

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage storage = Mockito.mock(IBPRunStorage.class);

		registry.setBPRunStorage(storage);

		String runid = "runid";

		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(requesterEmail).when(user).getEmail();

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(ownerEmail).when(run).getOwner();

		List<String> shared = new ArrayList<>();

		shared.add("shared1@mail.com");
		shared.add("shared2@mail.com");
		shared.add("shared3@mail.com");

		Mockito.doReturn(shared).when(run).getSharedWith();

		boolean publicRun = false;

		Mockito.doReturn(publicRun).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		List<BPInput> inputs = new ArrayList<>();
		BPInput input = Mockito.mock(BPInput.class);
		inputs.add(input);
		BPInput input2 = Mockito.mock(BPInput.class);
		inputs.add(input2);

		Mockito.doReturn(inputs).when(run).getInputs();

		String wfid = "wfid";
		Mockito.doReturn(wfid).when(run).getWorkflowid();

		String name = "name";
		Mockito.doReturn(name).when(run).getName();

		Mockito.doReturn(run).when(storage).get(Mockito.any(), Mockito.any());

		BPRun runFromRegistry = registry.get(runid, user);

		Assert.assertFalse(run.getSharedWith().isEmpty());

		Assert.assertTrue(runFromRegistry.getSharedWith().isEmpty());

		assertEquals(runid, runFromRegistry.getRunid());

		assertEquals(inputs.size(), runFromRegistry.getInputs().size());

		assertEquals(wfid, runFromRegistry.getWorkflowid());

		assertEquals(name, runFromRegistry.getName());

		assertEquals(publicRun, runFromRegistry.isPublicRun());
		assertEquals(null, runFromRegistry.getOwner());

	}

	@Test
	public void testRemoveInfo2() throws BPException {

		String ownerEmail = "owner@mail.com";
		String requesterEmail = "owner@mail.com";

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage storage = Mockito.mock(IBPRunStorage.class);

		registry.setBPRunStorage(storage);

		String runid = "runid";

		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(requesterEmail).when(user).getEmail();

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(ownerEmail).when(run).getOwner();

		List<String> shared = new ArrayList<>();

		shared.add("shared1@mail.com");
		shared.add("shared2@mail.com");
		shared.add("shared3@mail.com");

		Mockito.doReturn(shared).when(run).getSharedWith();

		boolean publicRun = false;

		Mockito.doReturn(publicRun).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		List<BPInput> inputs = new ArrayList<>();
		BPInput input = Mockito.mock(BPInput.class);
		inputs.add(input);
		BPInput input2 = Mockito.mock(BPInput.class);
		inputs.add(input2);

		Mockito.doReturn(inputs).when(run).getInputs();

		String wfid = "wfid";
		Mockito.doReturn(wfid).when(run).getWorkflowid();

		String name = "name";
		Mockito.doReturn(name).when(run).getName();

		Long ctime = 1L;
		Mockito.doReturn(ctime).when(run).getCreationTime();

		Mockito.doReturn(run).when(storage).get(Mockito.any(), Mockito.any());

		BPRun runFromRegistry = registry.get(runid, user);

		Assert.assertFalse(run.getSharedWith().isEmpty());

		assertEquals(0, runFromRegistry.getSharedWith().size());

		assertEquals(runid, runFromRegistry.getRunid());

		assertEquals(inputs.size(), runFromRegistry.getInputs().size());

		assertEquals(wfid, runFromRegistry.getWorkflowid());

		assertEquals(name, runFromRegistry.getName());

		assertEquals(ctime, runFromRegistry.getCreationTime());

		assertEquals(publicRun, runFromRegistry.isPublicRun());
		assertEquals(null, runFromRegistry.getOwner());

	}

	@Test
	public void testSearch() throws BPException {

		String ownerEmail = "owner@mail.com";
		String requesterEmail = "requester@mail.com";
		Integer start = 3;
		Integer count = 30;

		String wfid = "wfid";

		String searchtext = "searchtext";
		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage storage = Mockito.mock(IBPRunStorage.class);

		registry.setBPRunStorage(storage);

		String runid = "runid";

		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(requesterEmail).when(user).getEmail();

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(ownerEmail).when(run).getOwner();

		List<String> shared = new ArrayList<>();

		shared.add("shared1@mail.com");
		shared.add("shared2@mail.com");
		shared.add("shared3@mail.com");

		Mockito.doReturn(shared).when(run).getSharedWith();

		boolean publicRun = false;

		Mockito.doReturn(publicRun).when(run).isPublicRun();

		Mockito.doReturn(runid).when(run).getRunid();

		List<BPInput> inputs = new ArrayList<>();
		BPInput input = Mockito.mock(BPInput.class);
		inputs.add(input);
		BPInput input2 = Mockito.mock(BPInput.class);
		inputs.add(input2);

		Mockito.doReturn(inputs).when(run).getInputs();

		Mockito.doReturn(wfid).when(run).getWorkflowid();

		String name = "name";
		Mockito.doReturn(name).when(run).getName();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPUser u = (BPUser) invocation.getArguments()[0];

				String tt = (String) invocation.getArguments()[1];

				Integer ss = (Integer) invocation.getArguments()[2];

				Integer cc = (Integer) invocation.getArguments()[3];

				String id = (String) invocation.getArguments()[4];

				if (!requesterEmail.equalsIgnoreCase(u.getEmail()))
					throw new Exception("Bad user");

				if (start - ss != 0)
					throw new Exception("Bad start");

				if (count - cc != 0)
					throw new Exception("Bad count");

				if (!searchtext.equalsIgnoreCase(tt))
					throw new Exception("Bad search text");

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad wfid");

				BPRuns res = new BPRuns();

				res.setTotal(1);

				res.setRuns(Arrays.asList(run));
				return res;
			}
		}).when(storage).search(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		BPRuns results = registry.search(user, searchtext, start, count, wfid);

		Assert.assertFalse(run.getSharedWith().isEmpty());

		assertEquals((Integer) 1, results.getTotal());

		BPRun runFromRegistry = results.getRuns().get(0);

		Assert.assertTrue(runFromRegistry.getSharedWith().isEmpty());

		assertEquals(runid, runFromRegistry.getRunid());

		assertEquals(inputs.size(), runFromRegistry.getInputs().size());

		assertEquals(wfid, runFromRegistry.getWorkflowid());

		assertEquals(name, runFromRegistry.getName());

		assertEquals(publicRun, runFromRegistry.isPublicRun());
		assertEquals(null, runFromRegistry.getOwner());

	}

	@Test
	public void testDerefister() throws BPException {

		String requesterEmail = "requester@mail.com";
		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(requesterEmail).when(user).getEmail();

		BPRun run = Mockito.mock(BPRun.class);
		String runind = "runind";

		Mockito.doReturn(runind).when(run).getRunid();

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String r = ((BPRun) invocation.getArguments()[0]).getRunid();

				String mail = (String) invocation.getArguments()[1];

				if (!requesterEmail.equalsIgnoreCase(mail))
					throw new Exception(("Bad email"));

				if (!runind.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				return true;
			}
		}).when(mockStorage).remove(Mockito.any(), Mockito.any());

		registry.setBPRunStorage(mockStorage);

		assertTrue(registry.unregisterBPRun(run, user));

		Mockito.verify(mockStorage, Mockito.times(1)).remove(Mockito.any(), Mockito.any());
	}

	@Test
	public void testDerefister2() throws BPException {

		String requesterEmail = "requester@mail.com";
		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(requesterEmail).when(user).getEmail();

		BPRun run = Mockito.mock(BPRun.class);
		String runind = "runind";

		Mockito.doReturn(runind).when(run).getRunid();

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String r = ((BPRun) invocation.getArguments()[0]).getRunid();

				String mail = (String) invocation.getArguments()[1];

				if (!requesterEmail.equalsIgnoreCase(mail))
					throw new Exception(("Bad email"));

				if (!runind.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				return false;
			}
		}).when(mockStorage).remove(Mockito.any(), Mockito.any());

		registry.setBPRunStorage(mockStorage);

		assertFalse(registry.unregisterBPRun(run, user));

		Mockito.verify(mockStorage, Mockito.times(1)).remove(Mockito.any(), Mockito.any());
	}

	@Test
	public void testUdateRun() throws BPException {

		String requesterEmail = "requester@mail.com";
		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(requesterEmail).when(user).getEmail();

		BPRun run = Mockito.mock(BPRun.class);
		String runind = "runind";

		Mockito.doReturn(runind).when(run).getRunid();

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String r = ((BPRun) invocation.getArguments()[0]).getRunid();

				String mail = (String) invocation.getArguments()[1];

				if (!requesterEmail.equalsIgnoreCase(mail))
					throw new Exception(("Bad email"));

				if (!runind.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				return true;
			}
		}).when(mockStorage).udpdateRun(Mockito.any(), Mockito.any());

		registry.setBPRunStorage(mockStorage);

		assertTrue(registry.updateRun(run, user));

		Mockito.verify(mockStorage, Mockito.times(1)).udpdateRun(Mockito.any(), Mockito.any());
	}

	@Test
	public void testUpdateRun2() throws BPException {

		String requesterEmail = "requester@mail.com";
		BPUser user = Mockito.mock(BPUser.class);

		Mockito.doReturn(requesterEmail).when(user).getEmail();

		BPRun run = Mockito.mock(BPRun.class);
		String runind = "runind";

		Mockito.doReturn(runind).when(run).getRunid();

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String r = ((BPRun) invocation.getArguments()[0]).getRunid();

				String mail = (String) invocation.getArguments()[1];

				if (!requesterEmail.equalsIgnoreCase(mail))
					throw new Exception(("Bad email"));

				if (!runind.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				return false;
			}
		}).when(mockStorage).udpdateRun(Mockito.any(), Mockito.any());

		registry.setBPRunStorage(mockStorage);

		assertFalse(registry.updateRun(run, user));

		Mockito.verify(mockStorage, Mockito.times(1)).udpdateRun(Mockito.any(), Mockito.any());
	}

	@Test
	public void testExtendVis() throws BPException {

		Integer seconds = 20;
		BPRun run = Mockito.mock(BPRun.class);
		String runind = "runind";

		Mockito.doReturn(runind).when(run).getRunid();

		BPRunRegistry registry = new BPRunRegistry();
		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String r = ((BPRun) invocation.getArguments()[0]).getRunid();

				Integer s = (Integer) invocation.getArguments()[1];

				if (seconds - s != 0)
					throw new Exception(("Bad seconds"));

				if (!runind.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				return true;
			}
		}).when(mockStorage).extendVisibilityTimeout(Mockito.any(), Mockito.any());

		registry.setBPRunStorage(mockStorage);

		registry.extendVisibilityTimeout(run, seconds);

		Mockito.verify(mockStorage, Mockito.times(1)).extendVisibilityTimeout(Mockito.any(), Mockito.eq(seconds));
	}

	@Test
	public void testMoveToTriggered() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);
		String runind = "runind";

		Mockito.doReturn(runind).when(run).getRunid();

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String r = ((BPRun) invocation.getArguments()[0]).getRunid();

				if (!runind.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				return true;
			}
		}).when(mockStorage).moveToTriggered(Mockito.any());

		registry.setBPRunStorage(mockStorage);

		registry.moveToTriggered(run);

		Mockito.verify(mockStorage, Mockito.times(1)).moveToTriggered(Mockito.any());
	}

	@Test
	public void testMoveToTriggered2() throws BPException {

		BPRun run = Mockito.mock(BPRun.class);
		String runind = "runind";
		Mockito.doReturn(runind).when(run).getRunid();

		expectedException.expect(new BPExceptionMatcher("Unable to move " + run.getRunid() + " to triggered set.",
				BPException.ERROR_CODES.ERR_MOVE_TO_TRIGGERED));

		BPRunRegistry registry = new BPRunRegistry();

		IBPRunStorage mockStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String r = ((BPRun) invocation.getArguments()[0]).getRunid();

				if (!runind.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				return false;
			}
		}).when(mockStorage).moveToTriggered(Mockito.any());

		registry.setBPRunStorage(mockStorage);

		registry.moveToTriggered(run);

	}

}