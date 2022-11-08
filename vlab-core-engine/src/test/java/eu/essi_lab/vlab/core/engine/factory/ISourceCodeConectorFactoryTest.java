package eu.essi_lab.vlab.core.engine.factory;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import eu.essi_lab.vlab.core.engine.BPExceptionMatcher;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class ISourceCodeConectorFactoryTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void test() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("No Source Code Connector was found for realization", BPException.ERROR_CODES.NO_ADAPTER_AVAILABLE));

		BPRealization realization = Mockito.mock(BPRealization.class);

		ISourceCodeConectorFactory factory = Mockito.spy(new ISourceCodeConectorFactory());
		Mockito.doReturn(Arrays.asList()).when(factory).loadConnectors();
		factory.getConnector(realization);
	}

	@Test
	public void test1() throws BPException {
		BPRealization realization = Mockito.mock(BPRealization.class);

		ISourceCodeConectorFactory factory = Mockito.spy(new ISourceCodeConectorFactory());

		ISourceCodeConnector mock1 = Mockito.mock(ISourceCodeConnector.class);
		SupportResponse supported = new SupportResponse();
		supported.setConventionsOk(true);

		Mockito.doReturn(supported).when(mock1).supports(Mockito.any());

		Mockito.doReturn(Arrays.asList(mock1)).when(factory).loadConnectors();

		factory.getConnector(realization);

		Mockito.verify(mock1, Mockito.times(1)).supports(Mockito.any());
	}

	@Test
	public void test2() throws BPException {
		BPRealization realization = Mockito.mock(BPRealization.class);

		ISourceCodeConectorFactory factory = Mockito.spy(new ISourceCodeConectorFactory());

		ISourceCodeConnector mock1 = Mockito.mock(ISourceCodeConnector.class);
		SupportResponse supported1 = new SupportResponse();
		supported1.setConventionsOk(false);
		Mockito.doReturn(supported1).when(mock1).supports(Mockito.any());

		ISourceCodeConnector mock2 = Mockito.mock(ISourceCodeConnector.class);
		SupportResponse supported2 = new SupportResponse();
		supported2.setConventionsOk(true);
		Mockito.doReturn(supported2).when(mock2).supports(Mockito.any());

		Mockito.doReturn(Arrays.asList(mock1, mock2)).when(factory).loadConnectors();

		ISourceCodeConnector connector = factory.getConnector(realization);

		assertTrue(connector.equals(mock2));

		Mockito.verify(mock1, Mockito.times(1)).supports(Mockito.any());
		Mockito.verify(mock2, Mockito.times(1)).supports(Mockito.any());
	}

	@Test
	public void test3() throws BPException {

		expectedException.expect(
				new BPExceptionMatcher("No Source Code Connector was found for realization", BPException.ERROR_CODES.NO_ADAPTER_AVAILABLE));

		BPRealization realization = Mockito.mock(BPRealization.class);

		ISourceCodeConectorFactory factory = Mockito.spy(new ISourceCodeConectorFactory());

		ISourceCodeConnector mock1 = Mockito.mock(ISourceCodeConnector.class);
		SupportResponse supported1 = new SupportResponse();
		supported1.setConventionsOk(false);
		Mockito.doReturn(supported1).when(mock1).supports(Mockito.any());

		ISourceCodeConnector mock2 = Mockito.mock(ISourceCodeConnector.class);
		SupportResponse supported2 = new SupportResponse();
		supported2.setConventionsOk(false);
		supported2.setCanRead(true);
		supported2.setConventionError("conv err");
		Mockito.doReturn(supported2).when(mock2).supports(Mockito.any());

		Mockito.doReturn(Arrays.asList(mock1, mock2)).when(factory).loadConnectors();

		ISourceCodeConnector connector = factory.getConnector(realization);

		assertTrue(connector.equals(mock2));

		Mockito.verify(mock1, Mockito.times(1)).supports(Mockito.any());
		Mockito.verify(mock2, Mockito.times(1)).supports(Mockito.any());
	}

	@Test
	public void test4() throws BPException {
		BPRealization realization = Mockito.mock(BPRealization.class);

		ISourceCodeConectorFactory factory = Mockito.spy(new ISourceCodeConectorFactory());

		ISourceCodeConnector mock1 = Mockito.mock(ISourceCodeConnector.class);
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				throw new Exception();
			}
		}).when(mock1).supports(Mockito.any());

		ISourceCodeConnector mock2 = Mockito.mock(ISourceCodeConnector.class);
		SupportResponse supported2 = new SupportResponse();
		supported2.setConventionsOk(true);
		Mockito.doReturn(supported2).when(mock2).supports(Mockito.any());

		Mockito.doReturn(Arrays.asList(mock1, mock2)).when(factory).loadConnectors();

		ISourceCodeConnector connector = factory.getConnector(realization);

		assertTrue(connector.equals(mock2));

		Mockito.verify(mock1, Mockito.times(1)).supports(Mockito.any());
		Mockito.verify(mock2, Mockito.times(1)).supports(Mockito.any());
	}
}