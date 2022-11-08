import eu.essi_lab.vlab.core.engine.services.IBPConfigurableService;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
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
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import eu.essi_lab.vlab.core.engine.services.provider.IBPServiceProvider;

/**
 * @author Mattia Santoro
 */
module vlab.core.engine {
	exports eu.essi_lab.vlab.core.engine.factory;
	exports eu.essi_lab.vlab.core.engine;
	exports eu.essi_lab.vlab.core.engine.conventions;
	exports eu.essi_lab.vlab.core.engine.utils;
	exports eu.essi_lab.vlab.core.engine.serviceloader;

	requires org.apache.logging.log4j;
	requires vlab.core.datamodel;
	requires vlab.core.engine.services.api;
	requires camunda.bpmn.model;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
	requires vlab.core.utils;
	requires software.amazon.awssdk.auth;
	requires software.amazon.awssdk.regions;
	requires software.amazon.awssdk.services.s3;


	uses IBPConfigurableService;
	uses IBPQueueClient;
	uses IBPRunLogRegistry;
	uses IBPRunLogStorage;
	uses IBPRunRegistry;
	uses IBPRunStatusRegistry;
	uses IBPRunStatusStorage;
	uses IBPRunStorage;
	uses IBPWorkflowRegistryStorage;
	uses IESClient;
	uses IESHttpResponseReader;
	uses IESRequestSubmitter;
	uses IExecutableBPRegistry;
	uses ISourceCodeConnector;
	uses IWebStorage;
	uses ISourceCodeConventionFileLoader;
	uses IBPServiceProvider;
	uses IBPOutputWebStorage;

}