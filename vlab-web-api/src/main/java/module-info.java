import eu.essi_lab.vlab.web.authentication.VLabAuth;

/**
 * @author Mattia Santoro
 */
module vlab.web.api {
	requires io.swagger.v3.oas.annotations;
	requires jakarta.ws.rs;
	requires vlab.core.datamodel;
	requires jakarta.servlet;
	requires vlab.client;
	requires vlab.web.authentication.services;
	requires vlab.core.engine;
	requires org.apache.logging.log4j;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires org.apache.httpcomponents.httpcore;
	requires jakarta.annotation;
	requires io.swagger.v3.oas.models;
	requires io.swagger.v3.oas.integration;
	requires io.swagger.v3.jaxrs2;

	exports eu.essi_lab.vlab.web.api;
	exports eu.essi_lab.vlab.web.api.servlet;
	exports eu.essi_lab.vlab.web.api.servlet.filter;
	exports eu.essi_lab.vlab.web.api.exception;

	uses VLabAuth;
}