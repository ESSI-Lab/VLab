package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.ecs.ECSTaskCheckResponse;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorSubmitResult;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class KubernetesJobManagerTest {

	@Test
	public void testSuccessWaitStart() throws BPException {

		V1Job job = Mockito.mock(V1Job.class);
		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);
		String name = "name";
		Mockito.doReturn(name).when(meta).getName();
		Mockito.doReturn(meta).when(job).getMetadata();

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobManager manager = new KubernetesJobManager(api, null);

		KubernetesJobChecker checker = Mockito.mock(KubernetesJobChecker.class);
		manager.setChecker(checker);

		KubernetesJobHandler handler = Mockito.mock(KubernetesJobHandler.class);
		manager.setFailureHandler(handler);

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(true).when(response).isSuccess();

		Mockito.when(checker.taskStartedWithSuccess(Mockito.any(), Mockito.anyLong())).thenReturn(response);

		ContainerOrchestratorSubmitResult result = manager.waitStart(job, 10000L, 0);

		Assert.assertNotNull(result);

		Assert.assertTrue(result.submissionSuccess());

		Assert.assertEquals(name, result.getIdentifier());

		Mockito.verify(checker, Mockito.times(1)).taskStartedWithSuccess(Mockito.any(), Mockito.any());

		Mockito.verify(checker, Mockito.times(1)).setCoreApi(Mockito.any());

	}

	@Test
	public void testFailureWaitStart() throws BPException {

		V1Job job = Mockito.mock(V1Job.class);
		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);
		String name = "name";
		Mockito.doReturn(name).when(meta).getName();
		Mockito.doReturn(meta).when(job).getMetadata();

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobManager manager = new KubernetesJobManager(api, null);

		KubernetesJobChecker checker = Mockito.mock(KubernetesJobChecker.class);
		manager.setChecker(checker);

		KubernetesJobHandler handler = Mockito.mock(KubernetesJobHandler.class);
		manager.setFailureHandler(handler);

		ContainerOrchestratorSubmitResult r = Mockito.mock(ContainerOrchestratorSubmitResult.class);

		Mockito.doReturn(false).when(r).submissionSuccess();

		Mockito.doReturn(r).when(handler).handleWaitStartFailure(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		ECSTaskCheckResponse response = Mockito.mock(ECSTaskCheckResponse.class);

		Mockito.doReturn(false).when(response).isSuccess();

		Mockito.when(checker.taskStartedWithSuccess(Mockito.any(), Mockito.anyLong())).thenReturn(response);

		ContainerOrchestratorSubmitResult result = manager.waitStart(job, 10000L, 0);

		Assert.assertNotNull(result);

		Assert.assertFalse(result.submissionSuccess());

		Mockito.verify(checker, Mockito.times(1)).taskStartedWithSuccess(Mockito.any(), Mockito.any());

		Mockito.verify(handler, Mockito.times(1)).handleWaitStartFailure(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testSuccess() {

		V1Job job = Mockito.mock(V1Job.class);
		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);
		String name = "name";
		Mockito.doReturn(name).when(meta).getName();
		Mockito.doReturn(meta).when(job).getMetadata();

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobManager manager = new KubernetesJobManager(api, null);

		KubernetesJobChecker checker = Mockito.mock(KubernetesJobChecker.class);
		manager.setChecker(checker);

		KubernetesJobHandler handler = Mockito.mock(KubernetesJobHandler.class);
		manager.setFailureHandler(handler);

		Mockito.when(checker.jobCompletedWithSuccess(Mockito.any(), Mockito.anyLong())).thenReturn(true);

		ContainerOrchestratorCommandResult result = manager.getJobResult(job, job, 10000L, 0);

		Assert.assertNotNull(result);

		Assert.assertTrue(result.isSuccess());

	}

	@Test
	public void testFailure() {

		V1Job job = Mockito.mock(V1Job.class);
		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);
		String name = "name";
		Mockito.doReturn(name).when(meta).getName();
		Mockito.doReturn(meta).when(job).getMetadata();

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobManager manager = new KubernetesJobManager(api, null);

		KubernetesJobChecker checker = Mockito.mock(KubernetesJobChecker.class);
		manager.setChecker(checker);

		KubernetesJobHandler handler = Mockito.mock(KubernetesJobHandler.class);
		manager.setFailureHandler(handler);

		Mockito.when(checker.jobCompletedWithSuccess(Mockito.any(), Mockito.anyLong())).thenReturn(false);

		ContainerOrchestratorCommandResult result = manager.getJobResult(job, job, 10000L, 0);

		Mockito.verify(handler, Mockito.times(1)).handleFailure(Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any());

	}

	@Test
	public void testReservationFailure() throws BPException {

		V1Job job = Mockito.mock(V1Job.class);
		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);
		String name = "name";
		Mockito.doReturn(name).when(meta).getName();
		Mockito.doReturn(meta).when(job).getMetadata();

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobManager manager = new KubernetesJobManager(api, null);

		KubernetesJobChecker checker = Mockito.mock(KubernetesJobChecker.class);
		manager.setChecker(checker);

		KubernetesJobHandler handler = Mockito.mock(KubernetesJobHandler.class);
		manager.setFailureHandler(handler);

		Mockito.doReturn(false).when(checker).reservationTaskRunning(Mockito.any());

		ContainerOrchestratorReservationResult result = manager.getReservationResult(job);

		Assert.assertNotNull(result);

		Assert.assertFalse(result.isAcquired());

		Assert.assertEquals(name, result.getTaskArn());

		Mockito.verify(checker, Mockito.times(1)).setCoreApi(Mockito.any());
		Mockito.verify(checker, Mockito.times(1)).reservationTaskRunning(Mockito.any());
	}

	@Test
	public void testReservationSuccess() throws BPException {

		V1Job job = Mockito.mock(V1Job.class);
		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);
		String name = "name";
		Mockito.doReturn(name).when(meta).getName();
		Mockito.doReturn(meta).when(job).getMetadata();

		BatchV1Api api = Mockito.mock(BatchV1Api.class);

		KubernetesJobManager manager = new KubernetesJobManager(api, null);

		KubernetesJobChecker checker = Mockito.mock(KubernetesJobChecker.class);
		manager.setChecker(checker);

		KubernetesJobHandler handler = Mockito.mock(KubernetesJobHandler.class);
		manager.setFailureHandler(handler);

		Mockito.doReturn(true).when(checker).reservationTaskRunning(Mockito.any());

		ContainerOrchestratorReservationResult result = manager.getReservationResult(job);

		Assert.assertNotNull(result);

		Assert.assertTrue(result.isAcquired());

		Assert.assertEquals(name, result.getTaskArn());

		Mockito.verify(checker, Mockito.times(1)).setCoreApi(Mockito.any());
		Mockito.verify(checker, Mockito.times(1)).reservationTaskRunning(Mockito.any());

	}
}