package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.ce.controller.services.BPExceptionMatcher;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPKubernetesComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerContainer;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class KubernetesClientTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@After
	public void removeFiles() {

		try {
			Stream<Path> files = Files.list(Paths.get("."));

			files.forEach(p -> {

				if (p.toString().endsWith(".sh")) {
					try {
						System.out.println("Deleting " + p);
						Files.delete(p);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private KubernetesClient initClient(boolean test, CoreV1Api coreV1Api, BatchV1Api batchV1Api, AppsV1Api aApi) throws BPException {

		BPKubernetesComputeInfrastructure computeInfra = Mockito.mock(BPKubernetesComputeInfrastructure.class);

		Optional<String> s3url = Optional.empty();
		Mockito.doReturn(s3url).when(computeInfra).getS3ServiceUrl();
		String ak = "ak";
		Mockito.doReturn(ak).when(computeInfra).getS3AccessKey();
		String sk = "sk";
		Mockito.doReturn(sk).when(computeInfra).getS3SecretKey();
		String region = "us-east-1";
		Mockito.doReturn(region).when(computeInfra).getS3BucketRegion();

		String serverurl = "http://example.com";
		Mockito.doReturn(serverurl).when(computeInfra).getServerUrl();

		String token = "token";
		Mockito.doReturn(token).when(computeInfra).getToken();

		String cnode = "cnode";
		Mockito.doReturn(cnode).when(computeInfra).getControllerNodeSelector();

		String enode = "enode";
		Mockito.doReturn(enode).when(computeInfra).getExecutorNodeSelector();

		Mockito.doReturn("vlabpv").when(computeInfra).getVlabPv();
		Mockito.doReturn("vlabpv-claim").when(computeInfra).getVlabPvClaim();

		KubernetesClient kubernetesDockerClient = Mockito.spy(new KubernetesClient());

		kubernetesDockerClient.setBPInfrastructure(computeInfra);

		Mockito.doReturn(coreV1Api).when(kubernetesDockerClient).getCoreApi();
		Mockito.doReturn(batchV1Api).when(kubernetesDockerClient).getBatchApi();
		Mockito.doReturn(aApi).when(kubernetesDockerClient).getAppApi();

		return kubernetesDockerClient;

	}

	@Test
	public void testContrucorNoConnectionTest() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(false, coreV1Api, batchV1Api, aApi);

		Mockito.verify(coreV1Api, Mockito.never()).listNamespaceCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testContrucorWithConnectionTest() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Response response = Mockito.mock(Response.class);
		Mockito.doReturn(true).when(response).isSuccessful();

		Call call = Mockito.mock(Call.class);

		Mockito.doReturn(response).when(call).execute();

		Mockito.doReturn(call).when(coreV1Api).listNamespaceCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		kubernetesDockerClient.testConnection();

		Mockito.verify(coreV1Api, Mockito.times(1)).listNamespaceCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testContrucorWithConnectionTestError() throws BPException, ApiException, IOException {

		expectedException.expect(new BPExceptionMatcher("Can't connect to kubernetes cluster",
				BPException.ERROR_CODES.OPERATION_NOT_SUPPORTED.BAD_CONFIGURATION));

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Response response = Mockito.mock(Response.class);

		ResponseBody body = Mockito.mock(ResponseBody.class);

		Mockito.doReturn(body).when(response).body();

		Mockito.doReturn(false).when(response).isSuccessful();

		Call call = Mockito.mock(Call.class);

		Mockito.doReturn(response).when(call).execute();

		Mockito.doReturn(call).when(coreV1Api).listNamespaceCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		kubernetesDockerClient.testConnection();

		Mockito.verify(coreV1Api, Mockito.times(1)).listNamespaceCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testCreateDirectory() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Response response = Mockito.mock(Response.class);
		Mockito.doReturn(true).when(response).isSuccessful();

		Call call = Mockito.mock(Call.class);

		Mockito.doReturn(response).when(call).execute();

		Mockito.doReturn(call).when(coreV1Api).listNamespaceCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String target = "/tmp/target/dir/" + UUID.randomUUID().toString();

		assertTrue(kubernetesDockerClient.createDirectory(target, 10L).isSuccess());

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelOutputFolderCreate/");

		File f = new File(dirUrl.getFile() + "../../../dirScript.sh");
		f.deleteOnExit();

	}

	@Test
	public void testdownloadFileto() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Call call = Mockito.mock(Call.class);

		String fileurl = "fileurl";

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String target = "/tmp/target/dir/" + UUID.randomUUID().toString();

		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(result).isSuccess();
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String t = (String) invocation.getArguments()[0];

				if (!target.equalsIgnoreCase(t))
					throw new Exception("Bad target: " + t);

				return result;
			}
		}).when(kubernetesDockerClient).createDirectory(Mockito.anyString(), Mockito.any());

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"cnode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				return responseJob;
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(result).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertTrue(kubernetesDockerClient.downloadFileTo(fileurl, target, 10L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(1)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testdownloadFiletoFalse() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Call call = Mockito.mock(Call.class);

		String fileurl = "fileurl";

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String target = "/tmp/target/dir/" + UUID.randomUUID().toString();

		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(result).isSuccess();
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String t = (String) invocation.getArguments()[0];

				if (!target.equalsIgnoreCase(t))
					throw new Exception("Bad target: " + t);

				return result;
			}
		}).when(kubernetesDockerClient).createDirectory(Mockito.anyString(), Mockito.any());

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"cnode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				return responseJob;
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		ContainerOrchestratorCommandResult result2 = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(result2).isSuccess();

		Mockito.doReturn(result2).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.downloadFileTo(fileurl, target, 10L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(1)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testdownloadFiletoBPEx() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Call call = Mockito.mock(Call.class);

		String fileurl = "fileurl";

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String target = "/tmp/target/dir/" + UUID.randomUUID().toString();

		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(result).isSuccess();
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String t = (String) invocation.getArguments()[0];

				if (!target.equalsIgnoreCase(t))
					throw new Exception("Bad target: " + t);

				return result;
			}
		}).when(kubernetesDockerClient).createDirectory(Mockito.anyString(), Mockito.any());

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"cnode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				throw new BPException("");
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.downloadFileTo(fileurl, target, 10L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testdownloadFiletoApiEx() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Call call = Mockito.mock(Call.class);

		String fileurl = "fileurl";

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String target = "/tmp/target/dir/" + UUID.randomUUID().toString();

		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(result).isSuccess();
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String t = (String) invocation.getArguments()[0];

				if (!target.equalsIgnoreCase(t))
					throw new Exception("Bad target: " + t);

				return result;
			}
		}).when(kubernetesDockerClient).createDirectory(Mockito.anyString(), Mockito.any());

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"cnode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				throw new ApiException();
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.downloadFileTo(fileurl, target, 10L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testdownloadFiletoCreateFail() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		Call call = Mockito.mock(Call.class);

		String fileurl = "fileurl";

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String target = "/tmp/target/dir/" + UUID.randomUUID().toString();

		ContainerOrchestratorCommandResult result = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(result).isSuccess();
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String t = (String) invocation.getArguments()[0];

				if (!target.equalsIgnoreCase(t))
					throw new Exception("Bad target: " + t);

				return result;
			}
		}).when(kubernetesDockerClient).createDirectory(Mockito.anyString(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.downloadFileTo(fileurl, target, 10L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testCopyFileToLocal() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelOutputFolderCreate/sample.sh");

		File file = new File(dirUrl.getFile());

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String target = "/tmp/target/dir/" + UUID.randomUUID().toString();

		assertTrue(kubernetesDockerClient.copyFileTo(file, target, 10L).isSuccess());

	}

	@Test
	public void testRunImageNoEODataApiEx() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);
		Mockito.doReturn(container).when(image).getContainer();

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();
		Mockito.doReturn(Boolean.TRUE).when(reservation).isAcquired();

		String runid = "runid";

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		KubernetesPodChecker podChecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(podChecker).when(kubernetesDockerClient).createPodChecker();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				String ns = (String) invocation.getArguments()[1];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));
				return null;
			}
		}).when(batchV1Api).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				return null;
			}
		}).when(podChecker).waitPodTermination(Mockito.any());

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"enode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				String n = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getName();
				String v = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getValue();

				if (!"BENGINERUNID".equalsIgnoreCase(n))
					throw new Exception("Bad env variable key: " + n);

				if (!runid.equalsIgnoreCase(v))
					throw new Exception("Bad env variable value: " + v);



				String n2 = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).getName();
				String v2 = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).getValue();

				if (!"vlabrunid".equalsIgnoreCase(n2))
					throw new Exception("Bad env variable key: " + n2);

				if (!runid.equalsIgnoreCase(v2))
					throw new Exception("Bad env variable value: " + v2);

				throw new ApiException();
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(resultok).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.runImage(image, reservation, "runfolder", 19L, runid, status).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).release(Mockito.any());
		Mockito.verify(podChecker, Mockito.times(1)).waitPodTermination(Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testRunImageNoEODataReleaseEx() throws BPException, ApiException, IOException {

		expectedException.expect(
				new BPExceptionMatcher("Unable to stop job reservationArn", BPException.ERROR_CODES.RESOURCES_RELEASE_ERROR));

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);
		Mockito.doReturn(container).when(image).getContainer();

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();
		Mockito.doReturn(Boolean.TRUE).when(reservation).isAcquired();

		String runid = "runid";

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		KubernetesPodChecker podChecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(podChecker).when(kubernetesDockerClient).createPodChecker();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				String ns = (String) invocation.getArguments()[1];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				throw new ApiException();
			}
		}).when(batchV1Api).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(resultok).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		kubernetesDockerClient.runImage(image, reservation, "runfolder", 19L, runid, status).isSuccess();

	}

	@Test
	public void testRunImage2() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);
		Mockito.doReturn(container).when(image).getContainer();

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();
		Mockito.doReturn(Boolean.TRUE).when(reservation).isAcquired();

		String runid = "runid";

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		KubernetesPodChecker podChecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(podChecker).when(kubernetesDockerClient).createPodChecker();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				String ns = (String) invocation.getArguments()[1];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));
				return null;
			}
		}).when(batchV1Api).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				return null;
			}
		}).when(podChecker).waitPodTermination(Mockito.any());

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"enode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				String n = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getName();
				String v = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getValue();

				if (!"BENGINERUNID".equalsIgnoreCase(n))
					throw new Exception("Bad env variable key: " + n);

				if (!runid.equalsIgnoreCase(v))
					throw new Exception("Bad env variable value: " + v);

				String n2 = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).getName();
				String v2 = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).getValue();

				if (!"vlabrunid".equalsIgnoreCase(n2))
					throw new Exception("Bad env variable key: " + n2);

				if (!runid.equalsIgnoreCase(v2))
					throw new Exception("Bad env variable value: " + v2);

				return responseJob;
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(resultok).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertTrue(kubernetesDockerClient.runImage(image, reservation, "runfolder", 19L, runid, status).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(1)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).release(Mockito.any());
		Mockito.verify(podChecker, Mockito.times(1)).waitPodTermination(Mockito.any());

	}

	@Test
	public void testRunImage2WithRes() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);
		Mockito.doReturn(container).when(image).getContainer();

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String mem = "100";
		String cpu = "2";

		Mockito.doReturn(mem).when(resources).getMemory_mb();
		Mockito.doReturn(cpu).when(resources).getCpu_units();

		Mockito.doReturn(resources).when(image).getResources();

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();
		Mockito.doReturn(Boolean.TRUE).when(reservation).isAcquired();

		String runid = "runid";

		BPRunStatus status = Mockito.mock(BPRunStatus.class);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		KubernetesPodChecker podChecker = Mockito.mock(KubernetesPodChecker.class);

		Mockito.doReturn(podChecker).when(kubernetesDockerClient).createPodChecker();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				String ns = (String) invocation.getArguments()[1];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));
				return null;
			}
		}).when(batchV1Api).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				return null;
			}
		}).when(podChecker).waitPodTermination(Mockito.any());

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"enode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				Quantity reqmem = job.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().get("memory");

				if (!(mem + "M").equalsIgnoreCase(reqmem.toSuffixedString()))
					throw new Exception("Bad memory requested " + reqmem.toSuffixedString());

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				String n = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getName();
				String v = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getValue();

				if (!"BENGINERUNID".equalsIgnoreCase(n))
					throw new Exception("Bad env variable key: " + n);

				if (!runid.equalsIgnoreCase(v))
					throw new Exception("Bad env variable value: " + v);

				String n2 = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).getName();
				String v2 = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).getValue();

				if (!"vlabrunid".equalsIgnoreCase(n2))
					throw new Exception("Bad env variable key: " + n2);

				if (!runid.equalsIgnoreCase(v2))
					throw new Exception("Bad env variable value: " + v2);

				return responseJob;
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(resultok).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertTrue(kubernetesDockerClient.runImage(image, reservation, "runfolder", 19L, runid, status).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(1)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).release(Mockito.any());
		Mockito.verify(podChecker, Mockito.times(1)).waitPodTermination(Mockito.any());

	}

	@Test
	public void testRelease() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		String reservationArn = "reservationArn";

		Mockito.doReturn(null).when(reservation).getTaskArn();
		Mockito.doReturn(Boolean.FALSE).when(reservation).isAcquired();

		kubernetesDockerClient.release(reservation);
		Mockito.verify(batchV1Api, Mockito.times(0)).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testRelease2() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();
		Mockito.doReturn(Boolean.FALSE).when(reservation).isAcquired();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String arn = (String) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(arn))
					throw new Exception("bad arn: " + arn);

				String ns = (String) invocation.getArguments()[1];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));
				return null;
			}
		}).when(batchV1Api).deleteNamespacedJob(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());

		kubernetesDockerClient.release(reservation);
		Mockito.verify(batchV1Api, Mockito.times(1)).deleteNamespacedJob(Mockito.argThat(new ArgumentMatcher<String>() {
			@Override
			public boolean matches(String argument) {
				return reservationArn.equals(argument);
			}

			@Override
			public String toString() {
				return reservationArn;
			}

		}), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	public void testRemoveDirectoryLocal() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		String folder = "/folder";

		Mockito.doReturn(resultok).when(kubernetesDockerClient).submitLocalCmdAndGetResult(Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(resultok).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertTrue(kubernetesDockerClient.removeDirectory(folder, 19L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testSaveFileToS3() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		IWebStorage webStorage = Mockito.mock(IWebStorage.class);
		Mockito.doReturn(webStorage).when(kubernetesDockerClient).createIWebStorageClient(Mockito.any());

		String filepath = "/folder/filepath.txt";
		String bucketname = "bucket";
		String s3key = "s3key";

		WebStorageObject wso = Mockito.mock(WebStorageObject.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String k = (String) invocation.getArguments()[1];

				if (!s3key.equalsIgnoreCase(k))
					throw new Exception(("Bad key " + k));

				File job = (File) invocation.getArguments()[0];

				return wso;
			}

		}).when(webStorage).upload((File) Mockito.any(), Mockito.any());

		assertTrue(kubernetesDockerClient.saveFileToWebStorage(filepath, bucketname, s3key, true, 19L).isSuccess());

		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(webStorage, Mockito.times(1)).upload((File) Mockito.any(), Mockito.any());

	}

	@Test
	public void testSaveFileToS3Fail() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		String filepath = "/folder/filepath.txt";
		String bucketname = "bucket";
		String s3key = "s3key";

		Mockito.doThrow(BPException.class).when(kubernetesDockerClient).createIWebStorageClient(Mockito.any());

		assertFalse(kubernetesDockerClient.saveFileToWebStorage(filepath, bucketname, s3key, true, 19L).isSuccess());

		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testSaveFileToS3LocalClientUploadFail() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelOutputFolderCreate/sample.sh");

		String filepath = dirUrl.getFile();
		String bucketname = "bucket";
		String s3key = "s3key";

		IWebStorage webStorage = Mockito.mock(IWebStorage.class);
		Mockito.doReturn(webStorage).when(kubernetesDockerClient).createIWebStorageClient(Mockito.any());

		Mockito.doThrow(BPException.class).when(webStorage).upload((File) Mockito.any(), Mockito.any());

		assertFalse(kubernetesDockerClient.saveFileToWebStorage(filepath, bucketname, s3key, true, 19L).isSuccess());

		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(webStorage, Mockito.times(1)).upload((File) Mockito.any(), Mockito.any());
	}

	@Test
	public void testSaveFolderToS3() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		String filepath = "/folder/dir/";
		String bucketname = "bucket";
		String s3key = "s3key";

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"cnode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				return responseJob;
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(resultfail).when(jobmanager).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.saveFolderToWebStorage(filepath, bucketname, s3key, true, 19L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(1)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	@Test
	public void testReserveResources() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String mem = "100";
		String cpu = "2";

		Mockito.doReturn(mem).when(resources).getMemory_mb();
		Mockito.doReturn(cpu).when(resources).getCpu_units();

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		Mockito.doReturn(true).when(reservation).isAcquired();

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"enode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				Quantity reqmem = job.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().get("memory");

				if (!(mem + "M").equalsIgnoreCase(reqmem.toSuffixedString()))
					throw new Exception("Bad memory requested " + reqmem.toSuffixedString());

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				return responseJob;
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(reservation).when(jobmanager).getReservationResult(Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertTrue(kubernetesDockerClient.reserveResources(resources).isAcquired());

		Mockito.verify(jobmanager, Mockito.times(1)).getReservationResult(Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testReserveResourcesFails() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String mem = "100";
		String cpu = "2";

		Mockito.doReturn(mem).when(resources).getMemory_mb();
		Mockito.doReturn(cpu).when(resources).getCpu_units();

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		Mockito.doReturn(false).when(reservation).isAcquired();

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"enode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				Quantity reqmem = job.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().get("memory");

				if (!(mem + "M").equalsIgnoreCase(reqmem.toSuffixedString()))
					throw new Exception("Bad memory requested " + reqmem.toSuffixedString());

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				return responseJob;
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				ContainerOrchestratorReservationResult r = (ContainerOrchestratorReservationResult) invocation.getArguments()[0];

				if (!reservationArn.equalsIgnoreCase(r.getTaskArn()))
					throw new Exception("Bad reservation arn: " + r.getTaskArn());

				return reservation;
			}
		}).when(kubernetesDockerClient).release(Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(reservation).when(jobmanager).getReservationResult(Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.reserveResources(resources).isAcquired());

		Mockito.verify(jobmanager, Mockito.times(1)).getReservationResult(Mockito.any());
		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).release(Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testReserveResourcesApiEx() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		VLabDockerResources resources = Mockito.mock(VLabDockerResources.class);

		String mem = "100";
		String cpu = "2";

		Mockito.doReturn(mem).when(resources).getMemory_mb();
		Mockito.doReturn(cpu).when(resources).getCpu_units();

		ContainerOrchestratorReservationResult reservation = Mockito.mock(ContainerOrchestratorReservationResult.class);

		Mockito.doReturn(false).when(reservation).isAcquired();

		String reservationArn = "reservationArn";

		Mockito.doReturn(reservationArn).when(reservation).getTaskArn();

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		V1Job responseJob = Mockito.mock(V1Job.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String ns = (String) invocation.getArguments()[0];

				if (!"gi-suite".equalsIgnoreCase(ns))
					throw new Exception(("Bad namespace " + ns));

				V1Job job = (V1Job) invocation.getArguments()[1];

				String selector = job.getSpec().getTemplate().getSpec().getNodeSelector().get("vlabkey");

				if (!"enode".equalsIgnoreCase(selector))
					throw new Exception("Bad selector: " + selector);

				Quantity reqmem = job.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().get("memory");

				if (!(mem + "M").equalsIgnoreCase(reqmem.toSuffixedString()))
					throw new Exception("Bad memory requested " + reqmem.toSuffixedString());

				String pv = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getName();

				if (!"vlabpv".equalsIgnoreCase(pv))
					throw new Exception("Bad persistent volume: " + pv);

				String pvc = job.getSpec().getTemplate().getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName();

				if (!"vlabpv-claim".equalsIgnoreCase(pvc))
					throw new Exception("Bad persistent volume claim: " + pvc);

				throw new ApiException();
			}

		}).when(batchV1Api).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(reservation).when(jobmanager).getReservationResult(Mockito.any());
		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		assertFalse(kubernetesDockerClient.reserveResources(resources).isAcquired());

		Mockito.verify(jobmanager, Mockito.times(0)).getReservationResult(Mockito.any());
		Mockito.verify(kubernetesDockerClient, Mockito.times(0)).release(Mockito.any());
		Mockito.verify(batchV1Api, Mockito.times(1)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

	}

	@Test
	public void testAppendParamToLocal() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		String key = "key";
		String value = "value";
		String targetfile = "targetfile";

		Mockito.doReturn(resultok).doReturn(resultfail).when(kubernetesDockerClient).submitLocalCmdAndGetResult(Mockito.any(),
				Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String max = (String) invocation.getArguments()[0];

				if (!targetfile.equalsIgnoreCase(max))
					throw new Exception("Expected to create " + targetfile + " but, found " + max);

				return resultok;
			}

		}).when(kubernetesDockerClient).createDirectory(Mockito.any(), Mockito.any());

		assertTrue(kubernetesDockerClient.appendParamTo(key, value, targetfile, 10L).isSuccess());
		assertFalse(kubernetesDockerClient.appendParamTo(key, value, targetfile, 10L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.verify(kubernetesDockerClient, Mockito.times(2)).submitLocalCmdAndGetResult(Mockito.any(), Mockito.any());

	}

	@Test
	public void testAppendParamNumToLocal() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		ContainerOrchestratorCommandResult resultfail = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(false).when(resultfail).isSuccess();

		String key = "key";
		Integer value = 3;
		String targetfile = "targetfile";

		Mockito.doReturn(resultok).doReturn(resultfail).when(kubernetesDockerClient).submitLocalCmdAndGetResult(Mockito.any(),
				Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String max = (String) invocation.getArguments()[0];

				if (!targetfile.equalsIgnoreCase(max))
					throw new Exception("Expected to create " + targetfile + " but, found " + max);

				return resultok;
			}

		}).when(kubernetesDockerClient).createDirectory(Mockito.any(), Mockito.any());

		assertTrue(kubernetesDockerClient.appendParamTo(key, value, targetfile, 10L).isSuccess());
		assertFalse(kubernetesDockerClient.appendParamTo(key, value, targetfile, 10L).isSuccess());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.verify(kubernetesDockerClient, Mockito.times(2)).submitLocalCmdAndGetResult(Mockito.any(), Mockito.any());

	}

	@Test
	public void testAppendParamNumToLocal_1() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		String key = "key";
		Integer value = 3;
		String targetfile = "targetfile";

		Mockito.doReturn(resultok).when(kubernetesDockerClient).submitLocalCmdAndGetResult(Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String max = (String) invocation.getArguments()[0];

				if (!targetfile.equalsIgnoreCase(max))
					throw new Exception("Expected to create " + targetfile + " but, found " + max);

				return resultok;
			}

		}).when(kubernetesDockerClient).createDirectory(Mockito.any(), Mockito.any());

		assertTrue(kubernetesDockerClient.appendParamTo(key, value, targetfile, 10L).isSuccess());
		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).createDirectory(Mockito.any(), Mockito.any());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).submitLocalCmdAndGetResult(Mockito.any(), Mockito.any());

	}

	@Test
	public void testAppendParamTextToLocal() throws BPException, ApiException, IOException {

		CoreV1Api coreV1Api = Mockito.mock(CoreV1Api.class);
		BatchV1Api batchV1Api = Mockito.mock(BatchV1Api.class);
		AppsV1Api aApi = Mockito.mock(AppsV1Api.class);

		KubernetesClient kubernetesDockerClient = initClient(true, coreV1Api, batchV1Api, aApi);

		ContainerOrchestratorCommandResult resultok = Mockito.mock(ContainerOrchestratorCommandResult.class);

		Mockito.doReturn(true).when(resultok).isSuccess();

		String key = "key";
		String value = "text";
		String targetfile = "targetfile";

		Mockito.doReturn(resultok).when(kubernetesDockerClient).submitLocalCmdAndGetResult(Mockito.any(), Mockito.any());

		KubernetesJobManager jobmanager = Mockito.mock(KubernetesJobManager.class);

		Mockito.doReturn(jobmanager).when(kubernetesDockerClient).createJobManager(Mockito.any());

		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {

				String max = (String) invocation.getArguments()[0];

				if (!targetfile.equalsIgnoreCase(max))
					throw new Exception("Expected to create " + targetfile + " but, found " + max);

				return resultok;
			}

		}).when(kubernetesDockerClient).createDirectory(Mockito.any(), Mockito.any());

		assertTrue(kubernetesDockerClient.appendParamTo(key, value, targetfile, 10L).isSuccess());
		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).createDirectory(Mockito.any(), Mockito.any());

		Mockito.verify(jobmanager, Mockito.times(0)).getJobResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Mockito.verify(batchV1Api, Mockito.times(0)).createNamespacedJob(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.verify(kubernetesDockerClient, Mockito.times(1)).submitLocalCmdAndGetResult(Mockito.any(), Mockito.any());

	}

}