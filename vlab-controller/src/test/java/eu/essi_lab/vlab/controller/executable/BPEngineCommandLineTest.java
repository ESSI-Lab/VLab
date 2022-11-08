package eu.essi_lab.vlab.controller.executable;

import eu.essi_lab.vlab.core.datamodel.BPException;
import java.lang.reflect.Field;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BPEngineCommandLineTest {

	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();

	@Test
	public void testFailure() {
		exit.expectSystemExitWithStatus(1);

		BPEngineCommandLine.main(new String[0]);

	}

	@Test
	public void testOk() throws BPException, NoSuchFieldException, IllegalAccessException {

		exit.expectSystemExitWithStatus(0);

		BPEngineCommandLineExecutor executor = Mockito.mock(BPEngineCommandLineExecutor.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				if (((String[]) invocation.getArgument(0))[0].equalsIgnoreCase(EngineCmdRequest.PULL.toString()))
					return null;

				throw new Exception("Expected PULL, found " + ((String[]) invocation.getArgument(0))[0]);
			}
		}).when(executor).execCmd(Mockito.any());

		Field instance = BPEngineCommandLine.class.getDeclaredField("executor");

		instance.setAccessible(true);

		instance.set(executor, executor);

		BPEngineCommandLine.main(new String[] { "PULL" });

		Mockito.verify(executor, Mockito.times(1)).execCmd(Mockito.any());

	}
}