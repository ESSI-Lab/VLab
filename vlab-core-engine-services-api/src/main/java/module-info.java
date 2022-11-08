/**
 * @author Mattia Santoro
 */
module vlab.core.engine.services.api {
	requires vlab.core.datamodel;
	requires org.apache.httpcomponents.httpclient;

	exports eu.essi_lab.vlab.core.engine.services.provider;
	exports eu.essi_lab.vlab.core.engine.services;

}