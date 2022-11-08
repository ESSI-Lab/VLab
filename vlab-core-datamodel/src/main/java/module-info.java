/**
 * @author Mattia Santoro
 */
module vlab.core.datamodel {
	requires org.apache.logging.log4j;
	requires com.fasterxml.jackson.annotation;
	requires camunda.bpmn.model;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jdk8;

	exports eu.essi_lab.vlab.core.datamodel;
	exports eu.essi_lab.vlab.core.configuration;
	exports eu.essi_lab.vlab.core.serialization.json;
	exports eu.essi_lab.vlab.core.datamodel.utils;

}