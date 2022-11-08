package eu.essi_lab.vlab.core.engine.bpmn;

import eu.essi_lab.vlab.core.engine.conventions.ISourceCodeConventionFileLoaderMock;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class BPModelGeneratorTest {

	private ISourceCodeConnector mockSCConnector(URL root) {

		ISourceCodeConnector sourceCodeConnector = Mockito.mock(ISourceCodeConnector.class);

		Mockito.doReturn(new File(root.getPath())).when(sourceCodeConnector).getDir();

		return sourceCodeConnector;
	}

	@Test
	public void test() throws BPException {
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());

		ValidateRealizationRequest request = Mockito.mock(ValidateRealizationRequest.class);

		BPRealization realization = new BPRealization();

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		String uri = dirUrl.getFile();

		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(generator)
				.getISourceCodeConventionFileLoader(Mockito.any());

		realization.setRealizationURI(uri);

		Mockito.doReturn(realization).when(request).getRealization();

		String modelName = "testModelName";

		Mockito.doReturn(modelName).when(request).getModelName();

		ISourceCodeConnector sourceCodeConnector = mockSCConnector(dirUrl);

		Mockito.doReturn(sourceCodeConnector).when(generator).getAdapter(Mockito.any());

		AtomicExecutableBP bpmn = generator.createModel(request);

		assertNotNull(bpmn);

		assertEquals(1, bpmn.getExecutableTasks().size());

		assertEquals(modelName, bpmn.getExecutableTasks().iterator().next().getName());
	}

	@Test
	public void testWithNullTaskName() throws BPException {
		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());

		ValidateRealizationRequest request = Mockito.mock(ValidateRealizationRequest.class);

		BPRealization realization = new BPRealization();

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		String uri = dirUrl.getFile();
		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(generator)
				.getISourceCodeConventionFileLoader(Mockito.any());
		realization.setRealizationURI(uri);

		Mockito.doReturn(realization).when(request).getRealization();


		ISourceCodeConnector sourceCodeConnector = mockSCConnector(dirUrl);

		Mockito.doReturn(sourceCodeConnector).when(generator).getAdapter(Mockito.any());

		AtomicExecutableBP bpmn = generator.createModel(request);

		assertNotNull(bpmn);

		assertEquals(1, bpmn.getExecutableTasks().size());

		String modelName = "Task";

		assertEquals(modelName, bpmn.getExecutableTasks().iterator().next().getName());
	}

	@Test
	public void testWithInput() throws BPException {

		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());

		ValidateRealizationRequest request = Mockito.mock(ValidateRealizationRequest.class);

		BPRealization realization = new BPRealization();

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");
		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(generator)
				.getISourceCodeConventionFileLoader(Mockito.any());
		String uri = dirUrl.getFile();

		realization.setRealizationURI(uri);

		Mockito.doReturn(realization).when(request).getRealization();

		String modelName = "testModelName";

		Mockito.doReturn(modelName).when(request).getModelName();


		ISourceCodeConnector sourceCodeConnector = mockSCConnector(dirUrl);

		Mockito.doReturn(sourceCodeConnector).when(generator).getAdapter(Mockito.any());

		AtomicExecutableBP bpmn = generator.createModel(request);

		assertNotNull(bpmn);
		Collection<BPInput> inputs = bpmn.getInputs();

		assertEquals(1, inputs.size());

		assertEquals("DataObject_1wb6a70", inputs.iterator().next().getId());

		Collection<BPOutput> outputs = bpmn.getOutputs();

		assertEquals(1, outputs.size());

		assertEquals("iDataObject_0loj2kk", outputs.iterator().next().getId());

		assertEquals(uri, bpmn.getRealization().getRealizationURI());

		assertEquals(modelName, bpmn.getExecutableTasks().iterator().next().getName());

	}
}