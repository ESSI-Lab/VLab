import eu.essi_lab.vlab.controller.adapters.SourceCodeAdapter;
import eu.essi_lab.vlab.controller.services.IBPAdapter;
import eu.essi_lab.vlab.controller.services.IBPComputeInfraProvider;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.controller.services.IResourceAllocator;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.controller.services.OutputIngestor;
import eu.essi_lab.vlab.core.engine.services.IBPConfigurableService;
import eu.essi_lab.vlab.core.engine.services.IBPQueueClient;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;
import eu.essi_lab.vlab.core.engine.services.IBPWorkflowRegistryStorage;
import eu.essi_lab.vlab.core.engine.services.IESClient;
import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import eu.essi_lab.vlab.core.engine.services.IESRequestSubmitter;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.engine.services.provider.IBPServiceProvider;

/**
 * @author Mattia Santoro
 */
module vlab.controller {
	requires vlab.core.datamodel;
	requires org.apache.logging.log4j;
	requires vlab.controller.services.api;
	requires vlab.core.engine;
	requires vlab.core.engine.services.api;
	requires vlab.core.utils;

	exports eu.essi_lab.vlab.controller.factory;
	exports eu.essi_lab.vlab.controller.executable;

	uses IBPRunStatusRegistry;
	uses IExecutableBPRegistry;
	uses IResourceAllocator;
	uses IBPComputeInfraProvider;
	uses IBPAdapter;
	uses IBPConfigurableService;
	uses IBPQueueClient;
	uses IBPRunLogRegistry;
	uses IBPRunLogStorage;
	uses IBPRunRegistry;
	uses IBPRunStatusStorage;
	uses IBPRunStorage;
	uses IBPWorkflowRegistryStorage;
	uses IESClient;
	uses IESHttpResponseReader;
	uses IESRequestSubmitter;
	uses ISourceCodeConnector;
	uses IWebStorage;
	uses IBPServiceProvider;
	uses IContainerOrchestratorManager;
	uses IContainerOrchestratorCommandExecutor;
	uses IndividualInputIngestor;
	uses OutputIngestor;

	provides IBPAdapter with SourceCodeAdapter;

}