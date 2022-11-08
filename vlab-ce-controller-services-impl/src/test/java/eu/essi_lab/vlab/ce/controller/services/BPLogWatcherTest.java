package eu.essi_lab.vlab.ce.controller.services;

import eu.essi_lab.vlab.ce.controller.services.kubernetes.KubernetesLogParser;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class BPLogWatcherTest {

	@Test
	public void test() throws IOException, BPException {

		InputStream s = this.getClass().getClassLoader().getResourceAsStream("logchunck.txt");

		String text = IOUtils.toString(s, Charset.forName("UTF-8"));

		KubernetesLogParser parser = new KubernetesLogParser();
		BPLogChunk parsed = parser.parse(text);

		s = this.getClass().getClassLoader().getResourceAsStream("logchunck_0.txt");

		text = IOUtils.toString(s, Charset.forName("UTF-8"));

		BPLogChunk parsed_0 = parser.parse(text);

		IContainerOrchestratorCommandExecutor dce = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		Mockito.doReturn(parsed_0, parsed).when(dce).readLogChunk(Mockito.any(), Mockito.any(), Mockito.any());
		BPLogWatcher watcher = new BPLogWatcher(dce, null, null, new ConsoleLogWriter());

		assertEquals((Integer) 208, watcher.doRun());
		assertEquals((Integer) 309, watcher.doRun());

	}

	@Test
	public void test2() throws IOException, BPException {

		InputStream s = this.getClass().getClassLoader().getResourceAsStream("logchunck.txt");

		String text = IOUtils.toString(s, Charset.forName("UTF-8"));

		KubernetesLogParser parser = new KubernetesLogParser();
		BPLogChunk parsed = parser.parse(text);

		IContainerOrchestratorCommandExecutor dce = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		Mockito.doReturn(parsed).when(dce).readLogChunk(Mockito.any(), Mockito.any(), Mockito.any());
		BPLogWatcher watcher = new BPLogWatcher(dce, null, null, new ConsoleLogWriter());

		assertEquals((Integer) 417, watcher.doRun());

	}

	@Test
	public void test3() throws BPException, InterruptedException {
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		IContainerOrchestratorCommandExecutor dce = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				KubernetesLogParser parser = new KubernetesLogParser();
				BPLogChunk parsed = parser.parse(null);

				return parsed;
			}
		}).when(dce).readLogChunk(Mockito.any(), Mockito.any(), Mockito.any());

		BPLogWatcher watcher = new BPLogWatcher(dce, null, null, new ConsoleLogWriter());

		watcher.setSinceSeconds(11);

		scheduledExecutor.scheduleAtFixedRate(watcher, 0L, 100L, TimeUnit.MILLISECONDS);

		Thread.sleep(350L);

		assertEquals((Integer) 4, watcher.getCounter());
		scheduledExecutor.shutdownNow();
	}

	@Test
	public void test4() throws BPException, InterruptedException {
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		IContainerOrchestratorCommandExecutor dce = Mockito.mock(IContainerOrchestratorCommandExecutor.class);

		BPLogWatcher watcher = Mockito.spy(new BPLogWatcher(dce, null, null, new ConsoleLogWriter()));

		watcher.setSinceSeconds(11);

		Mockito.doThrow(BPException.class).when(watcher).doRun();
		scheduledExecutor.scheduleAtFixedRate(watcher, 0L, 100L, TimeUnit.MILLISECONDS);

		Thread.sleep(350L);

		assertEquals((Integer) 4, watcher.getCounter());

		scheduledExecutor.shutdownNow();
	}
}