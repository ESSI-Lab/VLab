import eu.essi_lab.vlab.core.engine.services.provider.IBPServiceProvider;
import eu.essi_lab.vlab.web.authentication.VLabAuth;
import eu.essi_lab.vlab.web.ce.CEBPServiceProvider;

/**
 * @author Mattia Santoro
 */
module vlab.ce.web.api {
	requires org.apache.logging.log4j;
	requires vlab.core.datamodel;
	requires vlab.client;
	requires vlab.web.authentication.services;
	requires io.swagger.v3.oas.annotations;
	requires jakarta.ws.rs;


	requires com.fasterxml.jackson.annotation;
	requires org.apache.httpcomponents.httpcore;
	requires com.fasterxml.jackson.core;

	requires jakarta.activation;
	requires jakarta.servlet;
	requires jakarta.annotation;

	requires io.swagger.v3.jaxrs2;
	requires io.swagger.v3.oas.integration;
	requires io.swagger.v3.oas.models;
	requires vlab.core.engine.services.api;
	requires vlab.web.api;

	uses VLabAuth;

	provides IBPServiceProvider with CEBPServiceProvider;



}