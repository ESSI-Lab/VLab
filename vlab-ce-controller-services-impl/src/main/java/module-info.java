import eu.essi_lab.vlab.ce.controller.services.AWSResourceAllocator;
import eu.essi_lab.vlab.ce.controller.services.BBoxAndStrParamIngestor;
import eu.essi_lab.vlab.ce.controller.services.DefaultOutputIngestor;
import eu.essi_lab.vlab.ce.controller.services.ECSManager;
import eu.essi_lab.vlab.ce.controller.services.KubernetesClientManager;
import eu.essi_lab.vlab.ce.controller.services.KubernetesResourceAllocator;
import eu.essi_lab.vlab.ce.controller.services.LocalComputeInfraProvider;
import eu.essi_lab.vlab.ce.controller.services.NumParameterIngestor;
import eu.essi_lab.vlab.ce.controller.services.URLIngestor;
import eu.essi_lab.vlab.ce.controller.services.ecs.ECSClient;
import eu.essi_lab.vlab.ce.controller.services.kubernetes.KubernetesClient;
import eu.essi_lab.vlab.controller.services.IBPComputeInfraProvider;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.controller.services.IContainerOrchestratorManager;
import eu.essi_lab.vlab.controller.services.IResourceAllocator;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.controller.services.OutputIngestor;

/**
 * @author Mattia Santoro
 */
module vlab.ce.controller.services.impl {
	requires org.apache.logging.log4j;
	requires vlab.core.engine.services.api;
	requires vlab.core.datamodel;
	requires vlab.controller.services.api;
	requires vlab.core.engine;
	requires software.amazon.awssdk.services.ecs;
	requires software.amazon.awssdk.regions;
	requires software.amazon.awssdk.auth;
	requires client.java.api;
	requires client.java;
	requires software.amazon.awssdk.core;
	requires software.amazon.awssdk.awscore;
	requires okhttp3;
	requires software.amazon.awssdk.services.autoscaling;
	requires software.amazon.awssdk.services.ec2;

	exports eu.essi_lab.vlab.ce.controller.services;
	exports eu.essi_lab.vlab.ce.controller.services.kubernetes;
	exports eu.essi_lab.vlab.ce.controller.services.kubernetes.jobs;
	exports eu.essi_lab.vlab.ce.controller.services.ecs;

	provides IContainerOrchestratorManager with ECSManager, KubernetesClientManager;
	provides IContainerOrchestratorCommandExecutor with ECSClient, KubernetesClient;
	provides IResourceAllocator with AWSResourceAllocator, KubernetesResourceAllocator;
	provides IBPComputeInfraProvider with LocalComputeInfraProvider;
	provides IndividualInputIngestor with URLIngestor, NumParameterIngestor, BBoxAndStrParamIngestor;
	provides OutputIngestor with DefaultOutputIngestor;
}