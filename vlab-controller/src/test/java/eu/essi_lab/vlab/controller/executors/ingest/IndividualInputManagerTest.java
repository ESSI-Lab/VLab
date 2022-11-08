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
import eu.essi_lab.vlab.core.datamodel.BPRunRequest;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import eu.essi_lab.vlab.core.engine.utils.BPUtils;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import java.io.InputStream;
import java.util.Optional;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mattia Santoro
 */
public class IndividualInputManagerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private Logger logger = LoggerFactory.getLogger(IndividualInputManagerTest.class);

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

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		Mockito.doReturn(Mockito.mock(BPInput.class)).when(defaultInput).getInput();
		String defaultValue = "dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValue);

		String defaultValuType = "value";

		Mockito.when(defaultInput.getInput().getValueType()).thenReturn(defaultValuType);

		BPInput input = null;

		execTest(manager, defaultInput, input, defaultValue);

	}

	public IngestorFactory mockFactory() {

		IngestorFactory factory = Mockito.mock(IngestorFactory.class);

		return factory;

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

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		String defaultValueWithKey = "key=dafaultValue";
		String defaultValue = "dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = null;

		IngestorFactory factory = mockFactory();
		Mockito.doReturn(Optional.of(Mockito.mock(IndividualInputIngestor.class))).when(factory).getIngestor(
				(BPInputDescription) Mockito.any());

		execTest(manager, defaultInput, input, defaultValue);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Default input, with value type value</li>
	 *         <li> user input, with value type value</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultAndUserValue() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);

		String defaultValueWithKey = "key=dafaultValue";
		String defaultValue = "dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";
		BPInput dinput = Mockito.mock(BPInput.class);
		Mockito.when(defaultInput.getInput()).thenReturn(dinput);

		Mockito.when(dinput.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		String inputValue = "inputValue";

		Mockito.when(input.getValue()).thenReturn(inputValue);

		String inputValueType = "value";

		Mockito.when(input.getValueType()).thenReturn(inputValueType);
		IngestorFactory factory = mockFactory();
		Mockito.doReturn(Optional.of(Mockito.mock(IndividualInputIngestor.class))).when(factory).getIngestor(
				(BPInputDescription) Mockito.any());

		execTest(manager, defaultInput, input, inputValue);

	}

	/**
	 * This tests:
	 * <pre>
	 *     <ul>
	 *         <li> Default input, with value type value</li>
	 *         <li> user input, with value type keyValue</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDefaultAndUserKeyValue() throws BPException {

		String runid = "runid";
		String vlabRootFolder = "/rootfolder";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		String defaultValueWithKey = "key=dafaultValue";
		String defaultValue = "dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		BPInput input = Mockito.mock(BPInput.class);

		String inputValue = "inputValue";

		String inputKeyValue = "key=" + inputValue;
		Mockito.when(input.getValue()).thenReturn(inputKeyValue);

		String inputValueType = "keyValue";

		Mockito.when(input.getValueType()).thenReturn(inputValueType);

		IngestorFactory factory = mockFactory();
		Mockito.doReturn(Optional.of(Mockito.mock(IndividualInputIngestor.class))).when(factory).getIngestor(
				(BPInputDescription) Mockito.any());

		execTest(manager, defaultInput, input, inputValue);

	}

	@Test
	public void testNoInputSource() throws BPException {
		expectedException.expect(BPException.class);

		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.doReturn(true).when(input).getObligation();

		Mockito.doReturn(input).when(defaultInput).getInput();
		manager.ingest(defaultInput, input);
	}

	@Test
	public void testNoInputSourceOptional() throws BPException {

		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		Mockito.doReturn(Mockito.mock(BPInput.class)).when(defaultInput).getInput();

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.doReturn(false).when(input).getObligation();

		manager.ingest(defaultInput, input);
	}

	private IContainerOrchestratorCommandExecutor execTest(IndividualInputManager m, BPInputDescription defaultInput, BPInput input,
			String expectedValueTodownload) throws BPException {

		IngestorFactory factory = mockFactory();

		IndividualInputIngestor inputIngestor = Mockito.mock(IndividualInputIngestor.class);
		Mockito.doReturn(Boolean.TRUE).when(inputIngestor).canIngest(Mockito.any());

		IndividualInputManager manager = Mockito.spy(m);

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

				if (null == invocation.getArguments()[2])
					throw new Exception("Expected not null IContainerOrchestratorCommandExecutor");

				if (null == invocation.getArguments()[3])
					throw new Exception("Expected not null PathConventionParser");

				ContainerOrchestratorCommandResult res = new ContainerOrchestratorCommandResult();
				res.setSuccess(source.equalsIgnoreCase(expectedValueTodownload));
				return res;
			}
		}).when(inputIngestor).ingest(Mockito.isA(BPInputDescription.class), Mockito.any(),
				Mockito.isA(IContainerOrchestratorCommandExecutor.class), Mockito.isA(PathConventionParser.class));

		manager.setDockerHost(dockerHost);
		manager.setPathParser(parser);
		ContainerOrchestratorCommandResult result = manager.ingest(defaultInput, input);

		assertTrue(result.isSuccess());

		return dockerHost;

	}

	/**
	 * This tests the parameter case:
	 * <pre>
	 *     <ul>
	 *         <li> Default input, with value type KeyValue</li>
	 *         <li> user input, with value type keyValue</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testStringParamDefaultAndUserKeyValue() throws BPException {

		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		String defaultValueWithKey = "key=dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		String vlaueSchema = "string_parameter";

		Mockito.when(in.getValueSchema()).thenReturn(vlaueSchema);

		String inputid = "inputid";
		Mockito.when(in.getId()).thenReturn(inputid);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getId()).thenReturn(inputid);

		String inputValue = "six";

		String inputKeyValue = "key=" + inputValue;
		Mockito.when(input.getValue()).thenReturn(inputKeyValue);

		String inputValueType = "keyValue";

		Mockito.when(input.getValueType()).thenReturn(inputValueType);

		execTest(manager, defaultInput, input, inputValue);

	}

	/**
	 * This tests the parameter case:
	 * <pre>
	 *     <ul>
	 *         <li> Default input, with value type KeyValue</li>
	 *         <li> user input, with value type keyValue</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testBBoxParamDefaultAndUserKeyValue() throws BPException {

		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		String defaultValueWithKey = "key=dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		String vlaueSchema = "bbox";

		Mockito.when(in.getValueSchema()).thenReturn(vlaueSchema);

		String inputid = "inputid";
		Mockito.when(in.getId()).thenReturn(inputid);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getId()).thenReturn(inputid);

		String inputValue = "(west,south,east,north)";

		String inputKeyValue = "key=" + inputValue;
		Mockito.when(input.getValue()).thenReturn(inputKeyValue);

		String inputValueType = "keyValue";

		Mockito.when(input.getValueType()).thenReturn(inputValueType);

		execTest(manager, defaultInput, input, inputValue);

	}

	/**
	 * This tests the parameter case:
	 * <pre>
	 *     <ul>
	 *         <li> Default input, with value type KeyValue</li>
	 *         <li> user input, with value type keyValue</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testParamDefaultAndUserKeyValue() throws BPException {

		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		String defaultValueWithKey = "key=dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		String vlaueSchema = "number_parameter";

		Mockito.when(in.getValueSchema()).thenReturn(vlaueSchema);

		String inputid = "inputid";
		Mockito.when(in.getId()).thenReturn(inputid);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getId()).thenReturn(inputid);

		String inputValue = "6";

		String inputKeyValue = "key=" + inputValue;
		Mockito.when(input.getValue()).thenReturn(inputKeyValue);

		String inputValueType = "keyValue";

		Mockito.when(input.getValueType()).thenReturn(inputValueType);

		execTest(manager, defaultInput, input, inputValue);

	}

	/**
	 * This tests the parameter case:
	 * <pre>
	 *     <ul>
	 *         <li> Default input, with value type KeyValue</li>
	 *         <li> user input, with value type value</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testParamDefaultAndUserValue() throws BPException {

		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);

		String defaultValueWithKey = "key=dafaultValue";

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();

		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		String vlaueSchema = "number_parameter";

		Mockito.when(in.getValueSchema()).thenReturn(vlaueSchema);

		String inputid = "inputid";
		Mockito.when(in.getId()).thenReturn(inputid);

		BPInput input = Mockito.mock(BPInput.class);

		Mockito.when(input.getId()).thenReturn(inputid);

		String inputValue = "9.4";

		String inputKeyValue = "" + inputValue;
		Mockito.when(input.getValue()).thenReturn(inputKeyValue);

		String inputValueType = "value";

		Mockito.when(input.getValueType()).thenReturn(inputValueType);

		execTest(manager, defaultInput, input, inputValue);

	}

	/**
	 * This tests the parameter case:
	 * <pre>
	 *     <ul>
	 *         <li> Default input, with value type KeyValue</li>
	 *         <li> user input, with value type value but null</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testParamDefaultAndUserValueNull() throws BPException {

		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);

		String defVal = "3";
		String defaultValueWithKey = "key=" + defVal;

		Mockito.when(defaultInput.getDefaultValue()).thenReturn(defaultValueWithKey);

		String defaultValuType = "keyValue";
		BPInput in = Mockito.mock(BPInput.class);
		Mockito.doReturn(in).when(defaultInput).getInput();
		Mockito.when(in.getValueType()).thenReturn(defaultValuType);

		String vlaueSchema = "number_parameter";

		Mockito.when(in.getValueSchema()).thenReturn(vlaueSchema);

		String inputid = "inputid";
		Mockito.when(in.getId()).thenReturn(inputid);

		execTest(manager, defaultInput, null, defVal);

	}

	@Test
	public void tonumbertest() throws BPException {
		String runid = "runid";

		IndividualInputManager manager = new IndividualInputManager(runid);

		String s = "3";
		assertTrue(BPUtils.toNumber(s) instanceof Integer);

		s = "3.4";
		assertTrue(BPUtils.toNumber(s) instanceof Double);

		s = ".3";
		assertTrue(BPUtils.toNumber(s) instanceof Double);

		s = "37867868";
		assertTrue(BPUtils.toNumber(s) instanceof Integer);

		s = "3.098765432";
		assertTrue(BPUtils.toNumber(s) instanceof Double);

	}

	@Test
	public void nullInputTest() {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("nullInputs.json");

		BPRunRequest bprunRequest = null;

		try {

			bprunRequest = new JSONDeserializer().deserialize(stream, BPRunRequest.class);

			BPInput input3 = bprunRequest.getInputs().get(3);

			String val3 = (String) input3.getValue();

			assertNull(val3);

			IndividualInputManager manager = new IndividualInputManager("nullinputtestid");

			BPInputDescription defaultInput = Mockito.mock(BPInputDescription.class);

			String defaultValue = "(NDVI>0.3)";

			Mockito.doReturn(defaultValue).when(defaultInput).getDefaultValue();

			String defaultValueType = "key";

			BPInput input = Mockito.mock(BPInput.class);

			Mockito.doReturn(defaultValueType).when(input).getValueType();
			Mockito.doReturn(input).when(defaultInput).getInput();
			String readValue = manager.sourceValue(defaultInput, input3);

			assertEquals(defaultValue, readValue);

		} catch (BPException e) {

			logger.error("Error loading test json nullInputs.json");

			assertTrue("Error loading test json nullInputs.json", false);

		}

	}

	@Test
	public void testSave() throws BPException {

		OutputIngestor outputIngestor = Mockito.mock(OutputIngestor.class);
		Mockito.doReturn(Boolean.TRUE).when(outputIngestor).canIngest(Mockito.any());

		IngestorFactory factory = mockFactory();

		Mockito.doReturn(Optional.of(outputIngestor)).when(factory).getIngestor((BPOutputDescription) Mockito.any());

		String name = "oname";
		String o1id = "o1id";

		String runid = "runid";

		BPOutputDescription out1 = Mockito.mock(BPOutputDescription.class);
		BPOutput output = Mockito.mock(BPOutput.class);

		Mockito.doReturn(o1id).when(output).getId();

		Mockito.doReturn(name).when(output).getName();

		Mockito.doReturn("individual").when(output).getOutputType();

		Mockito.doReturn(output).when(out1).getOutput();

		ContainerOrchestratorCommandResult saved = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(saved).isSuccess();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				BPOutputDescription o = (BPOutputDescription) invocation.getArguments()[0];

				if (!o1id.equalsIgnoreCase(o.getOutput().getId()))
					throw new Exception("Bad output id");

				if (!name.equalsIgnoreCase(o.getOutput().getName()))
					throw new Exception("Bad output name");
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

		IndividualInputManager im = Mockito.spy(new IndividualInputManager(runid));

		im.setDockerHost(Mockito.mock(IContainerOrchestratorCommandExecutor.class));
		im.setPathParser(Mockito.mock(PathConventionParser.class));

		Mockito.doReturn(factory).when(im).getIngestorFactory();

		ContainerOrchestratorCommandResult result = im.save(out1);

		assertTrue(result.isSuccess());

	}
}