package eu.essi_lab.vlab.core.engine;

import eu.essi_lab.vlab.core.engine.conventions.ISourceCodeConventionFileLoaderMock;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessage;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import eu.essi_lab.vlab.core.engine.bpmn.BPModelGenerator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.AdapterAvailableRealizationValidator;
import eu.essi_lab.vlab.core.engine.validator.filter.impl.AdapterFound;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class BPPreprocessorTest {

	private File cloneDir = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());

	@Test
	public void testExecValidateNonValidRealization() throws BPException {

		String uri = "test";

		BPPreprocessor pre = new BPPreprocessor();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(uri);

		request.setRealization(realization);

		BPPreprocessor preprocessor = Mockito.spy(pre);

		ValidateRealizationResponse response = new ValidateRealizationResponse();
		response.setValid(false);

		Mockito.doReturn(response).when(preprocessor).validateRealization(request);

		ValidateRealizationResponse r = preprocessor.execValidation(request);

		assertFalse("Expected Not Valid", r.getValid());
	}

	@Before
	public void emptyGitCloneDirectory() throws IOException {
		emptyFolder(cloneDir);
	}

	private void emptyFolder(File dir) {

		File[] files = dir.listFiles();

		if (files == null)
			return;

		for (File file : files) {
			if (file.isDirectory())
				emptyFolder(file);

			file.delete();

		}

	}

	@Test
	public void testExecValidateValidRealization() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		BPPreprocessor pre = Mockito.spy(new BPPreprocessor());

		Mockito.doReturn("http://example.bpmn/id").when(pre).publishDiagram(Mockito.any(), Mockito.any());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(pre).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(pre).instantiateGenerator();
		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(generator)
				.getISourceCodeConventionFileLoader(Mockito.any());
		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		request.setModelDeveloper("Developer");

		request.setModelDeveloperEmail("Developer@mail.com");
		request.setModelDeveloperOrg("Developer Org");

		ValidateRealizationResponse r = pre.execValidation(request);

		assertTrue("Expected Valid", r.getValid());

		assertTrue(r.getWorkflow().getId().startsWith("http://eu.essi_lab.vlab.core/workflow/autogenerated"));

		assertEquals("No name provided", r.getWorkflow().getName());

		assertEquals("No description provided", r.getWorkflow().getDescription());

		assertEquals("http://example.bpmn/id", r.getWorkflow().getBpmn_url());

		assertEquals("Developer", r.getWorkflow().getModelDeveloper());

		assertEquals("Developer@mail.com", r.getWorkflow().getModelDeveloperEmail());

		assertEquals("Developer Org", r.getWorkflow().getModelDeveloperOrg());

	}

	private AdapterAvailableRealizationValidator mockAdapterAvailableRealizationValidator(URL dirUrl) throws BPException {

		AdapterAvailableRealizationValidator validator = Mockito.spy(new AdapterAvailableRealizationValidator());

		AdapterFound found = new AdapterFound();
		found.setAdapter(mockSCConnector(dirUrl));
		Mockito.doReturn(found).when(validator).findAdapter(Mockito.any());
		Mockito.doReturn(found.getAdapter()).when(validator).getAdapter();
		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(validator)
				.getLoader();

		return validator;
	}

	private AdapterAvailableRealizationValidator mockAdapterAvailableRealizationValidatorAdapterError(URL dirUrl) {

		AdapterAvailableRealizationValidator validator = Mockito.spy(new AdapterAvailableRealizationValidator());

		AdapterFound found = new AdapterFound();
		found.setMsg("Can't find VLab/dockerImage.json");
		Mockito.doReturn(found).when(validator).findAdapter(Mockito.any());

		return validator;
	}

	private ISourceCodeConnector mockSCConnector(URL root) {

		ISourceCodeConnector sourceCodeConnector = Mockito.mock(ISourceCodeConnector.class);

		Mockito.doReturn(new File(root.getPath())).when(sourceCodeConnector).getDir();

		return sourceCodeConnector;
	}

	@Test
	public void testExecValidateValidRealization2() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		BPPreprocessor pre = Mockito.spy(new BPPreprocessor());

		Mockito.doReturn("http://example.bpmn/id").when(pre).publishDiagram(Mockito.any(), Mockito.any());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(pre).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(pre).instantiateGenerator();

		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(generator)
				.getISourceCodeConventionFileLoader(Mockito.any());

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);
		request.setModelName("Model Name");
		request.setModelDeveloper("Developer");

		request.setModelDeveloperEmail("Developer@mail.com");
		request.setModelDeveloperOrg("Developer Org");

		ValidateRealizationResponse r = pre.execValidation(request);

		assertTrue("Expected Valid", r.getValid());

		assertTrue(r.getWorkflow().getId().startsWith("http://eu.essi_lab.vlab.core/workflow/autogenerated"));

		assertEquals("Model Name", r.getWorkflow().getName());

		assertEquals("No description provided", r.getWorkflow().getDescription());

		assertEquals("http://example.bpmn/id", r.getWorkflow().getBpmn_url());

		assertEquals("Developer", r.getWorkflow().getModelDeveloper());

		assertEquals("Developer@mail.com", r.getWorkflow().getModelDeveloperEmail());

		assertEquals("Developer Org", r.getWorkflow().getModelDeveloperOrg());

	}

	@Test
	public void testExecValidateValidRealization3() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		BPPreprocessor pre = Mockito.spy(new BPPreprocessor());

		Mockito.doReturn("http://example.bpmn/id").when(pre).publishDiagram(Mockito.any(), Mockito.any());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(pre).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(pre).instantiateGenerator();
		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(generator)
				.getISourceCodeConventionFileLoader(Mockito.any());
		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);
		request.setModelName("Model Name");

		request.setModelDescription("Model Desc");
		request.setModelDeveloper("Developer");

		request.setModelDeveloperEmail("Developer@mail.com");
		request.setModelDeveloperOrg("Developer Org");

		ValidateRealizationResponse r = pre.execValidation(request);

		assertTrue("Expected Valid", r.getValid());

		assertTrue(r.getWorkflow().getId().startsWith("http://eu.essi_lab.vlab.core/workflow/autogenerated"));

		assertEquals("Model Name", r.getWorkflow().getName());

		assertEquals("Model Desc", r.getWorkflow().getDescription());

		assertEquals("http://example.bpmn/id", r.getWorkflow().getBpmn_url());

		assertEquals("Developer", r.getWorkflow().getModelDeveloper());

		assertEquals("Developer@mail.com", r.getWorkflow().getModelDeveloperEmail());

		assertEquals("Developer Org", r.getWorkflow().getModelDeveloperOrg());

	}

	@Test
	public void testInvalidJSONFiles() throws BPException {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidJson/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(3, response.getMessages().size());

		response.getMessages().forEach(m -> {

			if (!m.getMessage().contains("Unparsable: Unexpected")) {
				assertFalse("Unexpected error message " + m.getMessage(), true);
			}

			if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
				assertFalse("Unexpected message type " + m.getType(), true);
			}

		});

	}

	@Test
	public void testMissingResources() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelMissingResources/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(1, response.getMessages().size());

		ValidationMessage m = response.getMessages().get(0);

		if (!m.getMessage().contains("Bad VLab convention file found " + BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME
				+ " -> Missing mandatory field \'resources\' (NOTE " + "that " + "at "
				+ "least one of resources.cpu_units or resources.memory_mb must be present)")) {
			assertFalse("Unexpected error message " + m.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
			assertFalse("Unexpected message type " + m.getType(), true);
		}

	}

	@Test
	public void testMissingResourceFields() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelMissingResourceFields/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(1, response.getMessages().size());

		ValidationMessage m = response.getMessages().get(0);

		if (!m.getMessage().contains(
				"Bad VLab convention file found " + BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME + " -> The \'resources\' field is not"
						+ " correctly set: at " + "least one of resources.cpu_units or resources.memory_mb must be present")) {
			assertFalse("Unexpected error message " + m.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
			assertFalse("Unexpected message type " + m.getType(), true);
		}

	}

	@Test
	public void testInvalidMissingFile() throws BPException {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidMissingFile/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidatorAdapterError(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(1, response.getMessages().size());

		ValidationMessage m = response.getMessages().get(0);

		if (!m.getMessage().contains("Can't find VLab/dockerImage.json")) {
			assertFalse("Unexpected error message " + m.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
			assertFalse("Unexpected message type " + m.getType(), true);
		}

	}

	@Test
	public void testInvalidOneJson() throws BPException {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidOneJson/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(1, response.getMessages().size());

		ValidationMessage m = response.getMessages().get(0);

		if (!m.getMessage().contains("Unparsable: Unexpected character")) {
			assertFalse("Unexpected error message " + m.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
			assertFalse("Unexpected message type " + m.getType(), true);
		}

	}

	@Test
	public void testInvalidSameInputsId() throws BPException {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidSameInputsId/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(1, response.getMessages().size());

		ValidationMessage m = response.getMessages().get(0);

		if (!m.getMessage().contains("Found duplicate input id with id: DataObject_1wb6a70")) {
			assertFalse("Unexpected error message " + m.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
			assertFalse("Unexpected message type " + m.getType(), true);
		}

	}

	@Test
	public void testInvalidSameOutputsId() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidSameOutputsId/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(1, response.getMessages().size());

		ValidationMessage m = response.getMessages().get(0);

		if (!m.getMessage().contains("Found duplicate output id with id: iDataObject_0loj2kk ")) {
			assertFalse("Unexpected error message " + m.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
			assertFalse("Unexpected message type " + m.getType(), true);
		}

	}

	@Test
	public void testInvalidSameInputsOutputsId() throws BPException {

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidSameInputsOutputsId/");

		BPPreprocessor preprocessor = Mockito.spy(new BPPreprocessor());

		AdapterAvailableRealizationValidator adapterValidator = mockAdapterAvailableRealizationValidator(dirUrl);
		Mockito.doReturn(adapterValidator).when(preprocessor).instantiateAdapterValidator();
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());
		Mockito.doReturn(mockSCConnector(dirUrl)).when(generator).getAdapter(Mockito.any());
		Mockito.doReturn(generator).when(preprocessor).instantiateGenerator();

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(dirUrl.getFile());

		request.setRealization(realization);

		String modelName = "Model Name";

		request.setModelName(modelName);

		String modelDescription = "Model Description";
		request.setModelDescription(modelDescription);

		ValidateRealizationResponse response = preprocessor.execValidation(request);

		assertFalse("Expected Invalid", response.getValid());

		assertEquals(2, response.getMessages().size());

		ValidationMessage m = response.getMessages().get(0);

		if (!m.getMessage().contains("Found duplicate input id with id: DataObject_1wb6a70")) {
			assertFalse("Unexpected error message " + m.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m.getType()) != 0) {
			assertFalse("Unexpected message type " + m.getType(), true);
		}

		ValidationMessage m2 = response.getMessages().get(1);

		if (!m2.getMessage().contains("Found duplicate output id with id: iDataObject_0loj2kk")) {
			assertFalse("Unexpected error message " + m2.getMessage(), true);
		}

		if (ValidationMessageType.ERROR.compareTo(m2.getType()) != 0) {
			assertFalse("Unexpected message type " + m2.getType(), true);
		}

	}

}