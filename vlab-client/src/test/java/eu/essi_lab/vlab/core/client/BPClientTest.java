package eu.essi_lab.vlab.core.client;

import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPMNConventionParser;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunRequest;
import eu.essi_lab.vlab.core.datamodel.BPRunResult;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.BPWorkflowInputsResponse;
import eu.essi_lab.vlab.core.datamodel.BPWorkflowOutputsResponse;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResult;
import eu.essi_lab.vlab.core.datamodel.DeleteRunResponse;
import eu.essi_lab.vlab.core.datamodel.DeleteWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateRunResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.engine.BPPreprocessor;
import eu.essi_lab.vlab.core.engine.factory.BPRegistriesFactory;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BPClientTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testGetOutputs() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		assertNotNull(stream);

		AtomicExecutableBP executable = new AtomicExecutableBP(stream, new BPMNConventionParser());

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		BPRegistriesFactory factory = Mockito.mock(BPRegistriesFactory.class);
		IExecutableBPRegistry executableRegistry = Mockito.mock(IExecutableBPRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return executable;
			}
		}).when(executableRegistry).getExecutable(Mockito.any(), Mockito.any());

		Mockito.doReturn(executableRegistry).when(factory).getExecutableRegistry();

		Mockito.doReturn(factory).when(action).getFactory();

		BPWorkflowOutputsResponse response = action.getBPOutputs(wfid, Mockito.mock(BPUser.class));

		Assert.assertEquals((Integer) 1, (Integer) response.getOutputs().size());

		Assert.assertEquals("DataObject_0loj2kk", response.getOutputs().get(0).getId());

		Mockito.verify(factory, Mockito.times(1)).getExecutableRegistry();
		Mockito.verify(executableRegistry, Mockito.times(1)).getExecutable(Mockito.any(), Mockito.any());
	}

	@Test
	public void testGetInputs() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		assertNotNull(stream);

		AtomicExecutableBP executable = new AtomicExecutableBP(stream, new BPMNConventionParser());

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		BPRegistriesFactory factory = Mockito.mock(BPRegistriesFactory.class);
		IExecutableBPRegistry executableRegistry = Mockito.mock(IExecutableBPRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return executable;
			}
		}).when(executableRegistry).getExecutable(Mockito.any(), Mockito.any());

		Mockito.doReturn(executableRegistry).when(factory).getExecutableRegistry();

		Mockito.doReturn(factory).when(action).getFactory();

		BPWorkflowInputsResponse response = action.getBPInputs(wfid, Mockito.mock(BPUser.class));

		Assert.assertEquals((Integer) 2, (Integer) response.getInputs().size());

		Assert.assertEquals("DataObject_1wb6a70", response.getInputs().get(0).getId());
		Assert.assertEquals("DataObject_1ogs25p", response.getInputs().get(1).getId());

		Mockito.verify(factory, Mockito.times(1)).getExecutableRegistry();
		Mockito.verify(executableRegistry, Mockito.times(1)).getExecutable(Mockito.any(), Mockito.any());
	}

	@Test
	public void testGetInputs2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("inputTest.bpmn");

		assertNotNull(stream);

		AtomicExecutableBP executable = new AtomicExecutableBP(stream, new BPMNConventionParser());

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		BPRegistriesFactory factory = Mockito.mock(BPRegistriesFactory.class);
		IExecutableBPRegistry executableRegistry = Mockito.mock(IExecutableBPRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return executable;
			}
		}).when(executableRegistry).getExecutable(Mockito.any(), Mockito.any());

		Mockito.doReturn(executableRegistry).when(factory).getExecutableRegistry();

		Mockito.doReturn(factory).when(action).getFactory();

		BPWorkflowInputsResponse response = action.getBPInputs(wfid, Mockito.mock(BPUser.class));

		Assert.assertEquals((Integer) 9, (Integer) response.getInputs().size());

		Assert.assertEquals("p1_s2a", response.getInputs().get(0).getId());

		Assert.assertEquals("url", response.getInputs().get(0).getValueSchema());

		Mockito.verify(factory, Mockito.times(1)).getExecutableRegistry();
		Mockito.verify(executableRegistry, Mockito.times(1)).getExecutable(Mockito.any(), Mockito.any());

		String jsonResponse = new JSONSerializer().serialize(response);
		Assert.assertEquals("{\"inputs\":[{\"valueType\":\"value\",\"description\":\"Sentinel 2A scene (https://USER:PWD@scihub"
						+ ".copernicus.eu/dhus/odata/v1/Products('4c74f791-6c89-4760-9cda-285cc49f2603')/$value)\",\"name\":\"Period 1 - "
						+ "Sentinel 2A scene\",\"id\":\"p1_s2a\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\","
						+ "\"obligation\":true,\"hasDefault\":false,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,"
						+ "\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"Sentinel 2A scene "
						+ "(https://USER:PWD@scihub.copernicus.eu/dhus/odata/v1/Products('4c74f791-6c89-4760-9cda-285cc49f2603')/$value)"
						+ "\",\"name\":\"Period 2 - Sentinel 2A scene\",\"id\":\"p2_s2a\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"url\",\"obligation\":true,\"hasDefault\":false,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"bbox\",\"name\":\"Bounding box\",\"id\":\"bbox\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"bbox\",\"obligation\":true,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"Indexes (comma-separated list)\",\"name\":\"Indexes (comma-separated list)\",\"id\":\"indexes\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"string_parameter\",\"obligation\":false,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"Aquatic\",\"name\":\"Aquatic\",\"id\":\"aquatic_wat_cat\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"string_parameter\",\"obligation\":false,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"Artificial water\",\"name\":\"Artificial water\",\"id\":\"artwatr_wat_cat\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"string_parameter\",\"obligation\":false,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"Vegetation\",\"name\":\"Vegetation\",\"id\":\"vegetat_veg_cat\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"string_parameter\",\"obligation\":false,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"Artificial Surface\",\"name\":\"Artificial Surface\",\"id\":\"artific_urb_cat\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"string_parameter\",\"obligation\":false,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null},{\"valueType\":\"value\",\"description\":\"Cultivated\",\"name\":\"Cultivated\",\"id\":\"cultman_agr_cat\",\"value\":null,\"valueArray\":null,\"valueSchema\":\"string_parameter\",\"obligation\":false,\"hasDefault\":true,\"inputType\":\"individual\",\"valueArrayInterpretation\":null,\"valueKey\":null,\"valueKeyArray\":null}]}",
				jsonResponse);
	}

	@Test
	public void testGetOutputbyid2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		assertNotNull(stream);

		AtomicExecutableBP executable = new AtomicExecutableBP(stream, new BPMNConventionParser());

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		BPRegistriesFactory factory = Mockito.mock(BPRegistriesFactory.class);
		IExecutableBPRegistry executableRegistry = Mockito.mock(IExecutableBPRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return executable;
			}
		}).when(executableRegistry).getExecutable(Mockito.any(), Mockito.any());

		Mockito.doReturn(executableRegistry).when(factory).getExecutableRegistry();

		Mockito.doReturn(factory).when(action).getFactory();

		BPOutput response = action.getBPOutput(wfid, "DataObject_0loj2kk", Mockito.mock(BPUser.class));

		Assert.assertEquals("DataObject_0loj2kk", response.getId());

		Mockito.verify(factory, Mockito.times(1)).getExecutableRegistry();
		Mockito.verify(executableRegistry, Mockito.times(1)).getExecutable(Mockito.any(), Mockito.any());
	}

	@Test
	public void testGetOutputbyid() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		String oid = "oid";

		List<BPOutput> list = new ArrayList<>();

		BPOutput o1 = Mockito.mock(BPOutput.class);
		String o1id = "nomatch";
		Mockito.doReturn(o1id).when(o1).getId();
		list.add(o1);

		BPOutput o2 = Mockito.mock(BPOutput.class);
		String o2id = "oid";
		Mockito.doReturn(o2id).when(o2).getId();
		list.add(o2);
		BPWorkflowOutputsResponse r = Mockito.mock(BPWorkflowOutputsResponse.class);
		Mockito.doReturn(list).when(r).getOutputs();
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return r;
			}
		}).when(action).getBPOutputs(Mockito.any(), Mockito.any());

		BPOutput response = action.getBPOutput(wfid, oid, Mockito.mock(BPUser.class));

		Assert.assertEquals(oid, response.getId());
		Mockito.verify(action, Mockito.times(1)).getBPOutputs(Mockito.any(), Mockito.any());
	}

	@Test
	public void testGetOutputbyidNotfound() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Can't find requested object ", BPException.ERROR_CODES.RESOURCE_NOT_FOUND));

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		String oid = "oid";

		List<BPOutput> list = new ArrayList<>();

		BPOutput o1 = Mockito.mock(BPOutput.class);
		String o1id = "nomatch";
		Mockito.doReturn(o1id).when(o1).getId();
		list.add(o1);

		BPOutput o2 = Mockito.mock(BPOutput.class);
		String o2id = "nomatch";
		Mockito.doReturn(o2id).when(o2).getId();
		list.add(o2);

		BPWorkflowOutputsResponse r = Mockito.mock(BPWorkflowOutputsResponse.class);
		Mockito.doReturn(list).when(r).getOutputs();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return r;
			}
		}).when(action).getBPOutputs(Mockito.any(), Mockito.any());

		action.getBPOutput(wfid, oid, Mockito.mock(BPUser.class));

	}

	@Test
	public void testGetIntputbyid2() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		assertNotNull(stream);

		AtomicExecutableBP executable = new AtomicExecutableBP(stream, new BPMNConventionParser());

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		BPRegistriesFactory factory = Mockito.mock(BPRegistriesFactory.class);
		IExecutableBPRegistry executableRegistry = Mockito.mock(IExecutableBPRegistry.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return executable;
			}
		}).when(executableRegistry).getExecutable(Mockito.any(), Mockito.any());

		Mockito.doReturn(executableRegistry).when(factory).getExecutableRegistry();

		Mockito.doReturn(factory).when(action).getFactory();

		BPInput response = action.getBPInput(wfid, "DataObject_1ogs25p", Mockito.mock(BPUser.class));

		Assert.assertEquals("DataObject_1ogs25p", response.getId());

		Mockito.verify(factory, Mockito.times(1)).getExecutableRegistry();
		Mockito.verify(executableRegistry, Mockito.times(1)).getExecutable(Mockito.any(), Mockito.any());
	}

	@Test
	public void testGetIntputbyid() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		String oid = "oid";

		List<BPInput> list = new ArrayList<>();

		BPInput o1 = Mockito.mock(BPInput.class);
		String o1id = "nomatch";
		Mockito.doReturn(o1id).when(o1).getId();
		list.add(o1);

		BPInput o2 = Mockito.mock(BPInput.class);
		String o2id = "oid";
		Mockito.doReturn(o2id).when(o2).getId();
		list.add(o2);
		BPWorkflowInputsResponse r = Mockito.mock(BPWorkflowInputsResponse.class);
		Mockito.doReturn(list).when(r).getInputs();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return r;
			}
		}).when(action).getBPInputs(Mockito.any(), Mockito.any());

		BPInput response = action.getBPInput(wfid, oid, Mockito.mock(BPUser.class));

		Assert.assertEquals(oid, response.getId());
		Mockito.verify(action, Mockito.times(1)).getBPInputs(Mockito.any(), Mockito.any());
	}

	@Test
	public void testGetInputbyidNotfound() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Can't find requested object ", BPException.ERROR_CODES.RESOURCE_NOT_FOUND));

		BPClient action = Mockito.spy(new BPClient());

		String wfid = "wfid";
		String oid = "oid";

		List<BPInput> list = new ArrayList<>();

		BPInput o1 = Mockito.mock(BPInput.class);
		String o1id = "nomatch";
		Mockito.doReturn(o1id).when(o1).getId();
		list.add(o1);

		BPInput o2 = Mockito.mock(BPInput.class);
		String o2id = "nomatch";
		Mockito.doReturn(o2id).when(o2).getId();
		list.add(o2);
		BPWorkflowInputsResponse r = Mockito.mock(BPWorkflowInputsResponse.class);
		Mockito.doReturn(list).when(r).getInputs();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String i = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(i))
					throw new Exception("Bad workflow id");

				return r;
			}
		}).when(action).getBPInputs(Mockito.any(), Mockito.any());

		action.getBPInput(wfid, oid, Mockito.mock(BPUser.class));

	}

	@Test
	public void testUpdateRun() throws BPException, IOException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		String runtext = new JSONSerializer().serialize(run);

		Mockito.when(mockBPRunRegistry.updateRun(Mockito.any(), Mockito.any())).thenReturn(true);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);
		Object response = action.updateRun(testrunid, new JSONObject(runtext), user);

		assertEquals(testrunid, ((UpdateRunResponse) response).getUpdatedRunId());

	}

	@Test
	public void testUpdateRunFailsWithRunidMismatch() throws BPException, IOException {
		expectedException.expect(new BPExceptionMatcher("Run identifier mismatch: path id is testrunid2 but body run id is testrunid",
				BPException.ERROR_CODES.INVALID_REQUEST));

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		String runtext = new JSONSerializer().serialize(run);

		Mockito.when(mockBPRunRegistry.updateRun(Mockito.any(), Mockito.any())).thenReturn(false);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);

		action.updateRun(testrunid + "2", new JSONObject(runtext), user);

	}

	@Test
	public void testUpdateRunFailsWithUnparsebleBody() throws BPException, IOException {
		expectedException.expect(new BPExceptionMatcher("Error parsing BPRun to be updated", BPException.ERROR_CODES.INVALID_REQUEST));

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		ValidateRealizationRequest req = new ValidateRealizationRequest();

		req.setModelName("model");

		String runtext = new JSONSerializer().serialize(req);

		Mockito.when(mockBPRunRegistry.updateRun(Mockito.any(), Mockito.any())).thenReturn(false);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);

		action.updateRun(testrunid, new JSONObject(runtext), user);

	}

	@Test
	public void testUpdateRunFailsWithNoException() throws BPException, IOException {
		expectedException.expect(
				new BPExceptionMatcher("BPRunRegistry returned false on updateRun for testrunid", BPException.ERROR_CODES.UNKNOWN));

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		String runtext = new JSONSerializer().serialize(run);

		Mockito.when(mockBPRunRegistry.updateRun(Mockito.any(), Mockito.any())).thenReturn(false);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);

		action.updateRun(testrunid, new JSONObject(runtext), user);

	}

	@Test
	public void testGetRun() throws BPException {
		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		Mockito.when(mockBPRunRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);

		Object response = action.getRun(testrunid, user);

		assertEquals(testrunid, ((BPRun) response).getRunid());

	}

	@Test
	public void testGetStatus() throws BPException {
		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		IBPRunStatusRegistry statusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(testrunid).when(status).getRunid();

		Mockito.doReturn(statusRegistry).when(mockFactory).getBPRunStatusRegistry();

		Mockito.doReturn(status).when(statusRegistry).getBPRunStatus(Mockito.any());

		Mockito.when(mockBPRunRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPRunStatus response = action.getStatus(testrunid);

		assertEquals(testrunid, response.getRunid());

	}

	@Test
	public void testSearchRuns() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		BPRuns runs = new BPRuns();

		runs.setRuns(Arrays.asList(run));

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		Mockito.doReturn(runs).when(mockBPRunRegistry).search(Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.any());

		BPUser user = Mockito.mock(BPUser.class);

		BPRuns response = action.searchRuns("", 0, 3, user, null);

		assertEquals(runs.getRuns().size(), ((BPRuns) response).getRuns().size());

	}

	@Test
	public void testDeleteRun() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		Mockito.when(mockBPRunRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);

		Mockito.when(mockBPRunRegistry.unregisterBPRun(Mockito.any(), Mockito.any())).thenReturn(true);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);
		Object response = action.deleteRun(testrunid, user);

		assertEquals(testrunid, ((DeleteRunResponse) response).getDeletedRunId());

	}

	@Test
	public void testDeleteRunFailsWithNoException() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("BPRunRegistry returned false on deregisterBPRun for testrunid", BPException.ERROR_CODES.UNKNOWN));

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		Mockito.when(mockBPRunRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);

		Mockito.when(mockBPRunRegistry.unregisterBPRun(Mockito.any(), Mockito.any())).thenReturn(false);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);
		Object response = action.deleteRun(testrunid, user);

		assertEquals(testrunid, ((DeleteRunResponse) response).getDeletedRunId());

	}

	@Test
	public void testCreateRun() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		List<BPInput> inputs = new ArrayList<>();
		BPInput in = new BPInput();
		String inid = "inid";
		in.setId(inid);
		inputs.add(in);
		run.setInputs(inputs);

		BPRunRequest runRequest = new BPRunRequest();

		runRequest.setInputs(inputs);

		Mockito.when(mockBPRunRegistry.registerBPRun(Mockito.any())).thenReturn(run);

		Mockito.when(mockBPRunRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);
		String workflowid = "workflowid";

		Object response = action.createRun(workflowid, new JSONObject(new JSONSerializer().serialize(runRequest)), user);

		assertEquals(testrunid, ((BPRun) response).getRunid());

		assertEquals(run.getInputs().size(), ((BPRun) response).getInputs().size());

		assertEquals(run.getInputs(), ((BPRun) response).getInputs());

	}

	@Test
	public void testCreateRunWithInfra() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		List<BPInput> inputs = new ArrayList<>();
		BPInput in = new BPInput();
		String inid = "inid";
		in.setId(inid);
		inputs.add(in);
		run.setInputs(inputs);

		BPRunRequest runRequest = new BPRunRequest();

		runRequest.setInputs(inputs);

		runRequest.setInfra("infra");

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPRun r = (BPRun) invocation.getArguments()[0];

				if (null == r.getExecutionInfrastructure() || !"infra".equalsIgnoreCase(r.getExecutionInfrastructure()))
					throw new Exception("Expected execution infrastructure in run, found " + r.getExecutionInfrastructure());

				return run;
			}
		}).when(mockBPRunRegistry).registerBPRun(Mockito.any());

		Mockito.when(mockBPRunRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);
		String workflowid = "workflowid";

		Object response = action.createRun(workflowid, new JSONObject(new JSONSerializer().serialize(runRequest)), user);

		assertEquals(testrunid, ((BPRun) response).getRunid());

		assertEquals(run.getInputs().size(), ((BPRun) response).getInputs().size());

		assertEquals(run.getInputs(), ((BPRun) response).getInputs());

	}

	@Test
	public void testCreateRunFilsUnparsable() throws BPException {

		expectedException.expect(new BPExceptionMatcher("Error parsing BPRunRequest: ", BPException.ERROR_CODES.INVALID_REQUEST));

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunRegistry mockBPRunRegistry = Mockito.mock(IBPRunRegistry.class);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		Mockito.when(mockBPRunRegistry.registerBPRun(Mockito.any())).thenReturn(run);

		Mockito.when(mockBPRunRegistry.get(Mockito.any(), Mockito.any())).thenReturn(run);

		Mockito.when(mockFactory.getBPRunRegistry()).thenReturn(mockBPRunRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);
		String workflowid = "workflowid";

		action.createRun(workflowid, new JSONObject(new JSONSerializer().serialize(new ValidateRealizationRequest())), user);

	}

	@Test
	public void testGetLogs() throws BPException {

		BPClient action = Mockito.spy(new BPClient());
		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IBPRunLogRegistry logRegistry = Mockito.mock(IBPRunLogRegistry.class);

		Mockito.doReturn(logRegistry).when(mockFactory).getBPRunLogRegistry();

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPRun run = new BPRun();
		String testrunid = "testrunid";
		run.setRunid(testrunid);

		BPUser user = Mockito.mock(BPUser.class);
		String runid = "runid";

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(runid).when(status).getRunid();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!runid.equals(id))
					throw new Exception("Bad run id");

				return status;
			}
		}).when(action).getStatus(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!runid.equals(id))
					throw new Exception("Bad run id");

				return run;
			}
		}).when(action).getRun(Mockito.any(), Mockito.any());

		String nextToken = "nextToken";

		LogMessagesResponse logMessagesResponse = Mockito.mock(LogMessagesResponse.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				BPRunStatus st = (BPRunStatus) invocationOnMock.getArguments()[0];

				if (!runid.equals(st.getRunid()))
					throw new Exception("Bad runid");

				if ((Boolean) invocationOnMock.getArguments()[1])
					throw new Exception("Bad head");

				String t = (String) invocationOnMock.getArguments()[2];

				if (!nextToken.equals(t))
					throw new Exception("Bad token");

				return logMessagesResponse;
			}
		}).when(logRegistry).getLogs(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		LogMessagesResponse response = action.getLogs(runid, false, nextToken, user);

		Assert.assertNotNull(response);

	}

	@Test
	public void testValidateJson() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		ValidateRealizationRequest validateRequest = new ValidateRealizationRequest();

		ValidateRealizationResponse resp = Mockito.mock(ValidateRealizationResponse.class);

		Mockito.doReturn(resp).when(action).validateRealization((ValidateRealizationRequest) Mockito.any());

		ValidateRealizationResponse response = action.validateRealization(new JSONObject(new JSONSerializer().serialize(validateRequest)));

		Assert.assertNotNull(response);
		Mockito.verify(action, Mockito.times(1)).validateRealization((ValidateRealizationRequest) Mockito.any());
	}

	@Test
	public void testValidateJsonUnparsable() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("Error parsing ValidateRealizationRequest: ", BPException.ERROR_CODES.INVALID_REQUEST));

		BPClient action = Mockito.spy(new BPClient());

		BPRunRequest validateRequest = new BPRunRequest();

		ValidateRealizationResponse resp = Mockito.mock(ValidateRealizationResponse.class);

		Mockito.doReturn(resp).when(action).validateRealization((ValidateRealizationRequest) Mockito.any());

		action.validateRealization(new JSONObject(new JSONSerializer().serialize(validateRequest)));

	}

	@Test
	public void testValidate() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		ValidateRealizationRequest validateRequest = new ValidateRealizationRequest();

		ValidateRealizationResponse resp = Mockito.mock(ValidateRealizationResponse.class);

		Mockito.doReturn(Boolean.TRUE).when(resp).getValid();

		BPPreprocessor preprocessor = Mockito.mock(BPPreprocessor.class);
		Mockito.doReturn(resp).when(preprocessor).execValidation(Mockito.any());

		Mockito.doReturn(preprocessor).when(action).getPreprocessor();

		ValidateRealizationResponse response = action.validateRealization(validateRequest);

		Assert.assertNotNull(response);
		Assert.assertTrue(response.getValid());
		Mockito.verify(preprocessor, Mockito.times(1)).execValidation(Mockito.any());
	}

	@Test
	public void testSearchWF() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);

		SearchWorkflowsResponse workflowsresponse = Mockito.mock(SearchWorkflowsResponse.class);

		Mockito.doReturn(4).when(workflowsresponse).getTotal();

		Mockito.when(mockFactory.getExecutableRegistry()).thenReturn(executableBPRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!"search".equalsIgnoreCase(t))
					throw new Exception("Bad search text: " + t);

				Integer s = (Integer) invocation.getArguments()[1];

				if (0 != s)
					throw new Exception("Bad start");

				Integer c = (Integer) invocation.getArguments()[2];

				if (3 != c)
					throw new Exception("Bad count");

				Boolean ut = (Boolean) invocation.getArguments()[3];

				if (!ut)
					throw new Exception("Bad under test");

				return workflowsresponse;
			}
		}).when(executableBPRegistry).search(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		BPUser user = Mockito.mock(BPUser.class);

		SearchWorkflowsResponse response = action.searchWorkflows("search", 0, 3, true, user);

		assertEquals((Integer) 4, (Integer) response.getTotal());
		Mockito.verify(executableBPRegistry, Mockito.times(1)).search(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	@Test
	public void testGetWF() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);

		APIWorkflowDetail workflowsresponse = Mockito.mock(APIWorkflowDetail.class);

		Mockito.doReturn("ident").when(workflowsresponse).getId();

		Mockito.when(mockFactory.getExecutableRegistry()).thenReturn(executableBPRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String t = (String) invocation.getArguments()[0];

				if (!"search".equalsIgnoreCase(t))
					throw new Exception("Bad search text: " + t);

				return workflowsresponse;
			}
		}).when(executableBPRegistry).getWorkflowDetail(Mockito.any(), Mockito.any());

		BPUser user = Mockito.mock(BPUser.class);

		APIWorkflowDetail response = action.getWorkflowDetail("search", user);

		assertEquals("ident", response.getId());
		Mockito.verify(executableBPRegistry, Mockito.times(1)).getWorkflowDetail(Mockito.any(), Mockito.any());

	}

	@Test
	public void testCreateWF() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);

		CreateWorkflowResponse workflowsresponse = Mockito.mock(CreateWorkflowResponse.class);

		Mockito.doReturn(CreateWorkflowResult.SUCCESS).when(workflowsresponse).getResult();

		Mockito.when(mockFactory.getExecutableRegistry()).thenReturn(executableBPRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);

		String uid = "uid";
		Mockito.doReturn(uid).when(user).getEmail();

		APIWorkflowDetail workflow = Mockito.mock(APIWorkflowDetail.class);
		String wid = "wid";
		Mockito.doReturn(wid).when(workflow).getId();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				APIWorkflowDetail wf = (APIWorkflowDetail) invocation.getArguments()[0];

				BPUser u = (BPUser) invocation.getArguments()[1];

				if (!wid.equalsIgnoreCase(wf.getId()))
					throw new Exception("Bad workflowid: " + wf.getId());

				if (!uid.equalsIgnoreCase(u.getEmail()))
					throw new Exception("Bad workflowid: " + u.getEmail());

				return workflowsresponse;
			}
		}).when(executableBPRegistry).createWorkflow(Mockito.any(), Mockito.any());

		CreateWorkflowResponse response = action.createWorkflowDetail(workflow, user);

		assertEquals("SUCCESS", response.getResult().toString());
		Mockito.verify(executableBPRegistry, Mockito.times(1)).createWorkflow(Mockito.any(), Mockito.any());

	}

	@Test
	public void testDeleteWF() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);

		DeleteWorkflowResponse workflowsresponse = Mockito.mock(DeleteWorkflowResponse.class);
		String wid = "wid";
		Mockito.doReturn(wid).when(workflowsresponse).getDeletedWorkflowId();

		Mockito.when(mockFactory.getExecutableRegistry()).thenReturn(executableBPRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);

		String uid = "uid";
		Mockito.doReturn(uid).when(user).getEmail();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String wf = (String) invocation.getArguments()[0];

				BPUser u = (BPUser) invocation.getArguments()[1];

				if (!wid.equalsIgnoreCase(wf))
					throw new Exception("Bad workflowid: " + wf);

				if (!uid.equalsIgnoreCase(u.getEmail()))
					throw new Exception("Bad workflowid: " + u.getEmail());

				return workflowsresponse;
			}
		}).when(executableBPRegistry).deleteWorkflow(Mockito.any(), Mockito.any());

		DeleteWorkflowResponse response = action.deleteWorkflow(wid, user);

		assertEquals(wid, response.getDeletedWorkflowId());
		Mockito.verify(executableBPRegistry, Mockito.times(1)).deleteWorkflow(Mockito.any(), Mockito.any());

	}

	@Test
	public void testUpdateWF() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		BPRegistriesFactory mockFactory = Mockito.mock(BPRegistriesFactory.class);

		IExecutableBPRegistry executableBPRegistry = Mockito.mock(IExecutableBPRegistry.class);

		UpdateWorkflowResponse workflowsresponse = Mockito.mock(UpdateWorkflowResponse.class);
		String wid = "wid";
		Mockito.doReturn(wid).when(workflowsresponse).getUpdatedWorkflowId();

		Mockito.when(mockFactory.getExecutableRegistry()).thenReturn(executableBPRegistry);

		Mockito.when(action.getFactory()).thenReturn(mockFactory);

		BPUser user = Mockito.mock(BPUser.class);

		APIWorkflowDetail workflow = Mockito.mock(APIWorkflowDetail.class);

		String uid = "uid";
		Mockito.doReturn(uid).when(user).getEmail();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String wf = (String) invocation.getArguments()[0];

				BPUser u = (BPUser) invocation.getArguments()[2];

				if (!wid.equalsIgnoreCase(wf))
					throw new Exception("Bad workflowid: " + wf);

				if (!uid.equalsIgnoreCase(u.getEmail()))
					throw new Exception("Bad workflowid: " + u.getEmail());

				return workflowsresponse;
			}
		}).when(executableBPRegistry).updateWorkflow(Mockito.any(), Mockito.any(), Mockito.any());

		UpdateWorkflowResponse response = action.updateWorkflow(wid, workflow, user);

		assertEquals(wid, response.getUpdatedWorkflowId());
		Mockito.verify(executableBPRegistry, Mockito.times(1)).updateWorkflow(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testGetRunOutputs() throws BPException {

		BPClient client = Mockito.spy(new BPClient());

		String runid = "runid";

		String wfid = "wfid";
		String oid = "oid";

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(wfid).when(run).getWorkflowid();

		Mockito.doReturn(run).when(client).getRun(Mockito.any(), Mockito.any());

		BPWorkflowOutputsResponse outputresponse = Mockito.mock(BPWorkflowOutputsResponse.class);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(BPRunStatuses.COMPLETED.toString()).when(status).getStatus();
		Mockito.doReturn(BPRunResult.SUCCESS.toString()).when(status).getResult();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!runid.equalsIgnoreCase(id))
					throw new Exception("Bad runid id");

				return status;
			}
		}).when(client).getStatus(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad workflow id");

				return outputresponse;
			}
		}).when(client).getBPOutputs(Mockito.any(), Mockito.any());

		List<BPOutput> list = new ArrayList<>();

		BPOutput o1 = Mockito.mock(BPOutput.class);
		String o1id = "nomatch1";
		Mockito.doReturn("url").when(o1).getValueSchema();
		Mockito.doReturn(o1id).when(o1).getId();
		Mockito.doCallRealMethod().when(o1).setValue(Mockito.any());
		Mockito.doCallRealMethod().when(o1).getValue();
		list.add(o1);

		BPOutput o2 = Mockito.mock(BPOutput.class);
		String o2id = "nomatch2";
		Mockito.doReturn("url").when(o2).getValueSchema();
		Mockito.doReturn(o2id).when(o2).getId();

		Mockito.doCallRealMethod().when(o2).setValue(Mockito.any());
		Mockito.doCallRealMethod().when(o2).getValue();
		list.add(o2);

		Mockito.doReturn(list).when(outputresponse).getOutputs();

		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);

		Mockito.doReturn(iWebStorage).when(client).getWebStorage(Mockito.any());
		IBPOutputWebStorage ibpoutputWebStorage = Mockito.mock(IBPOutputWebStorage.class);

		Mockito.doReturn(ibpoutputWebStorage).when(client).getBPOutputWebStorag(Mockito.any());

		BPWorkflowOutputsResponse resp = client.getBPRunOutputs(runid, Mockito.mock(BPUser.class));

		Assert.assertEquals((Integer) 2, (Integer) resp.getOutputs().size());

		Assert.assertEquals(o1id, resp.getOutputs().get(0).getId());
		Assert.assertEquals(o2id, resp.getOutputs().get(1).getId());

		Mockito.verify(ibpoutputWebStorage, Mockito.times(2)).addValue(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testGetRunOutputs1Ex() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		String runid = "runid";

		String wfid = "wfid";
		String oid = "oid";

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(wfid).when(run).getWorkflowid();

		Mockito.doReturn(run).when(action).getRun(Mockito.any(), Mockito.any());

		BPWorkflowOutputsResponse outputresponse = Mockito.mock(BPWorkflowOutputsResponse.class);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(BPRunStatuses.COMPLETED.toString()).when(status).getStatus();
		Mockito.doReturn(BPRunResult.SUCCESS.toString()).when(status).getResult();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!runid.equalsIgnoreCase(id))
					throw new Exception("Bad runid id");

				return status;
			}
		}).when(action).getStatus(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad workflow id");

				return outputresponse;
			}
		}).when(action).getBPOutputs(Mockito.any(), Mockito.any());

		List<BPOutput> list = new ArrayList<>();

		BPOutput o1 = Mockito.mock(BPOutput.class);
		String o1id = "nomatch1";
		Mockito.doReturn(o1id).when(o1).getId();
		list.add(o1);

		BPOutput o2 = Mockito.mock(BPOutput.class);
		String o2id = "nomatch2";
		Mockito.doReturn(o2id).when(o2).getId();
		list.add(o2);

		Mockito.doReturn(list).when(outputresponse).getOutputs();

		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);

		Mockito.doReturn(iWebStorage).when(action).getWebStorage(Mockito.any());

		Mockito.doThrow(BPException.class).doNothing().when(action).setOutputValue(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		BPWorkflowOutputsResponse resp = action.getBPRunOutputs(runid, Mockito.mock(BPUser.class));

		Assert.assertEquals((Integer) 2, (Integer) resp.getOutputs().size());

		Assert.assertEquals(o1id, resp.getOutputs().get(0).getId());
		Assert.assertEquals(o2id, resp.getOutputs().get(1).getId());

		Mockito.verify(action, Mockito.times(2)).setOutputValue(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

	}

	@Test
	public void testGetRunOutput() throws BPException {

		BPClient action = Mockito.spy(new BPClient());

		String runid = "runid";

		String wfid = "wfid";
		String oid = "oid";

		BPRun run = Mockito.mock(BPRun.class);

		Mockito.doReturn(runid).when(run).getRunid();

		Mockito.doReturn(wfid).when(run).getWorkflowid();

		Mockito.doReturn(run).when(action).getRun(Mockito.any(), Mockito.any());

		BPWorkflowOutputsResponse outputresponse = Mockito.mock(BPWorkflowOutputsResponse.class);

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		Mockito.doReturn(BPRunStatuses.COMPLETED.toString()).when(status).getStatus();
		Mockito.doReturn(BPRunResult.SUCCESS.toString()).when(status).getResult();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!runid.equalsIgnoreCase(id))
					throw new Exception("Bad runid id");

				return status;
			}
		}).when(action).getStatus(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!wfid.equalsIgnoreCase(id))
					throw new Exception("Bad workflow id");

				return outputresponse;
			}
		}).when(action).getBPOutputs(Mockito.any(), Mockito.any());

		List<BPOutput> list = new ArrayList<>();

		BPOutput o1 = Mockito.mock(BPOutput.class);
		String o1id = "nomatch";
		Mockito.doReturn(o1id).when(o1).getId();
		list.add(o1);

		BPOutput o2 = Mockito.mock(BPOutput.class);
		String o2id = "oid";
		Mockito.doReturn(o2id).when(o2).getId();
		list.add(o2);

		Mockito.doReturn(list).when(outputresponse).getOutputs();

		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);

		Mockito.doReturn(iWebStorage).when(action).getWebStorage(Mockito.any());

		Mockito.doThrow(BPException.class).doNothing().when(action).setOutputValue(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		BPOutput resp = action.getBPRunOutput(runid, oid, Mockito.mock(BPUser.class));

		Assert.assertEquals(o2id, resp.getId());

		Mockito.verify(action, Mockito.times(2)).setOutputValue(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

	}
}