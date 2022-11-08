package eu.essi_lab.vlab.core.engine;

import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BPEngineTest {

	@Test
	public void testExecute() throws BPException {

		BPEngine engine = new BPEngine();

		IBPRunRegistry mockRegistry = Mockito.mock(IBPRunRegistry.class);

		IBPRunStatusRegistry mockStatusRegistry = Mockito.mock(IBPRunStatusRegistry.class);

		Mockito.doReturn(true).when(mockStatusRegistry).createBPRunStatus(Mockito.any());

		IBPRunStorage runStorage = Mockito.mock(IBPRunStorage.class);

		Mockito.doReturn(true).when(runStorage).queue(Mockito.any());

		mockRegistry.setBPRunStorage(runStorage);

		mockRegistry.setStatusRegistry(mockStatusRegistry);

		engine.setBpRunRegistry(mockRegistry);

		String wfid = "testwfid";

		List<BPInput> inputs = new ArrayList<>();

		BPInput i1 = new BPInput();
		inputs.add(i1);

		BPInput i2 = new BPInput();
		inputs.add(i2);

		BPUser fakeUser = new BPUser();

		fakeUser.setEmail("testuser@mail.com");

		String testName = "testName";

		String testDescription = "testDescription";

		BPRun registered = new BPRun();

		Mockito.doAnswer(new Answer() {
			@Override
			public BPRun answer(InvocationOnMock invocationOnMock) throws Throwable {
				return (BPRun) invocationOnMock.getArguments()[0];
			}
		}).when(mockRegistry).registerBPRun(Mockito.any());

		BPRun run = engine.execute(wfid, inputs, fakeUser, testName, testDescription);

		assertNotNull(run.getWorkflowid());
		assertNotNull(run.getInputs());

		assertNotNull(run.getName());

		assertEquals(testName, run.getName());
		assertEquals(testDescription, run.getDescription());

		assertEquals(2, run.getInputs().size());
		assertEquals(wfid, run.getWorkflowid());

		assertEquals(fakeUser.getEmail(), run.getOwner());

		assertNotNull(run.getCreationTime());

		Mockito.verify(mockRegistry, Mockito.times(1)).registerBPRun(Mockito.any());

	}

}