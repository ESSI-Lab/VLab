/**
 * @author Mattia Santoro
 */
module vlab.client {
	exports eu.essi_lab.vlab.core.client;
	requires org.apache.logging.log4j.core;
	requires vlab.core.datamodel;
	requires org.apache.logging.log4j;
	requires vlab.core.engine;
	requires vlab.core.engine.services.api;

	requires org.json;

}