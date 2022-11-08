package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTASK_STATUS;
import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTaskCheckResponse;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobCheckerStartWaitTest {

	@Test
	public void testPodNotInstantiated() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobChecker checker = Mockito.spy(new KubernetesJobChecker(api));

		KubernetesPodChecker podchecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(false).when(podchecker).podInstantiated((V1Job) Mockito.any());

		Mockito.doReturn(podchecker).when(checker).getPodChecker();

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String name = "name";

		Mockito.doReturn(name).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		V1JobStatus statusRunning = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(1).when(statusRunning).getActive();

		Mockito.doReturn(statusRunning).when(job).getStatus();

		Mockito.doReturn(job).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		ECSTaskCheckResponse started = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertFalse(started.isSuccess());
		Assert.assertEquals(ECSTASK_STATUS.NOSTATUS, started.getLastStatus());

		Mockito.verify(api, Mockito.times(0)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}


	@Test
	public void test() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobChecker checker = Mockito.spy(new KubernetesJobChecker(api));

		KubernetesPodChecker podchecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(true).when(podchecker).podInstantiated((V1Job) Mockito.any());

		Mockito.doReturn(podchecker).when(checker).getPodChecker();

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String name = "name";

		Mockito.doReturn(name).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		V1JobStatus statusRunning = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(1).when(statusRunning).getActive();

		Mockito.doReturn(statusRunning).when(job).getStatus();

		Mockito.doReturn(job).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		ECSTaskCheckResponse started = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertTrue(started.isSuccess());
		Assert.assertEquals(ECSTASK_STATUS.RUNNING, started.getLastStatus());

		Mockito.verify(api, Mockito.times(1)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}


	@Test
	public void testTimeout() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobChecker checker = Mockito.spy(new KubernetesJobChecker(api));

		KubernetesPodChecker podchecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(true).when(podchecker).podInstantiated((V1Job) Mockito.any());

		Mockito.doReturn(podchecker).when(checker).getPodChecker();

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String name = "name";

		Mockito.doReturn(name).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		V1JobStatus statusRunning = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusRunning).getActive();

		Mockito.doReturn(statusRunning).when(job).getStatus();

		Mockito.doReturn(job,job,job,job,job,job,job,job,job,job,job).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		ECSTaskCheckResponse started = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertFalse(started.isSuccess());
		Assert.assertEquals(ECSTASK_STATUS.TIMEDOUT, started.getLastStatus());

		Mockito.verify(api, Mockito.times(6)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testPendingRunning() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobChecker checker = Mockito.spy(new KubernetesJobChecker(api));

		KubernetesPodChecker podchecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(true).when(podchecker).podInstantiated((V1Job) Mockito.any());

		Mockito.doReturn(podchecker).when(checker).getPodChecker();

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String name = "name";

		Mockito.doReturn(name).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		V1JobStatus statusRunning = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(1).when(statusRunning).getActive();

		Mockito.doReturn(statusRunning).when(job).getStatus();

		V1Job job1 = Mockito.mock(V1Job.class);

		V1JobStatus statusPending = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusPending).getActive();

		Mockito.doReturn(statusPending).when(job1).getStatus();

		Mockito.doReturn(job1, job).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		ECSTaskCheckResponse started = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertTrue(started.isSuccess());
		Assert.assertEquals(ECSTASK_STATUS.RUNNING, started.getLastStatus());

		Mockito.verify(api, Mockito.times(2)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}


	@Test
	public void testPendingSucceded() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobChecker checker = Mockito.spy(new KubernetesJobChecker(api));

		KubernetesPodChecker podchecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(true).when(podchecker).podInstantiated((V1Job) Mockito.any());

		Mockito.doReturn(podchecker).when(checker).getPodChecker();

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String name = "name";

		Mockito.doReturn(name).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		V1JobStatus statusRunning = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(1).when(statusRunning).getSucceeded();

		Mockito.doReturn(statusRunning).when(job).getStatus();

		V1Job job1 = Mockito.mock(V1Job.class);

		V1JobStatus statusPending = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusPending).getActive();

		Mockito.doReturn(statusPending).when(job1).getStatus();

		Mockito.doReturn(job1, job).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		ECSTaskCheckResponse started = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertTrue(started.isSuccess());
		Assert.assertEquals(ECSTASK_STATUS.STOPPED, started.getLastStatus());

		Mockito.verify(api, Mockito.times(2)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}




	@Test
	public void testPendingFailed() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobChecker checker = Mockito.spy(new KubernetesJobChecker(api));

		KubernetesPodChecker podchecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(true).when(podchecker).podInstantiated((V1Job) Mockito.any());

		Mockito.doReturn(podchecker).when(checker).getPodChecker();

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		String name = "name";

		Mockito.doReturn(name).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		V1JobStatus statusRunning = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(1).when(statusRunning).getFailed();

		Mockito.doReturn(statusRunning).when(job).getStatus();

		V1Job job1 = Mockito.mock(V1Job.class);

		V1JobStatus statusPending = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusPending).getActive();

		Mockito.doReturn(statusPending).when(job1).getStatus();

		Mockito.doReturn(job1, job).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		ECSTaskCheckResponse started = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertFalse(started.isSuccess());
		Assert.assertEquals(ECSTASK_STATUS.STOPPED, started.getLastStatus());

		Mockito.verify(api, Mockito.times(2)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

}