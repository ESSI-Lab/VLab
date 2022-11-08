package eu.essi_lab.vlab.core.engine.bpmn;

import eu.essi_lab.vlab.core.engine.conventions.ISourceCodeConventionFileLoaderMock;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class BPDiagramGeneratorTest {


	private ISourceCodeConnector mockSCConnector(URL root) {

		ISourceCodeConnector sourceCodeConnector = Mockito.mock(ISourceCodeConnector.class);

		Mockito.doReturn(new File(root.getPath())).when(sourceCodeConnector).getDir();

		return sourceCodeConnector;
	}


	@Test
	public void testWithInput() throws BPException {

		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());

		ValidateRealizationRequest request = Mockito.mock(ValidateRealizationRequest.class);

		BPRealization realization = new BPRealization();

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModel/");

		String uri = dirUrl.getFile();

		Mockito.doReturn(ISourceCodeConventionFileLoaderMock.mocked(new File(dirUrl.getPath()).getAbsolutePath())).when(generator)
				.getISourceCodeConventionFileLoader(Mockito.any());

		realization.setRealizationURI(uri);

		Mockito.doReturn(realization).when(request).getRealization();

		String modelName = "testModelName testModelName testModelName testModelName";

		Mockito.doReturn(modelName).when(request).getModelName();
		ISourceCodeConnector sourceCodeConnector = mockSCConnector(dirUrl);

		Mockito.doReturn(sourceCodeConnector).when(generator).getAdapter(Mockito.any());

		AtomicExecutableBP bpmn = generator.createModel(request);

		BPDiagramGenerator dgen = new BPDiagramGenerator(bpmn);
		AtomicExecutableBP diagram = dgen.createDiagram();

		File file = null;
		try {
			file = File.createTempFile("bpmn-model-api-", ".bpmn");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bpmn.writeModelToFile(file, diagram.getModelInstace());

		System.out.println(file.getAbsolutePath());

	}

	@Test
	public void testWithTwoInput2() throws BPException {

		BPModelGenerator generator = Mockito.spy(new BPModelGenerator());

		ValidateRealizationRequest request = Mockito.mock(ValidateRealizationRequest.class);

		BPRealization realization = new BPRealization();

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelMultiInputs/");

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

		BPDiagramGenerator dgen = new BPDiagramGenerator(bpmn);
		AtomicExecutableBP diagram = dgen.createDiagram();

		File file = null;
		try {
			file = File.createTempFile("bpmn-model-api-multi-", ".bpmn");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bpmn.writeModelToFile(file, diagram.getModelInstace());

		System.out.println(file.getAbsolutePath());

	}
}