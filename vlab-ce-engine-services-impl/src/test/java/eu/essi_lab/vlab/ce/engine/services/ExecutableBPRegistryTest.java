package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResult;
import eu.essi_lab.vlab.core.datamodel.DeleteWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateWorkflowResponse;
import eu.essi_lab.vlab.core.engine.services.IBPWorkflowRegistryStorage;
import eu.essi_lab.vlab.core.utils.URLReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.junit.Assert;
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
public class ExecutableBPRegistryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testGetPublicRemoved() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.spy(new APIWorkflowDetail());

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		List<String> list = Arrays.asList(useremail);

		Mockito.doCallRealMethod().when(workflowDetail).getSharedWith();
		Mockito.doCallRealMethod().when(workflowDetail).setSharedWith(Mockito.any());

		Mockito.doCallRealMethod().when(workflowDetail).getModelDeveloperEmail();
		Mockito.doCallRealMethod().when(workflowDetail).setModelDeveloperEmail(Mockito.any());
		String owneremail = "owner";
		workflowDetail.setModelDeveloperEmail(owneremail);

		workflowDetail.setSharedWith(list);
		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String id = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad id: " + id);

				return workflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		APIWorkflowDetail wf = registry.getWorkflowDetail(wfid, user);

		Assert.assertNotNull(wf.getSharedWith());
		Assert.assertTrue(wf.getSharedWith().isEmpty());

		Assert.assertNull(wf.getModelDeveloperEmail());

		Mockito.verify(storage, Mockito.times(1)).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

	}

	@Test
	public void testGetUnauthorized() throws BPException, IOException {
		expectedException.expect(
				new BPExceptionMatcher("Tried to perform an unauthorized action on workflow", BPException.ERROR_CODES.NOT_AUTHORIZED));

		BPUser user = Mockito.mock(BPUser.class);

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.spy(new APIWorkflowDetail());

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = "owner";
		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperEmail();
		List<String> list = new ArrayList<>();

		Mockito.doCallRealMethod().when(workflowDetail).getSharedWith();
		Mockito.doCallRealMethod().when(workflowDetail).setSharedWith(Mockito.any());
		Mockito.doReturn(true).when(workflowDetail).isUnder_test();

		workflowDetail.setSharedWith(list);
		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String id = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad id: " + id);

				return workflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		APIWorkflowDetail wf = registry.getWorkflowDetail(wfid, user);

	}

	@Test
	public void testGetSharedwith() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = "owner";
		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperEmail();
		List<String> list = Arrays.asList(useremail);
		Mockito.doReturn(list).when(workflowDetail).getSharedWith();

		Mockito.doReturn(true).when(workflowDetail).isUnder_test();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String id = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad id: " + id);

				return workflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		Mockito.doReturn(stream).when(registry).readUrl(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String get = (String) invocation.getArguments()[0];

				if (!bpmnURL.equalsIgnoreCase(get))
					throw new Exception("Bad url: " + get);

				return stream;
			}

		}).when(registry).readUrl(Mockito.any());

		AtomicExecutableBP executable = registry.getExecutable(wfid, user);

		verifyExecutable(executable);

		Mockito.verify(storage, Mockito.times(1)).getAPIWorkflowDetail(Mockito.any(), Mockito.any());
		Mockito.verify(registry, Mockito.times(1)).readUrl(Mockito.any());

	}

	@Test
	public void testGet() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String useremail = "email";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		Mockito.doReturn(useremail).when(workflowDetail).getModelDeveloperEmail();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String id = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad id: " + id);

				return workflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		Mockito.doReturn(stream).when(registry).readUrl(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String get = (String) invocation.getArguments()[0];

				if (!bpmnURL.equalsIgnoreCase(get))
					throw new Exception("Bad url: " + get);

				return stream;
			}

		}).when(registry).readUrl(Mockito.any());

		AtomicExecutableBP executable = registry.getExecutable(wfid, user);

		verifyExecutable(executable);

		Mockito.verify(storage, Mockito.times(1)).getAPIWorkflowDetail(Mockito.any(), Mockito.any());
		Mockito.verify(registry, Mockito.times(1)).readUrl(Mockito.any());

	}

	private void verifyExecutable(AtomicExecutableBP executable) {
		Collection<ScriptTask> exTasks = executable.getExecutableTasks();

		assertTrue(exTasks.size() > 0);

		for (ScriptTask task : exTasks) {

			System.out.println(task.getName());

		}

		Collection<BPInput> inputs = executable.getInputs();

		assertTrue(inputs.size() > 0);

		assertTrue(inputs.size() == 2);

		for (BPInput input : inputs) {

			assertTrue(input.getId().equalsIgnoreCase("DataObject_1ogs25p") || input.getId().equalsIgnoreCase("DataObject_1wb6a70"));

			System.out.println(input.getId());
			System.out.println(input.getName());

		}

		Collection<BPOutput> outputs = executable.getOutputs();

		assertTrue(outputs.size() > 0);

		assertTrue(outputs.size() == 1);

		for (BPOutput output : outputs) {

			assertTrue(output.getId().equalsIgnoreCase("DataObject_0loj2kk"));

			System.out.println(output.getId());
			System.out.println(output.getName());

		}

		BPRealization realization = executable.getRealization();

		assertTrue(realization != null);

		assertTrue(realization.getRealizationURI().equalsIgnoreCase("http://example.com"));

	}

	@Test
	public void testGetIOEx() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String useremail = "email";
		Mockito.doReturn(useremail).when(user).getEmail();
		String bpmnURL = "bpmnURL";

		expectedException.expect(new BPExceptionMatcher("Error reading " + bpmnURL, BPException.ERROR_CODES.URL_READ_ERROR));

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();
		Mockito.doReturn(useremail).when(workflowDetail).getModelDeveloperEmail();
		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String id = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad id: " + id);

				return workflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		URLReader reader = Mockito.mock(URLReader.class);

		Mockito.doReturn(reader).when(registry).getReader();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String get = (String) invocation.getArguments()[0];

				if (!bpmnURL.equalsIgnoreCase(get))
					throw new Exception("Bad url: " + get);
				throw new IOException();
			}

		}).when(reader).read(Mockito.any());

		registry.getExecutable(wfid, user);

	}

	@Test
	public void testSearchSharedwithRemoved() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail_1 = new APIWorkflowDetail();
		String bpmnURL = "bpmnURL";
		workflowDetail_1.setBpmn_url(bpmnURL);
		String owneremail = "owner";
		workflowDetail_1.setModelDeveloperEmail(owneremail);
		List<String> list = Arrays.asList(useremail);
		workflowDetail_1.setSharedWith(list);

		APIWorkflowDetail workflowDetail_2 = new APIWorkflowDetail();
		workflowDetail_2.setBpmn_url(bpmnURL);
		workflowDetail_2.setModelDeveloperEmail(owneremail);
		workflowDetail_2.setSharedWith(list);

		APIWorkflowDetail workflowDetail_3 = new APIWorkflowDetail();
		workflowDetail_3.setBpmn_url(bpmnURL);
		workflowDetail_3.setModelDeveloperEmail(owneremail);
		workflowDetail_3.setSharedWith(list);

		SearchWorkflowsResponse r = Mockito.mock(SearchWorkflowsResponse.class);

		Mockito.doReturn(3).when(r).getTotal();

		List<APIWorkflowDetail> workflowlist = new ArrayList<>();
		workflowlist.add(workflowDetail_1);
		workflowlist.add(workflowDetail_2);
		workflowlist.add(workflowDetail_3);

		Mockito.doReturn(workflowlist).when(r).getWorkflows();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!text.equalsIgnoreCase(t))
					throw new Exception("Bad search text: " + t);

				Integer s = (Integer) invocation.getArguments()[1];

				if (0 != s)
					throw new Exception("Bad start");

				Integer c = (Integer) invocation.getArguments()[2];

				if (3 != c)
					throw new Exception("Bad count");

				Boolean ut = (Boolean) invocation.getArguments()[3];

				if (ut)
					throw new Exception("Bad under test");

				return r;
			}

		}).when(storage).searchWorkflowDetail(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		SearchWorkflowsResponse response = registry.search(text, 0, 3, false, user);

		Assert.assertEquals((Integer) 3, (Integer) response.getTotal());

		response.getWorkflows().forEach(w -> {
			Assert.assertNotNull(w.getSharedWith());
			Assert.assertTrue(w.getSharedWith().isEmpty());
			Assert.assertNull(w.getModelDeveloperEmail());
			Assert.assertFalse(w.isOwnedbyrequester());
			Assert.assertTrue(w.isSharedwithrequester());
		});

		Mockito.verify(storage, Mockito.times(1)).searchWorkflowDetail(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testCreateBadWFObj() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn(wfid).when(workflowDetail).getId();

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = "owner";
		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperEmail();

		CreateWorkflowResponse response = registry.createWorkflow(workflowDetail, user);

		Assert.assertEquals(CreateWorkflowResult.FAIL.toString(), response.getResult().toString());

		Assert.assertEquals("Workflow Developer Name is mandatory", response.getMessage());

		Mockito.verify(storage, Mockito.times(0)).storeWorkflowDetail(Mockito.any());

	}

	@Test
	public void testCreateBadWFObj2() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn(wfid).when(workflowDetail).getId();

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = "owner";
		Mockito.doReturn("").when(workflowDetail).getModelDeveloperEmail();

		CreateWorkflowResponse response = registry.createWorkflow(workflowDetail, user);

		Assert.assertEquals(CreateWorkflowResult.FAIL.toString(), response.getResult().toString());

		Assert.assertEquals("Workflow Developer Email is mandatory", response.getMessage());

		Mockito.verify(storage, Mockito.times(0)).storeWorkflowDetail(Mockito.any());

	}

	@Test
	public void testCreateBadUser() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn(wfid).when(workflowDetail).getId();

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = "owner";
		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperEmail();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloper();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperOrg();

		Mockito.doReturn(owneremail).when(workflowDetail).getName();

		CreateWorkflowResponse response = registry.createWorkflow(workflowDetail, user);

		Assert.assertEquals(CreateWorkflowResult.FAIL.toString(), response.getResult().toString());

		Assert.assertEquals("Only the owner of a workflow can request its storage", response.getMessage());

		Mockito.verify(storage, Mockito.times(0)).storeWorkflowDetail(Mockito.any());

	}

	@Test
	public void testCreateStroreok() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn(wfid).when(workflowDetail).getId();

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = useremail;
		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperEmail();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloper();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperOrg();

		Mockito.doReturn(owneremail).when(workflowDetail).getName();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				APIWorkflowDetail t = (APIWorkflowDetail) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t.getId()))
					throw new Exception("Bad workflow id");

				return true;
			}

		}).when(storage).storeWorkflowDetail(Mockito.any());

		CreateWorkflowResponse response = registry.createWorkflow(workflowDetail, user);

		Assert.assertEquals(CreateWorkflowResult.SUCCESS.toString(), response.getResult().toString());

		Assert.assertEquals("Successfully stored workflow wfid", response.getMessage());

		Mockito.verify(storage, Mockito.times(1)).storeWorkflowDetail(Mockito.any());

	}

	@Test
	public void testCreateStrorefail() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn(wfid).when(workflowDetail).getId();

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = useremail;
		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperEmail();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloper();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperOrg();

		Mockito.doReturn(owneremail).when(workflowDetail).getName();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				APIWorkflowDetail t = (APIWorkflowDetail) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t.getId()))
					throw new Exception("Bad workflow id");

				throw new BPException("");
			}

		}).when(storage).storeWorkflowDetail(Mockito.any());

		CreateWorkflowResponse response = registry.createWorkflow(workflowDetail, user);

		Assert.assertEquals(CreateWorkflowResult.FAIL.toString(), response.getResult().toString());

		Assert.assertEquals("An error occured during storage of workflow wfid (ex code: null)", response.getMessage());

		Mockito.verify(storage, Mockito.times(1)).storeWorkflowDetail(Mockito.any());

	}

	@Test
	public void testDeleteWF() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";
		APIWorkflowDetail realworkflowDetail = new APIWorkflowDetail();

		realworkflowDetail.setId(wfid);

		String bpmnURL = "bpmnURL";

		realworkflowDetail.setBpmn_url(bpmnURL);

		String owneremail = useremail;

		realworkflowDetail.setModelDeveloperEmail(owneremail);

		realworkflowDetail.setModelDeveloper(owneremail);

		realworkflowDetail.setModelDeveloperOrg(owneremail);

		realworkflowDetail.setName("existingname");

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t))
					throw new Exception("Bad workflow id");

				return realworkflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t))
					throw new Exception("Bad workflow id");

				return null;
			}

		}).when(storage).deleteWorkflowDetail(Mockito.any());

		DeleteWorkflowResponse response = registry.deleteWorkflow(wfid, user);

		Assert.assertEquals(wfid, response.getDeletedWorkflowId());

		Mockito.verify(storage, Mockito.times(1)).deleteWorkflowDetail(Mockito.any());
		Mockito.verify(storage, Mockito.times(1)).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

	}

	@Test
	public void testDeleteWFNotAuth() throws BPException, IOException {

		expectedException.expect(
				new BPExceptionMatcher("Tried to perform an unauthorized action on workflow", BPException.ERROR_CODES.NOT_AUTHORIZED));
		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";
		APIWorkflowDetail realworkflowDetail = new APIWorkflowDetail();

		realworkflowDetail.setId(wfid);

		String bpmnURL = "bpmnURL";

		realworkflowDetail.setBpmn_url(bpmnURL);

		String owneremail = "owner";

		realworkflowDetail.setModelDeveloperEmail(owneremail);

		realworkflowDetail.setModelDeveloper(owneremail);

		realworkflowDetail.setModelDeveloperOrg(owneremail);

		realworkflowDetail.setName("existingname");

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t))
					throw new Exception("Bad workflow id");

				return realworkflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		DeleteWorkflowResponse response = registry.deleteWorkflow(wfid, user);

	}

	@Test
	public void testUpdateWFNotAuth() throws BPException, IOException {

		expectedException.expect(
				new BPExceptionMatcher("Tried to perform an unauthorized action on workflow", BPException.ERROR_CODES.NOT_AUTHORIZED));
		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail realworkflowDetail = new APIWorkflowDetail();

		realworkflowDetail.setId(wfid);

		String bpmnURL = "bpmnURL";

		realworkflowDetail.setBpmn_url(bpmnURL);

		String owneremail = "owner";

		realworkflowDetail.setModelDeveloperEmail(owneremail);

		realworkflowDetail.setModelDeveloper(owneremail);

		realworkflowDetail.setModelDeveloperOrg(owneremail);

		realworkflowDetail.setName("existingname");

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t))
					throw new Exception("Bad workflow id");

				return realworkflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		APIWorkflowDetail newwf = Mockito.mock(APIWorkflowDetail.class);
		UpdateWorkflowResponse response = registry.updateWorkflow(wfid, newwf, user);

	}

	@Test
	public void testUpdateWFBadNewid() throws BPException, IOException {

		expectedException.expect(
				new BPExceptionMatcher("The workflow identifier can not be changed", BPException.ERROR_CODES.INVALID_REQUEST));

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail workflowDetail = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn(wfid).when(workflowDetail).getId();

		String bpmnURL = "bpmnURL";

		Mockito.doReturn(bpmnURL).when(workflowDetail).getBpmn_url();

		String owneremail = useremail;
		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperEmail();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloper();

		Mockito.doReturn(owneremail).when(workflowDetail).getModelDeveloperOrg();

		Mockito.doReturn("existingname").when(workflowDetail).getName();

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t))
					throw new Exception("Bad workflow id");

				return workflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				APIWorkflowDetail nwf = (APIWorkflowDetail) invocation.getArguments()[0];
				APIWorkflowDetail ewf = (APIWorkflowDetail) invocation.getArguments()[1];

				if (!"existingname".equalsIgnoreCase(ewf.getName()))
					throw new Exception("Bad existing workflow name");

				if (!"newname".equalsIgnoreCase(nwf.getName()))
					throw new Exception("Bad new workflow name");

				return null;
			}

		}).when(storage).updateWorkflowProperties(Mockito.any(), Mockito.any());

		APIWorkflowDetail newwf = Mockito.mock(APIWorkflowDetail.class);
		Mockito.doReturn("newid").when(newwf).getId();
		Mockito.doReturn("newname").when(newwf).getName();

		UpdateWorkflowResponse response = registry.updateWorkflow(wfid, newwf, user);

	}

	@Test
	public void testUpdateWF() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail realworkflowDetail = new APIWorkflowDetail();

		realworkflowDetail.setId(wfid);

		String bpmnURL = "bpmnURL";

		realworkflowDetail.setBpmn_url(bpmnURL);

		String owneremail = useremail;

		realworkflowDetail.setModelDeveloperEmail(owneremail);

		realworkflowDetail.setModelDeveloper(owneremail);

		realworkflowDetail.setModelDeveloperOrg(owneremail);

		realworkflowDetail.setName("existingname");

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t))
					throw new Exception("Bad workflow id");

				return realworkflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				APIWorkflowDetail nwf = (APIWorkflowDetail) invocation.getArguments()[0];
				APIWorkflowDetail ewf = (APIWorkflowDetail) invocation.getArguments()[1];

				if (!"existingname".equalsIgnoreCase(ewf.getName()))
					throw new Exception("Bad existing workflow name");

				if (!"newname".equalsIgnoreCase(nwf.getName()))
					throw new Exception("Bad new workflow name");

				return null;
			}

		}).when(storage).updateWorkflowProperties(Mockito.any(), Mockito.any());

		APIWorkflowDetail newwf = Mockito.mock(APIWorkflowDetail.class);
		Mockito.doReturn(wfid).when(newwf).getId();
		Mockito.doReturn("newname").when(newwf).getName();

		UpdateWorkflowResponse response = registry.updateWorkflow(wfid, newwf, user);

		Assert.assertEquals(wfid, response.getUpdatedWorkflowId());

		Mockito.verify(storage, Mockito.times(1)).getAPIWorkflowDetail(Mockito.any(), Mockito.any());
		Mockito.verify(storage, Mockito.times(1)).updateWorkflowProperties(Mockito.any(), Mockito.any());

	}

	@Test
	public void testUpdateWFWithShared() throws BPException, IOException {

		BPUser user = Mockito.mock(BPUser.class);

		String text = "text";

		String useremail = "useremail";
		Mockito.doReturn(useremail).when(user).getEmail();

		ExecutableBPRegistry registry = Mockito.spy(new ExecutableBPRegistry());

		IBPWorkflowRegistryStorage storage = Mockito.mock(IBPWorkflowRegistryStorage.class);
		registry.setBPExecutableStorage(storage);

		String wfid = "wfid";

		APIWorkflowDetail realworkflowDetail = new APIWorkflowDetail();

		realworkflowDetail.setId(wfid);

		String bpmnURL = "bpmnURL";

		realworkflowDetail.setBpmn_url(bpmnURL);

		String owneremail = useremail;

		realworkflowDetail.setModelDeveloperEmail(owneremail);

		realworkflowDetail.setModelDeveloper(owneremail);

		realworkflowDetail.setModelDeveloperOrg(owneremail);

		realworkflowDetail.setName("existingname");

		List<String> l1 = new ArrayList<>();
		l1.add("user1");

		realworkflowDetail.setSharedWith(l1);

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!wfid.equalsIgnoreCase(t))
					throw new Exception("Bad workflow id");

				return realworkflowDetail;
			}

		}).when(storage).getAPIWorkflowDetail(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				APIWorkflowDetail nwf = (APIWorkflowDetail) invocation.getArguments()[0];
				APIWorkflowDetail ewf = (APIWorkflowDetail) invocation.getArguments()[1];

				if (!"existingname".equalsIgnoreCase(ewf.getName()))
					throw new Exception("Bad existing workflow name");

				if (!"newname".equalsIgnoreCase(nwf.getName()))
					throw new Exception("Bad new workflow name");

				if (!nwf.getSharedWith().contains("user1"))
					throw new Exception("missing user1");

				if (!nwf.getSharedWith().contains("user2"))
					throw new Exception("missing user");

				if (nwf.getModelDeveloperEmail() == null)
					throw new Exception("Missing email");

				return null;
			}

		}).when(storage).updateWorkflowProperties(Mockito.any(), Mockito.any());

		APIWorkflowDetail newwf = new APIWorkflowDetail();

		newwf.setId(wfid);

		newwf.setBpmn_url(bpmnURL);

		newwf.setModelDeveloper(owneremail);

		newwf.setModelDeveloperOrg(owneremail);

		newwf.setName("newname");

		List<String> l2 = new ArrayList<>();
		l2.add("user2");

		newwf.setSharedWith(l2);

		UpdateWorkflowResponse response = registry.updateWorkflow(wfid, newwf, user);

		Assert.assertEquals(wfid, response.getUpdatedWorkflowId());

		Mockito.verify(storage, Mockito.times(1)).getAPIWorkflowDetail(Mockito.any(), Mockito.any());
		Mockito.verify(storage, Mockito.times(1)).updateWorkflowProperties(Mockito.any(), Mockito.any());

	}

}