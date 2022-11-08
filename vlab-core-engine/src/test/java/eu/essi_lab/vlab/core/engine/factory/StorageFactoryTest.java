package eu.essi_lab.vlab.core.engine.factory;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.BPExceptionMatcher;
import eu.essi_lab.vlab.core.engine.services.IBPConfigurableService;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusStorage;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.engine.services.IBPQueueClient;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import eu.essi_lab.vlab.core.engine.services.IBPWorkflowRegistryStorage;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class StorageFactoryTest {

	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testExecutableStorage() throws Throwable {

		doTestException(IBPWorkflowRegistryStorage.class, "getBPExecutableStorage");
	}

	@Test
	public void testExecutableStorage2() throws BPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

		doTestInstantiation(IBPWorkflowRegistryStorage.class, "getBPExecutableStorage");
	}

	@Test
	public void testIBPRunStatusStorage() throws Throwable {

		doTestException(IBPRunStatusStorage.class, "getBPRunStatusStorage");
	}

	@Test
	public void testIBPRunStatusStorage2() throws BPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

		doTestInstantiation(IBPRunStatusStorage.class, "getBPRunStatusStorage");
	}

	@Test
	public void testIBPRunLogStorage() throws Throwable {

		doTestException(IBPRunLogStorage.class, "getBPRunLogStorage");
	}

	@Test
	public void testIBPRunLogStorage2() throws BPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

		doTestInstantiation(IBPRunLogStorage.class, "getBPRunLogStorage");
	}

	@Test
	public void testIWebStorage() throws Throwable {

		doTestException(IWebStorage.class, "getWebStorage", "bucket");
	}

	@Test
	public void testIWebStorage2() throws BPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

		doTestInstantiation(IWebStorage.class, "getWebStorage", "bucket");
	}

	@Test
	public void testIBPQueueClient() throws Throwable {

		doTestException(IBPQueueClient.class, "getQueueClient");
	}

	@Test
	public void testIBPQueueClient2() throws BPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

		doTestInstantiation(IBPQueueClient.class, "getQueueClient");

	}

	private void doTestException(Class<?> clazz, String method, Object... args) throws Throwable {

		expectedException.expect(
				new BPExceptionMatcher("Bad Configuration of " + clazz.getSimpleName(), BPException.ERROR_CODES.BAD_CONFIGURATION));

		StorageFactory factory = Mockito.spy(new StorageFactory());
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				if (clazz.isAssignableFrom((Class<?>) invocationOnMock.getArguments()[0])) {
					return Arrays.asList(Mockito.mock(clazz));
				}

				throw new Exception(
						"Expected loading of " + clazz.getName() + " but was " + invocationOnMock.getArguments()[0].getClass().getName());
			}
		}).when(factory).load(Mockito.any());

		try {

			if (null == args)
				StorageFactory.class.getMethod(method).invoke(factory);
			else {
				Class<?>[] arg_classes = new Class[args.length];

				for (Integer i = 0; i < args.length; i++)
					arg_classes[i] = args[i].getClass();

				StorageFactory.class.getMethod(method, arg_classes).invoke(factory, args);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw e.getTargetException();
		}

	}

	private void doTestInstantiation(Class<? extends IBPConfigurableService> clazz, String method, Object... args)
			throws BPException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		StorageFactory factory = Mockito.spy(new StorageFactory());

		IBPConfigurableService mock1 = Mockito.mock(clazz);
		Mockito.doReturn(Boolean.FALSE).when(mock1).supports(Mockito.any());

		IBPConfigurableService mock2 = Mockito.mock(clazz);
		Mockito.doReturn(Boolean.TRUE).when(mock2).supports(Mockito.any());
		Mockito.doThrow(BPException.class).when(mock2).configure(Mockito.any());

		IBPConfigurableService mock3 = Mockito.mock(clazz);
		Mockito.doReturn(Boolean.TRUE).when(mock3).supports(Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				Class<?> c = Class.forName(clazz.getName());

				if (c.isAssignableFrom((Class<?>) invocationOnMock.getArguments()[0])) {
					return Arrays.asList(mock1, mock2, mock3);
				}

				throw new Exception("Expected loading of " + clazz.getName());
			}
		}).when(factory).load(Mockito.any());

		if (null == args)
			StorageFactory.class.getMethod(method).invoke(factory);
		else {
			Class<?>[] arg_classes = new Class[args.length];

			for (Integer i = 0; i < args.length; i++)
				arg_classes[i] = args[i].getClass();

			StorageFactory.class.getMethod(method, arg_classes).invoke(factory, args);
		}

		Mockito.verify(mock1, Mockito.times(1)).supports(Mockito.any());
		Mockito.verify(mock1, Mockito.times(0)).configure(Mockito.any());

		Mockito.verify(mock2, Mockito.times(1)).supports(Mockito.any());
		Mockito.verify(mock2, Mockito.times(1)).configure(Mockito.any());

		Mockito.verify(mock3, Mockito.times(1)).supports(Mockito.any());
		Mockito.verify(mock3, Mockito.times(1)).configure(Mockito.any());

	}

}