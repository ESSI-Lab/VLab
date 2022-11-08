package eu.essi_lab.vlab.controller.executors.ingest;

import eu.essi_lab.vlab.controller.factory.IngestorFactory;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.controller.services.OutputIngestor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mattia Santoro
 */
public class ArrayInputManagerTest {

	private Logger logger = LoggerFactory.getLogger(ArrayInputManagerTest.class);

	@Test
	public void testSaveOutputArray() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());

		BPOutputDescription output = Mockito.mock(BPOutputDescription.class);

		BPOutput o = Mockito.mock(BPOutput.class);

		Mockito.doReturn(o).when(output).getOutput();

		String targetDir = "targetDir";

		String outputTarget = targetDir + "/";

		Mockito.when(output.getTarget()).thenReturn(outputTarget);

		String outid = "outid";

		Mockito.when(o.getId()).thenReturn(outid);

		String valueType = "value";

		Mockito.when(o.getValueType()).thenReturn(valueType);

		Mockito.doReturn("array").when(o).getOutputType();

		String expectedOutputFolderAbsolutePAth = vlabRootFolder + "/" + runid + "/" + outputTarget;

		String s3baseExpected = runid + "/" + output.getOutput().getId() + "/";
		execSaveOutputArrayTest(manager, output, runid, vlabRootFolder, expectedOutputFolderAbsolutePAth, s3baseExpected);

	}

	private void execSaveOutputArrayTest(ArrayInputManager manager, BPOutputDescription output, String runid, String rootFolder,
			String expectedOutputFolderAbsolutePAth, String expectedS3base) throws BPException {

		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPOutputDescription defin = ((BPOutputDescription) invocation.getArguments()[0]);

				return rootFolder + "/" + runid + "/" + output.getTarget();

			}

		}).when(parser).getDockerContainerAbsolutePath(Mockito.isA(BPOutputDescription.class));

		OutputIngestor outputIngestor = Mockito.mock(OutputIngestor.class);
		Mockito.doReturn(Boolean.TRUE).when(outputIngestor).canIngest(Mockito.any());
		IngestorFactory factory = mockFactory();
		Mockito.doReturn(Optional.of(outputIngestor)).when(factory).getIngestor((BPOutputDescription) Mockito.any());

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPOutputDescription o = (BPOutputDescription) invocation.getArguments()[0];

				if (!"outid".equalsIgnoreCase(o.getOutput().getId()))
					throw new Exception("Bad output id");

				String r = (String) invocation.getArguments()[1];
				if (!runid.equalsIgnoreCase(r))
					throw new Exception("Bad run id");

				if (null == invocation.getArguments()[2])
					throw new Exception("Expected non null executor");

				if (null == invocation.getArguments()[3])
					throw new Exception("Expected non null path parser");
				return saved;
			}

		}).when(outputIngestor).ingest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());


		Mockito.doReturn(factory).when(manager).getIngestorFactory();

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);
		ContainerOrchestratorCommandResult result = manager.save(output);

		assertTrue(result.isSuccess());

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type value</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultOnly() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultValueArray);

		Mockito.when(in.getValueSchema()).thenReturn("url");

		String defaultValuType = "value";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = null;

		List<Object> expectedTargets = new ArrayList<>();

		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);

		execTest(manager, defaultInput, input, defaultValueArray, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type keyValue</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultOnlyKeyValue() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		List<Object> defaultKeyValueArray = new ArrayList<>();

		defaultKeyValueArray.add("k1=defIn_1");
		defaultKeyValueArray.add("k2=defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultKeyValueArray);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = null;

		List<Object> expectedTargets = new ArrayList<>();
		expectedTargets.add(vlabRootFolder + "/k1");
		expectedTargets.add(vlabRootFolder + "/k2");
		Mockito.when(in.getValueSchema()).thenReturn("url");
		execTest(manager, defaultInput, input, defaultValueArray, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type value</li>
	 *         <li> User input, with value type value and replace valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultUserReplace() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultValueArray);

		String defaultValuType = "value";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn(defaultValuType);

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputValueArray);

		String interpretation = "replace";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expectedTargets = new ArrayList<>();

		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);
		Mockito.when(in.getValueSchema()).thenReturn("url");
		execTest(manager, defaultInput, input, inputValueArray, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type value</li>
	 *         <li> User input, with value type keyValue and replace valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultUserKeyValueReplace() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultValueArray);

		String defaultValuType = "value";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn("keyValue");

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		List<Object> inputKeyValueArray = new ArrayList<>();

		inputKeyValueArray.add("k1=userIn_1");
		inputKeyValueArray.add("k2=userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputKeyValueArray);

		String interpretation = "replace";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expectedTargets = new ArrayList<>();
		expectedTargets.add(vlabRootFolder + "/k1");
		expectedTargets.add(vlabRootFolder + "/k2");
		Mockito.when(in.getValueSchema()).thenReturn("url");
		execTest(manager, defaultInput, input, inputValueArray, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type value</li>
	 *         <li> User input, with value type value and extend valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultUserExtend() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultValueArray);

		String defaultValuType = "value";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn(defaultValuType);

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputValueArray);

		String interpretation = "extend";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expected = new ArrayList<>();

		expected.addAll(defaultValueArray);
		expected.addAll(inputValueArray);

		List<Object> expectedTargets = new ArrayList<>();

		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);
		Mockito.when(in.getValueSchema()).thenReturn("url");
		execTest(manager, defaultInput, input, expected, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type value</li>
	 *         <li> User input, with value type keyValue and extend valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultUserKeyValueExtend() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultValueArray);

		String defaultValuType = "value";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn("keyValue");

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		List<Object> inputKeyValueArray = new ArrayList<>();

		inputKeyValueArray.add("k1=userIn_1");
		inputKeyValueArray.add("k2=userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputKeyValueArray);

		String interpretation = "extend";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expected = new ArrayList<>();

		expected.addAll(defaultValueArray);
		expected.addAll(inputValueArray);
		List<Object> expectedTargets = new ArrayList<>();
		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder + "/k1");
		expectedTargets.add(vlabRootFolder + "/k2");
		Mockito.when(in.getValueSchema()).thenReturn("url");
		execTest(manager, defaultInput, input, expected, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type keyValue</li>
	 *         <li> User input, with value type value and extend valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultKeyValueUserExtend() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		List<Object> defaultKeyValueArray = new ArrayList<>();

		defaultKeyValueArray.add("k1=defIn_1");
		defaultKeyValueArray.add("k2=defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultKeyValueArray);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn("value");

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputValueArray);

		String interpretation = "extend";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expected = new ArrayList<>();

		expected.addAll(defaultValueArray);
		expected.addAll(inputValueArray);

		List<Object> expectedTargets = new ArrayList<>();
		expectedTargets.add(vlabRootFolder + "/k1");
		expectedTargets.add(vlabRootFolder + "/k2");
		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);
		Mockito.when(in.getValueSchema()).thenReturn("url");
		execTest(manager, defaultInput, input, expected, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type keyValue</li>
	 *         <li> User input, with value type keyValue and extend valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultKeyValueUserKeyValueExtend() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		List<Object> defaultKeyValueArray = new ArrayList<>();

		defaultKeyValueArray.add("k1=defIn_1");
		defaultKeyValueArray.add("k2=defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultKeyValueArray);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn("keyValue");

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		List<Object> inputKeyValueArray = new ArrayList<>();

		inputKeyValueArray.add("k3=userIn_1");
		inputKeyValueArray.add("k4=userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputKeyValueArray);

		String interpretation = "extend";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expected = new ArrayList<>();

		expected.addAll(defaultValueArray);
		expected.addAll(inputValueArray);

		List<Object> expectedTargets = new ArrayList<>();
		expectedTargets.add(vlabRootFolder + "/k1");
		expectedTargets.add(vlabRootFolder + "/k2");
		expectedTargets.add(vlabRootFolder + "/k3");
		expectedTargets.add(vlabRootFolder + "/k4");
		Mockito.when(in.getValueSchema()).thenReturn("url");
		execTest(manager, defaultInput, input, expected, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests with docker host failure for user inputs:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type keyValue</li>
	 *         <li> User input, with value type value and extend valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultKeyValueUserExtendDockerHostFailsUserInputs() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		List<Object> defaultKeyValueArray = new ArrayList<>();

		defaultKeyValueArray.add("k1=defIn_1");
		defaultKeyValueArray.add("k2=defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultKeyValueArray);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn("value");

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputValueArray);

		String interpretation = "extend";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expected = new ArrayList<>();

		expected.addAll(defaultValueArray);
		expected.addAll(inputValueArray);

		List<Object> expectedTargets = new ArrayList<>();
		expectedTargets.add(vlabRootFolder + "/k1");
		expectedTargets.add(vlabRootFolder + "/k2");
		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);

		execTestDockerHostFailsOnUserInputs(manager, defaultInput, input, expected, expectedTargets, vlabRootFolder);

	}

	/**
	 * This tests with docker host failure:
	 * <pre>
	 *     <ul>
	 *         <li> Only default input, with value type keyValue</li>
	 *         <li> User input, with value type value and extend valueArrayInterpretation</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultKeyValueUserExtendDockerHostFails() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		ArrayInputManager manager = Mockito.spy(new ArrayInputManager(runid));
		manager.setStatus(new BPRunStatus());
		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		List<Object> defaultValueArray = new ArrayList<>();

		defaultValueArray.add("defIn_1");
		defaultValueArray.add("defIn_2");

		List<Object> defaultKeyValueArray = new ArrayList<>();

		defaultKeyValueArray.add("k1=defIn_1");
		defaultKeyValueArray.add("k2=defIn_2");

		Mockito.when(defaultInput.getDefaultValueArray()).thenReturn(defaultKeyValueArray);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getValueType()).thenReturn("value");

		List<Object> inputValueArray = new ArrayList<>();

		inputValueArray.add("userIn_1");
		inputValueArray.add("userIn_2");

		Mockito.when(input.getValueArray()).thenReturn(inputValueArray);

		String interpretation = "extend";

		Mockito.when(input.getValueArrayInterpretation()).thenReturn(interpretation);

		List<Object> expected = new ArrayList<>();

		expected.addAll(defaultValueArray);
		expected.addAll(inputValueArray);

		List<Object> expectedTargets = new ArrayList<>();
		expectedTargets.add(vlabRootFolder + "/k1");
		expectedTargets.add(vlabRootFolder + "/k2");
		expectedTargets.add(vlabRootFolder);
		expectedTargets.add(vlabRootFolder);

		execTestDockerHostFails(manager, defaultInput, input, expected, expectedTargets, vlabRootFolder);

	}

	private void execTestDockerHostFails(ArrayInputManager m, BPInputDescription defaultInput, BPInput input,
			List<Object> expectedValuesTodownload, List<Object> expectedTargets, String root) throws BPException {

		IngestorFactory factory = mockFactory();

		IndividualInputIngestor inputIngestor = Mockito.mock(IndividualInputIngestor.class);
		Mockito.doReturn(Boolean.TRUE).when(inputIngestor).canIngest(Mockito.any());

		ArrayInputManager manager = Mockito.spy(m);

		Mockito.doReturn(factory).when(manager).getIngestorFactory();
		Mockito.doReturn(Optional.of(inputIngestor)).when(factory).getIngestor((BPInputDescription) Mockito.any());

		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String source = ((String) invocation.getArguments()[1]);

				if (source == null || "".equalsIgnoreCase(source))
					throw new Exception("Found null source to download.");

				System.out.println("Requested to download " + source);

				if (null == invocation.getArguments()[2])
					throw new Exception("Expected not null IContainerOrchestratorCommandExecutor");

				if (null == invocation.getArguments()[3])
					throw new Exception("Expected not null PathConventionParser");

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(false);
				return res;
			}
		}).when(inputIngestor).ingest(Mockito.isA(BPInputDescription.class), Mockito.any(),
				Mockito.isA(IContainerOrchestratorCommandExecutor.class), Mockito.isA(PathConventionParser.class));

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);
		ContainerOrchestratorCommandResult result = manager.ingest(defaultInput, input);

		assertFalse("False is expected due to docker host failure", result.isSuccess());

	}

	private void execTestDockerHostFailsOnUserInputs(ArrayInputManager m, BPInputDescription defaultInput, BPInput input,
			List<Object> expectedValuesTodownload, List<Object> expectedTargets, String root) throws BPException {

		IngestorFactory factory = mockFactory();

		IndividualInputIngestor inputIngestor = Mockito.mock(IndividualInputIngestor.class);
		Mockito.doReturn(Boolean.TRUE).when(inputIngestor).canIngest(Mockito.any());

		ArrayInputManager manager = Mockito.spy(m);

		Mockito.doReturn(factory).when(manager).getIngestorFactory();
		Mockito.doReturn(Optional.of(inputIngestor)).when(factory).getIngestor((BPInputDescription) Mockito.any());

		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		List<Object> expectedValueTodownload = copyList(expectedValuesTodownload);
		List<Object> expectedTargetValues = copyList(expectedTargets);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String source = ((String) invocation.getArguments()[1]);

				if (source == null || "".equalsIgnoreCase(source))
					throw new Exception("Found null source to download.");

				System.out.println("Requested to download " + source);

				if (source == null || "".equalsIgnoreCase(source))
					throw new Exception("Found null source to download.");

				boolean found = false;

				for (Object expected : expectedValueTodownload) {
					if (((String) expected).equalsIgnoreCase(source))
						found = true;
				}

				System.out.println(source + " was " + (found ? "" : "not ") + "found");

				if (found)
					expectedValueTodownload.remove(source);

				String target = ((String) invocation.getArguments()[1]);

				System.out.println("Requested download to " + target);

				if (target == null || "".equalsIgnoreCase(target))
					throw new Exception("Found null download target.");

				if (null == invocation.getArguments()[2])
					throw new Exception("Expected not null IContainerOrchestratorCommandExecutor");

				if (null == invocation.getArguments()[3])
					throw new Exception("Expected not null PathConventionParser");

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(false);
				return res;
			}
		}).when(inputIngestor).ingest(Mockito.isA(BPInputDescription.class), Mockito.any(),
				Mockito.isA(IContainerOrchestratorCommandExecutor.class), Mockito.isA(PathConventionParser.class));

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);
		ContainerOrchestratorCommandResult result = manager.ingest(defaultInput, input);

		assertFalse("False is expected due to docker host failure", result.isSuccess());

	}

	public IngestorFactory mockFactory() {

		IngestorFactory factory = Mockito.mock(IngestorFactory.class);

		return factory;

	}

	private void execTest(ArrayInputManager m, BPInputDescription defaultInput, BPInput input, List<Object> expectedValuesTodownload,
			List<Object> expectedTargets, String root) throws BPException {

		IngestorFactory factory = mockFactory();

		IndividualInputIngestor inputIngestor = Mockito.mock(IndividualInputIngestor.class);
		Mockito.doReturn(Boolean.TRUE).when(inputIngestor).canIngest(Mockito.any());

		ArrayInputManager manager = Mockito.spy(m);

		Mockito.doReturn(factory).when(manager).getIngestorFactory();
		Mockito.doReturn(Optional.of(inputIngestor)).when(factory).getIngestor((BPInputDescription) Mockito.any());

		IContainerOrchestratorCommandExecutor dockerHost = Mockito.mock(IContainerOrchestratorCommandExecutor.class);
		PathConventionParser parser = Mockito.mock(PathConventionParser.class);

		List<Object> expectedValueTodownload = copyList(expectedValuesTodownload);

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String source = ((String) invocation.getArguments()[1]);

				if (source == null || "".equalsIgnoreCase(source))
					throw new Exception("Found null source to download.");

				System.out.println("Requested to download " + source);

				if (source == null || "".equalsIgnoreCase(source))
					throw new Exception("Found null source to download.");

				boolean found = false;

				for (Object expected : expectedValueTodownload) {
					if (((String) expected).equalsIgnoreCase(source))
						found = true;
				}

				System.out.println(source + " was " + (found ? "" : "not ") + "found");

				if (found)
					expectedValueTodownload.remove(source);

				String target = ((String) invocation.getArguments()[1]);

				System.out.println("Requested download to " + target);

				if (target == null || "".equalsIgnoreCase(target))
					throw new Exception("Found null download target.");

				if (null == invocation.getArguments()[2])
					throw new Exception("Expected not null IContainerOrchestratorCommandExecutor");

				if (null == invocation.getArguments()[3])
					throw new Exception("Expected not null PathConventionParser");

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(true);
				return res;
			}
		}).when(inputIngestor).ingest(Mockito.isA(BPInputDescription.class), Mockito.any(),
				Mockito.isA(IContainerOrchestratorCommandExecutor.class), Mockito.isA(PathConventionParser.class));

		ContainerOrchestratorCommandResult result = manager.ingest(defaultInput, input);

		assertTrue(result.isSuccess());
		assertTrue(expectedValueTodownload.isEmpty());

	}

	private List<Object> copyList(List<Object> expectedValuesTodownload) {
		List<Object> ret = new ArrayList<>();

		for (Object s : expectedValuesTodownload)
			ret.add(s);

		return ret;
	}

}