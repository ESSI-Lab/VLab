package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTaskCheckResponse;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobCheckerTest {

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

		V1Job job2 = Mockito.mock(V1Job.class);

		V1JobStatus statusSuccess = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusSuccess).getActive();

		Mockito.doReturn(1).when(statusSuccess).getSucceeded();

		Mockito.doReturn(statusSuccess).when(job2).getStatus();

		Mockito.doReturn(job, job, job2).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());
		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		boolean completedOk = checker.jobCompletedWithSuccess(job, 10000L);

		Assert.assertTrue(completedOk);

		Mockito.verify(api, Mockito.times(3)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testWaitStarted() throws ApiException {

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

		Mockito.doReturn(statusRunning).when(job).getStatus();

		V1Job job2 = Mockito.mock(V1Job.class);

		V1JobStatus statusSuccess = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusSuccess).getActive();

		Mockito.doReturn(1).when(statusSuccess).getSucceeded();

		Mockito.doReturn(statusSuccess).when(job2).getStatus();

		Mockito.doReturn(job, job, job2).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		ECSTaskCheckResponse completedOk = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertTrue(completedOk.isSuccess());

		Mockito.verify(api, Mockito.times(3)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testWaitStarted2() throws ApiException {

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
		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		ECSTaskCheckResponse completedOk = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertTrue(completedOk.isSuccess());

		Mockito.verify(api, Mockito.times(1)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testWaitStarted3() throws ApiException {

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

		V1JobStatus statusFail = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(1).when(statusFail).getFailed();

		Mockito.doReturn(statusFail).when(job).getStatus();

		Mockito.doReturn(job).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());
		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		ECSTaskCheckResponse completedOk = checker.taskStartedWithSuccess(job, 10000L);

		Assert.assertFalse(completedOk.isSuccess());

		Mockito.verify(api, Mockito.times(1)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testWithNull() throws ApiException {

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

		V1Job job2 = Mockito.mock(V1Job.class);

		V1JobStatus statusSuccess = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(null).when(statusSuccess).getActive();

		Mockito.doReturn(1).when(statusSuccess).getSucceeded();

		Mockito.doReturn(statusSuccess).when(job2).getStatus();

		Mockito.doReturn(job, job, job2).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());
		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		boolean completedOk = checker.jobCompletedWithSuccess(job, 10000L);

		Assert.assertTrue(completedOk);

		Mockito.verify(api, Mockito.times(3)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testFailWithNullNull() throws ApiException {

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

		V1Job job2 = Mockito.mock(V1Job.class);

		V1JobStatus statusFail = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(null).when(statusFail).getActive();

		Mockito.doReturn(null).when(statusFail).getSucceeded();

		Mockito.doReturn(statusFail).when(job2).getStatus();

		Mockito.doReturn(job, job, job2).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());
		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		boolean completedOk = checker.jobCompletedWithSuccess(job, 10000L);

		Assert.assertFalse(completedOk);

		Mockito.verify(api, Mockito.times(3)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testFailWithNull() throws ApiException {

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

		V1Job job2 = Mockito.mock(V1Job.class);

		V1JobStatus statusFail = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(null).when(statusFail).getActive();

		Mockito.doReturn(0).when(statusFail).getSucceeded();

		Mockito.doReturn(statusFail).when(job2).getStatus();

		Mockito.doReturn(job, job, job2).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());
		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		boolean completedOk = checker.jobCompletedWithSuccess(job, 10000L);

		Assert.assertFalse(completedOk);

		Mockito.verify(api, Mockito.times(3)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testFail() throws ApiException {

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

		V1Job job2 = Mockito.mock(V1Job.class);

		V1JobStatus statusFail = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusFail).getActive();

		Mockito.doReturn(0).when(statusFail).getSucceeded();

		Mockito.doReturn(statusFail).when(job2).getStatus();

		Mockito.doReturn(job, job, job2).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());
		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		boolean completedOk = checker.jobCompletedWithSuccess(job, 10000L);

		Assert.assertFalse(completedOk);

		Mockito.verify(api, Mockito.times(3)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testException() throws ApiException {

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

		V1Job job2 = Mockito.mock(V1Job.class);

		V1JobStatus statusFail = Mockito.mock(V1JobStatus.class);

		Mockito.doReturn(0).when(statusFail).getActive();

		Mockito.doReturn(0).when(statusFail).getSucceeded();

		Mockito.doReturn(statusFail).when(job2).getStatus();

		Mockito.doThrow(ApiException.class).when(api).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

		checker.setJobStatusRetryIntervalMilliSeconds(10L);
		checker.setRetrieveJobStatusWaitIntervalMilliSeconds(10L);
		boolean completedOk = checker.jobCompletedWithSuccess(job, 10000L);

		Assert.assertFalse(completedOk);

		Mockito.verify(api, Mockito.times(6)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

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

		boolean completedOk = checker.jobCompletedWithSuccess(job, 10000L);

		Assert.assertFalse(completedOk);

		Mockito.verify(api, Mockito.times(0)).readNamespacedJobStatus(Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testGetPodChecker() throws ApiException {

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		CoreV1Api coreApi = Mockito.mock(CoreV1Api.class);

		KubernetesJobChecker checker = Mockito.spy(new KubernetesJobChecker(api));

		checker.setCoreApi(coreApi);

		Assert.assertEquals(coreApi, checker.getPodChecker().getCoreApi());

	}

}