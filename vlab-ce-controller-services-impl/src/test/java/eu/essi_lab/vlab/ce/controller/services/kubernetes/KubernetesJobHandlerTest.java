package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.util.Optional;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobHandlerTest {

	@Test
	public void retryablesNumber() {

		assertEquals(2, new KubernetesJobHandler(null).getRetryableContainerNames().size());
	}

	@Test
	public void retryable() {

		V1Job job = Mockito.mock(V1Job.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(null));

		Optional<String> opt = Optional.of(new KubernetesJobHandler(null).getRetryableContainerNames().get(0));

		Mockito.doReturn(opt).when(handler).findContainerName(Mockito.any());

		assertTrue(handler.isRetryableJob(job));
	}

	@Test
	public void notRetryable() {

		V1Job job = Mockito.mock(V1Job.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(null));

		Optional<String> opt = Optional.of(new KubernetesJobHandler(null).getRetryableContainerNames().get(0) + "not");

		Mockito.doReturn(opt).when(handler).findContainerName(Mockito.any());

		assertFalse(handler.isRetryableJob(job));
	}

	@Test
	public void notRetryableEmpty() {

		V1Job job = Mockito.mock(V1Job.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(null));

		Optional<String> opt = Optional.empty();

		Mockito.doReturn(opt).when(handler).findContainerName(Mockito.any());

		assertFalse(handler.isRetryableJob(job));
	}

	@Test
	public void notRetryableNull() {

		V1Job job = Mockito.mock(V1Job.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(null));

		Optional<String> opt = Optional.ofNullable(null);

		Mockito.doReturn(opt).when(handler).findContainerName(Mockito.any());

		assertFalse(handler.isRetryableJob(job));
	}

	@Test
	public void nullPointer() {

		V1Job job = Mockito.mock(V1Job.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(null));

		assertFalse(handler.isRetryableJob(job));
	}

	@Test
	public void rerunTest() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(api));

		Mockito.doReturn(true).when(handler).isRetryableJob(Mockito.any());

		handler.setRetryInterval(2000L);

		handler.setMaxRetries(2);

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String ns = "namespace";
		Mockito.doReturn(ns).when(meta).getNamespace();

		Mockito.doReturn(meta).when(job).getMetadata();

		KubernetesJobManager jobManager = Mockito.mock(KubernetesJobManager.class);

		ContainerOrchestratorCommandResult r = handler.handleFailure(job, jobManager, 0, 10000L);

		Mockito.verify(api, Mockito.times(1)).createNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(jobManager, Mockito.times(1)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());

	}

	@Test
	public void rerunTestBPException() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(api));

		Mockito.doReturn(true).when(handler).isRetryableJob(Mockito.any());

		handler.setRetryInterval(2000L);

		handler.setMaxRetries(2);

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String ns = "namespace";
		Mockito.doReturn(ns).when(meta).getNamespace();

		Mockito.doReturn(meta).when(job).getMetadata();

		KubernetesJobManager jobManager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doThrow(ApiException.class).when(api).createNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		ContainerOrchestratorCommandResult r = handler.handleFailure(job, jobManager, 0, 10000L);

		assertFalse(r.isSuccess());

		assertTrue(r.getMessage().contains("Failed re-submitting job "));

		Mockito.verify(api, Mockito.times(1)).createNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(jobManager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());

	}

	@Test
	public void rerunTestNoRtries() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(api));

		Mockito.doReturn(true).when(handler).isRetryableJob(Mockito.any());

		handler.setRetryInterval(2000L);

		handler.setMaxRetries(0);

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String ns = "namespace";
		Mockito.doReturn(ns).when(meta).getNamespace();

		Mockito.doReturn(meta).when(job).getMetadata();

		KubernetesJobManager jobManager = Mockito.mock(KubernetesJobManager.class);

		ContainerOrchestratorCommandResult r = handler.handleFailure(job, jobManager, 0, 10000L);

		Mockito.verify(api, Mockito.times(0)).createNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(jobManager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());

	}

}