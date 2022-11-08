package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTASK_STATUS;
import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTaskCheckResponse;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Status;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobWaitStartHandlerTest {

	@Test
	public void keepWaitTestPending() throws ApiException, BPException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(api));

		Mockito.doReturn(true).when(handler).isRetryableJob(Mockito.any());

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String ns = "namespace";
		Mockito.doReturn(ns).when(meta).getNamespace();

		Mockito.doReturn(meta).when(job).getMetadata();

		KubernetesJobManager jobManager = Mockito.mock(KubernetesJobManager.class);
		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.PENDING).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(jobManager).waitStart(Mockito.any(), Mockito.any(), Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(job, jobManager, 0, 10000L, response);

		Mockito.verify(jobManager, Mockito.times(1)).waitStart(Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertTrue(r.submissionSuccess());

	}

	@Test
	public void keepWaitTestTimeout() throws ApiException, BPException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(api));

		Mockito.doReturn(true).when(handler).isRetryableJob(Mockito.any());

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String ns = "namespace";
		Mockito.doReturn(ns).when(meta).getNamespace();

		Mockito.doReturn(meta).when(job).getMetadata();

		KubernetesJobManager jobManager = Mockito.mock(KubernetesJobManager.class);
		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.TIMEDOUT).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(jobManager).waitStart(Mockito.any(), Mockito.any(), Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(job, jobManager, 0, 10000L, response);

		Mockito.verify(jobManager, Mockito.times(1)).waitStart(Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertTrue(r.submissionSuccess());

	}

	@Test
	public void tooManyretries() throws ApiException, BPException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(api));

		handler.setMaxRetries(1);

		Mockito.doReturn(true).when(handler).isRetryableJob(Mockito.any());

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String ns = "namespace";
		Mockito.doReturn(ns).when(meta).getNamespace();
		String arn = "arn";
		Mockito.doReturn(arn).when(meta).getName();
		Mockito.doReturn(meta).when(job).getMetadata();

		KubernetesJobManager jobManager = Mockito.mock(KubernetesJobManager.class);
		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.TIMEDOUT).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(jobManager).waitStart(Mockito.any(), Mockito.any(), Mockito.any());

		V1Status lastSt = Mockito.mock(V1Status.class);

		Integer code = 1;
		Mockito.doReturn(code).when(lastSt).getCode();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String jarn = (String) invocation.getArguments()[0];

				if (!arn.equalsIgnoreCase(jarn))
					throw new Exception("Bad job id " + jarn);

				String names = (String) invocation.getArguments()[1];

				if (!"gi-suite".equalsIgnoreCase(names))
					throw new Exception("Bad namespace " + names);
				return lastSt;
			}
		}).when(api).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(job, jobManager, 1, 10000L, response);

		Mockito.verify(jobManager, Mockito.times(0)).waitStart(Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(api, Mockito.times(1)).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertFalse(r.submissionSuccess());

	}

	@Test
	public void noStatus() throws ApiException, BPException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobHandler handler = Mockito.spy(new KubernetesJobHandler(api));

		Mockito.doReturn(true).when(handler).isRetryableJob(Mockito.any());

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String ns = "namespace";
		Mockito.doReturn(ns).when(meta).getNamespace();

		String arn = "arn";
		Mockito.doReturn(arn).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		KubernetesJobManager jobManager = Mockito.mock(KubernetesJobManager.class);
		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(ECSTASK_STATUS.NOSTATUS).when(response).getLastStatus();

		ContainerOrchestratorSubmitResult submitok = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(true).when(submitok).submissionSuccess();

		Mockito.doReturn(submitok).when(jobManager).waitStart(Mockito.any(), Mockito.any(), Mockito.any());

		V1Status lastSt = Mockito.mock(V1Status.class);

		Integer code = 1;
		Mockito.doReturn(code).when(lastSt).getCode();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String jarn = (String) invocation.getArguments()[0];

				if (!arn.equalsIgnoreCase(jarn))
					throw new Exception("Bad job id " + jarn);

				String names = (String) invocation.getArguments()[1];

				if (!"gi-suite".equalsIgnoreCase(names))
					throw new Exception("Bad namespace " + names);
				return lastSt;
			}
		}).when(api).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

		ContainerOrchestratorSubmitResult r = handler.handleWaitStartFailure(job, jobManager, 0, 10000L, response);

		Mockito.verify(jobManager, Mockito.times(0)).waitStart(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(api, Mockito.times(1)).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertFalse(r.submissionSuccess());

	}

}