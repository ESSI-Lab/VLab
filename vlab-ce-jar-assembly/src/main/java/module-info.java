import eu.essi_lab.vlab.core.assembly.CEBPServiceProvider;
import eu.essi_lab.vlab.core.engine.services.provider.IBPServiceProvider;

/**
 * @author Mattia Santoro
 */
module vlab.ce.jar.assembly {
	requires vlab.controller;
	requires vlab.ce.engine.services.impl;
	requires vlab.core.datamodel;
	requires vlab.core.engine;
	requires vlab.core.engine.services.api;


	provides IBPServiceProvider with CEBPServiceProvider;

}