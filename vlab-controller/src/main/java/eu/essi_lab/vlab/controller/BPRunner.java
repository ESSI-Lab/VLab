package eu.essi_lab.vlab.controller;

import eu.essi_lab.vlab.controller.factory.BPComputeInfraProviderFactory;
import eu.essi_lab.vlab.controller.factory.IBPAdapterFactory;
import eu.essi_lab.vlab.controller.factory.ResourceAllocatorFactory;
import eu.essi_lab.vlab.controller.services.IBPAdapter;
import eu.essi_lab.vlab.controller.services.IBPComputeInfraProvider;
import eu.essi_lab.vlab.controller.services.IResourceAllocator;
import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRunStatuses;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPRunner {

	private final BPRun run;
	private IBPRunStatusRegistry statusRegistry;
	private IExecutableBPRegistry exRegistry;

	private Logger logger = LogManager.getLogger(BPRunner.class);
	private IBPAdapter adapter;
	private ContainerOrchestratorReservationResult resourcesAcquired;
	private BPComputeInfrastructure computInfrastructure;

	public BPRunner(BPRun r) {
		run = r;
	}

	BPComputeInfrastructure getTargetInfrastructure() throws BPException {
		VLabDockerResources requiredRes = null;

		try {
			requiredRes = getAdapter().getRequiredResources();
		} catch (BPException e) {
			logger.error("BPException reading reuired resources for run {} - cleainig and re-throwing", run.getRunid());

			cleanBPResources();

			throw e;
		} catch (Exception e) {
			logger.error("Exception reading reuired resources for run {}", run.getRunid(), e);

			cleanBPResources();

			throw new BPException("Can't read required resources", BPException.ERROR_CODES.NO_REQUIRED_RESOURCES_FOUND);
		}

		return instantiateIBPComputeInfraProvider().selectComputeInfrastructure(run, requiredRes);

	}

	IBPComputeInfraProvider instantiateIBPComputeInfraProvider() throws BPException {
		return new BPComputeInfraProviderFactory().getIBPComputeInfraProvider();
	}

	/**
	 * Triggers the actual execution of the process. Returns the status of the run.
	 *
	 * @return
	 */
	public BPRunStatus runBusinessProcess() throws BPException {

		logger.info("Trying to run {}", run.getRunid());

		BPRunStatus status = statusRegistry.getBPRunStatus(run.getRunid());

		status.setStatus(BPRunStatuses.RESERVING_RESOURCES.toString());

		logger.debug("Run {} status set to {}", run.getRunid(), BPRunStatuses.RESERVING_RESOURCES);

		initBPAdapter();

		computInfrastructure = getTargetInfrastructure();

		status.setExecutionInfrastructureId(computInfrastructure.getId());
		status.setExecutionInfrastructureLabel(computInfrastructure.getLabel());

		adapter.setTargetComputeInfrastructure(computInfrastructure);

		/* Acquiring resources */

		adapter.setInputs(run.getInputs());

		logger.debug("Run {} trying to acquire resources", run.getRunid());

		resourcesAcquired = adapter.acquireResources(status);

		if (!resourcesAcquired.isAcquired()) {

			logger.debug("Run {} trying to acquire resources failed (with message: {}), status set back to {}", run.getRunid(),
					resourcesAcquired.getMessage(), BPRunStatuses.QUEUED);

			status.setStatus(BPRunStatuses.QUEUED.toString());
			status.setExecutionInfrastructureId(null);
			status.setExecutionInfrastructureLabel(null);

			return status;

		}

		/* Launching process */

		adapter.execute(status);

		return status;
	}

	public void releaseResources() throws BPException {

		if (resourcesAcquired == null) {
			logger.debug("No acquired resources to release");
			return;
		}

		logger.debug("Found acquired resources to release with task anr: {}", resourcesAcquired.getTaskArn());

		getAdapter().releaseResources(resourcesAcquired);
	}

	public void setAdapter(IBPAdapter adapter) {
		this.adapter = adapter;
	}

	public IBPAdapter getAdapter() throws BPException {
		initBPAdapter();
		return this.adapter;
	}

	private void initBPAdapter() throws BPException {

		if (adapter != null)
			return;

		String wfid = run.getWorkflowid();

		logger.debug("Looking for a BPAdapter for workflow {}", wfid);

		BPUser user = new BPUser();
		user.setEmail(run.getOwner());

		AtomicExecutableBP executable = exRegistry.getExecutable(wfid, user);

		BPRealization realization = executable.getRealization();

		if (realization == null)
			throw new BPException("No Realization found for this workflow", BPException.ERROR_CODES.NO_BP_REALIZATION);

		adapter = adapterFromFactory(realization);

		logger.debug("Found BPAdapter {} for workflow {}", adapter.getClass().getName(), wfid);

	}

	IBPAdapter adapterFromFactory(BPRealization r) throws BPException {
		return IBPAdapterFactory.getBPAdapter(r);
	}

	public void setStatusRegistry(IBPRunStatusRegistry statusRegistry) {
		this.statusRegistry = statusRegistry;
	}

	public void setExRegistry(IExecutableBPRegistry exRegistry) {
		this.exRegistry = exRegistry;
	}

	public BPRun getBPRun() {
		return run;
	}

	public void cleanBPResources() throws BPException {

		initBPAdapter();

		adapter.cleanBPRunResources(run.getRunid());

	}

	IResourceAllocator instantiateAllocator() throws BPException {
		return ResourceAllocatorFactory.getResourceAllocator(computInfrastructure);
	}

	public void requireResources() throws BPException {

		logger.debug("Requiring resources for BPRun {} [workflow {}]", this.getBPRun().getRunid(), this.getBPRun().getWorkflowid());

		IResourceAllocator allocator = instantiateAllocator();

		VLabDockerResources resources = null;
		try {

			resources = adapter.getRequiredResources();

		} catch (BPException e) {

			logger.warn("Excpetion reading required resources for BPRun {} [workflow {}]", this.getBPRun().getRunid(),
					this.getBPRun().getWorkflowid());

			return;
		}

		logger.debug("Required Resources are: Memory {} MB and CPU {} units", resources.getMemory_mb(), resources.getCpu_units());

		allocator.request(resources);

	}
}
