package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ContainerState;
import io.kubernetes.client.openapi.models.V1ContainerStateWaiting;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class KubernetesPodCheckerTest {

	@Test
	public void test() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);

		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = null;//Mockito.mock(V1PodList.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String ns = (String) invocationOnMock.getArguments()[0];

				String selector = (String) invocationOnMock.getArguments()[5];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception("Bad namespace " + ns);

				if (!("job-name=" + jobid).equalsIgnoreCase(selector))
					throw new Exception("Bad selector " + selector);

				return podList;
			}

		}).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		assertFalse(kubernetesPodChecker.podInstantiated(jobid));
	}

	@Test
	public void test2() {
		KubernetesPodChecker kubernetesPodChecker = Mockito.spy(new KubernetesPodChecker());
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		Mockito.doReturn(jobid).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!jobid.equalsIgnoreCase(id))
					throw new Exception("Bad job name " + id);

				return Optional.of("podid");
			}
		}).when(kubernetesPodChecker).podIdentifier(Mockito.anyString());

		assertTrue(kubernetesPodChecker.podInstantiated(job));
	}

	@Test
	public void test3() {

		KubernetesPodChecker kubernetesPodChecker = Mockito.spy(new KubernetesPodChecker());
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1Job job = Mockito.mock(V1Job.class);

		V1ObjectMeta meta = Mockito.mock(V1ObjectMeta.class);

		Mockito.doReturn(jobid).when(meta).getName();

		Mockito.doReturn(meta).when(job).getMetadata();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String id = (String) invocationOnMock.getArguments()[0];

				if (!jobid.equalsIgnoreCase(id))
					throw new Exception("Bad job name " + id);

				return false;
			}
		}).when(kubernetesPodChecker).podInstantiated(Mockito.anyString());

		assertFalse(kubernetesPodChecker.podInstantiated(job));
	}

	@Test
	public void test4() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = Mockito.mock(V1PodList.class);

		Mockito.doReturn(null).when(podList).getItems();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String ns = (String) invocationOnMock.getArguments()[0];

				String selector = (String) invocationOnMock.getArguments()[5];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception("Bad namespace " + ns);

				if (!("job-name=" + jobid).equalsIgnoreCase(selector))
					throw new Exception("Bad selector " + selector);

				return podList;
			}

		}).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		assertFalse(kubernetesPodChecker.podInstantiated(jobid));
	}

	@Test
	public void test5() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = Mockito.mock(V1PodList.class);

		Mockito.doReturn(new ArrayList<>()).when(podList).getItems();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String ns = (String) invocationOnMock.getArguments()[0];

				String selector = (String) invocationOnMock.getArguments()[5];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception("Bad namespace " + ns);

				if (!("job-name=" + jobid).equalsIgnoreCase(selector))
					throw new Exception("Bad selector " + selector);

				return podList;
			}

		}).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		assertFalse(kubernetesPodChecker.podInstantiated(jobid));
	}

	@Test
	public void test6() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = Mockito.mock(V1PodList.class);

		Mockito.doReturn(new ArrayList<>()).when(podList).getItems();

		Mockito.doThrow(ApiException.class).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		assertFalse(kubernetesPodChecker.podInstantiated(jobid));
	}

	@Test
	public void test7() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = Mockito.mock(V1PodList.class);

		ArrayList<V1Pod> pods = new ArrayList<>();

		V1Pod pod = Mockito.mock(V1Pod.class);

		pods.add(pod);

		V1PodStatus status = Mockito.mock(V1PodStatus.class);

		List<V1PodCondition> conditions = new ArrayList<>();

		V1PodCondition condition = Mockito.mock(V1PodCondition.class);

		String cstatus = "false";
		Mockito.doReturn(cstatus).when(condition).getStatus();

		String creason = "unschedulable";
		Mockito.doReturn(creason).when(condition).getReason();

		conditions.add(condition);

		Mockito.doReturn(conditions).when(status).getConditions();

		Mockito.doReturn(status).when(pod).getStatus();

		Mockito.doReturn(pods).when(podList).getItems();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String ns = (String) invocationOnMock.getArguments()[0];

				String selector = (String) invocationOnMock.getArguments()[5];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception("Bad namespace " + ns);

				if (!("job-name=" + jobid).equalsIgnoreCase(selector))
					throw new Exception("Bad selector " + selector);

				return podList;
			}

		}).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		assertFalse(kubernetesPodChecker.podInstantiated(jobid));
	}

	@Test
	public void test8() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = Mockito.mock(V1PodList.class);

		ArrayList<V1Pod> pods = new ArrayList<>();

		V1Pod pod = Mockito.mock(V1Pod.class);

		pods.add(pod);

		V1PodStatus status = Mockito.mock(V1PodStatus.class);

		List<V1PodCondition> conditions = new ArrayList<>();

		V1PodCondition condition = Mockito.mock(V1PodCondition.class);

		String cstatus = "true";
		Mockito.doReturn(cstatus).when(condition).getStatus();

		String creason = "unschedulable";
		Mockito.doReturn(creason).when(condition).getReason();

		conditions.add(condition);

		V1ContainerStatus containerStatus = Mockito.mock(V1ContainerStatus.class);

		V1ContainerState state = Mockito.mock(V1ContainerState.class);

		V1ContainerStateWaiting waitingo = Mockito.mock(V1ContainerStateWaiting.class);

		String reason = "ImagePullBackOff";
		Mockito.doReturn(reason).when(waitingo).getReason();
		Mockito.doReturn(waitingo).when(state).getWaiting();

		Mockito.doReturn(state).when(containerStatus).getState();

		List<V1ContainerStatus> containerStatuses = new ArrayList<>();

		containerStatuses.add(containerStatus);

		Mockito.doReturn(containerStatuses).when(status).getContainerStatuses();

		Mockito.doReturn(conditions).when(status).getConditions();

		Mockito.doReturn(status).when(pod).getStatus();

		Mockito.doReturn(pods).when(podList).getItems();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String ns = (String) invocationOnMock.getArguments()[0];

				String selector = (String) invocationOnMock.getArguments()[5];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception("Bad namespace " + ns);

				if (!("job-name=" + jobid).equalsIgnoreCase(selector))
					throw new Exception("Bad selector " + selector);

				return podList;
			}

		}).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		assertFalse(kubernetesPodChecker.podInstantiated(jobid));
	}

	@Test
	public void test9() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = Mockito.mock(V1PodList.class);

		ArrayList<V1Pod> pods = new ArrayList<>();

		V1Pod pod = Mockito.mock(V1Pod.class);

		pods.add(pod);

		V1PodStatus status = Mockito.mock(V1PodStatus.class);

		List<V1PodCondition> conditions = new ArrayList<>();

		V1PodCondition condition = Mockito.mock(V1PodCondition.class);

		String cstatus = "true";
		Mockito.doReturn(cstatus).when(condition).getStatus();

		String creason = "unschedulable";
		Mockito.doReturn(creason).when(condition).getReason();

		conditions.add(condition);

		V1ContainerStatus containerStatus = Mockito.mock(V1ContainerStatus.class);

		V1ContainerState state = Mockito.mock(V1ContainerState.class);

		V1ContainerStateWaiting waitingo = Mockito.mock(V1ContainerStateWaiting.class);

		String reason = "ok";
		Mockito.doReturn(reason).when(waitingo).getReason();
		Mockito.doReturn(waitingo).when(state).getWaiting();

		Mockito.doReturn(state).when(containerStatus).getState();

		List<V1ContainerStatus> containerStatuses = new ArrayList<>();

		containerStatuses.add(containerStatus);

		Mockito.doReturn(containerStatuses).when(status).getContainerStatuses();

		Mockito.doReturn(conditions).when(status).getConditions();

		Mockito.doReturn(status).when(pod).getStatus();

		V1ObjectMeta podMeatadata = Mockito.mock(V1ObjectMeta.class);

		String podname = "podname";
		Mockito.doReturn(podname).when(podMeatadata).getName();

		Mockito.doReturn(podMeatadata).when(pod).getMetadata();

		Mockito.doReturn(pods).when(podList).getItems();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String ns = (String) invocationOnMock.getArguments()[0];

				String selector = (String) invocationOnMock.getArguments()[5];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception("Bad namespace " + ns);

				if (!("job-name=" + jobid).equalsIgnoreCase(selector))
					throw new Exception("Bad selector " + selector);

				return podList;
			}

		}).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		assertTrue(kubernetesPodChecker.podInstantiated(jobid));
	}

	@Test
	public void test10() throws ApiException {

		KubernetesPodChecker kubernetesPodChecker = new KubernetesPodChecker();
		kubernetesPodChecker.setWaitIntervalPodInstantitionInitial(0L);
		CoreV1Api api = Mockito.mock(CoreV1Api.class);

		kubernetesPodChecker.setCoreApi(api);

		String jobid = "jobid";

		V1PodList podList = Mockito.mock(V1PodList.class);

		ArrayList<V1Pod> pods = new ArrayList<>();

		V1Pod pod = Mockito.mock(V1Pod.class);

		pods.add(pod);

		Mockito.doReturn(pods).when(podList).getItems();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String ns = (String) invocationOnMock.getArguments()[0];

				String selector = (String) invocationOnMock.getArguments()[5];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception("Bad namespace " + ns);

				if (!("job-name=" + jobid).equalsIgnoreCase(selector))
					throw new Exception("Bad selector " + selector);

				return podList;
			}

		}).doReturn(null).when(api).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		kubernetesPodChecker.waitPodTermination(jobid);

		Mockito.verify(api, Mockito.times(2)).listNamespacedPod(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}
}