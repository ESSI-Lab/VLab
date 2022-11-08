import eu.essi_lab.vlab.ce.engine.services.BPOutputWebStorage;
import eu.essi_lab.vlab.ce.engine.services.S3BucketClient;
import eu.essi_lab.vlab.ce.engine.services.AWSBPRunLogStorage;
import eu.essi_lab.vlab.ce.engine.services.AWSESBPRunRegistryStorage;
import eu.essi_lab.vlab.ce.engine.services.BPRunLogRegistry;
import eu.essi_lab.vlab.ce.engine.services.BPRunRegistry;
import eu.essi_lab.vlab.ce.engine.services.BPRunStatusRegistry;
import eu.essi_lab.vlab.ce.engine.services.ExecutableBPRegistry;
import eu.essi_lab.vlab.ce.engine.services.GitSourceCodeConnector;
import eu.essi_lab.vlab.ce.engine.services.KubemqClient;
import eu.essi_lab.vlab.ce.engine.services.RDFBPWorkflowRegistryStorage;
import eu.essi_lab.vlab.ce.engine.services.S3BPRunStatusRegistryStorage;
import eu.essi_lab.vlab.ce.engine.services.SQSClient;
import eu.essi_lab.vlab.ce.engine.services.SourceCodeConnector;
import eu.essi_lab.vlab.ce.engine.services.SourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.engine.services.IBPOutputWebStorage;
import eu.essi_lab.vlab.core.engine.services.IBPQueueClient;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusRegistry;
import eu.essi_lab.vlab.core.engine.services.IBPRunStatusStorage;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;
import eu.essi_lab.vlab.core.engine.services.IBPWorkflowRegistryStorage;
import eu.essi_lab.vlab.core.engine.services.IExecutableBPRegistry;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;

/**
 * @author Mattia Santoro
 */
module vlab.ce.engine.services.impl {
	requires vlab.core.engine.services.api;
	requires vlab.core.datamodel;
	requires org.apache.logging.log4j;
	requires org.json;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
	requires software.amazon.awssdk.auth;
	requires software.amazon.awssdk.regions;
	requires software.amazon.awssdk.http;
	requires software.amazon.awssdk.services.sqs;
	requires software.amazon.awssdk.core;
	requires software.amazon.awssdk.awscore;
	requires kubemq.sdk.Java;
	requires software.amazon.awssdk.services.s3;
	requires rdf4j.repository.api;
	requires rdf4j.query;
	requires rdf4j.model.api;
	requires rdf4j.model;
	requires rdf4j.model.vocabulary;
	requires rdf4j.sparqlbuilder;
	requires rdf4j.repository.sparql;
	requires software.amazon.awssdk.services.cloudwatchlogs;
	requires org.eclipse.jgit;
	requires vlab.core.utils;

	exports eu.essi_lab.vlab.ce.engine.services;
	exports eu.essi_lab.vlab.ce.engine.services.rdf;
	exports eu.essi_lab.vlab.ce.engine.services.es;
	exports eu.essi_lab.vlab.ce.engine.services.sourcecode;
	exports eu.essi_lab.vlab.ce.engine.services.aws;
	exports eu.essi_lab.vlab.ce.engine.services.utils;

	provides IBPRunRegistry with BPRunRegistry;
	provides ISourceCodeConnector with SourceCodeConnector, GitSourceCodeConnector;
	provides IBPRunStatusRegistry with BPRunStatusRegistry;
	provides IBPRunStatusStorage with S3BPRunStatusRegistryStorage;
	provides IBPQueueClient with SQSClient, KubemqClient;
	provides IBPRunStorage with AWSESBPRunRegistryStorage;
	provides IExecutableBPRegistry with ExecutableBPRegistry;
	provides IBPWorkflowRegistryStorage with RDFBPWorkflowRegistryStorage;
	provides IWebStorage with S3BucketClient;
	provides IBPRunLogRegistry with BPRunLogRegistry;
	provides IBPRunLogStorage with AWSBPRunLogStorage;
	provides ISourceCodeConventionFileLoader with SourceCodeConventionFileLoader;
	provides IBPOutputWebStorage with BPOutputWebStorage;
}