package eu.essi_lab.vlab.core.datamodel;

import java.io.InputStream;
import java.util.Collection;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class AtomicExecutableBPTest {

	@Test
	public void readBPMN() {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		assertNotNull(stream);

		AtomicExecutableBP executable = new AtomicExecutableBP(stream, new BPMNConventionParser());

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
	public void setBPRealizaion() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test.bpmn");

		assertNotNull(stream);

		AtomicExecutableBP executable = new AtomicExecutableBP(stream, new BPMNConventionParser());

		BPRealization realization = new BPRealization();

		String uri = "file:/dir/to/git";
		realization.setRealizationURI(uri);

		executable.setBPRealization(realization);

		AtomicExecutableBP executableBP = new AtomicExecutableBP(executable.asStream(), new BPMNConventionParser());

		assertEquals(uri, executableBP.getRealization().getRealizationURI());

	}

	@Test
	public void addInputTest() throws BPException {
		AtomicExecutableBP bpmn = new AtomicExecutableBP(new BPMNConventionParser());

		BPInputDescription input = new BPInputDescription();
		input.setInput(new BPInput());
		input.getInput().setId("id");
		input.getInput().setName("name");
		bpmn.addInput(input, bpmn.getExecutableTasks().iterator().next());

		assertNotNull(bpmn);

		assertEquals(1, bpmn.getInputs().size());

		assertEquals("id", bpmn.getInputs().iterator().next().getId());

		assertEquals("name", bpmn.getInputs().iterator().next().getName());
	}

	@Test
	public void addOutputTest() throws BPException {
		AtomicExecutableBP bpmn = new AtomicExecutableBP(new BPMNConventionParser());

		BPOutputDescription output = new BPOutputDescription();
		output.setOutput(new BPOutput());
		output.getOutput().setId("id");
		output.getOutput().setName("name");
		bpmn.addOutput(output, bpmn.getExecutableTasks().iterator().next());

		assertNotNull(bpmn);

		assertEquals(1, bpmn.getOutputs().size());

		assertEquals("id", bpmn.getOutputs().iterator().next().getId());

		assertEquals("name", bpmn.getOutputs().iterator().next().getName());
	}

}