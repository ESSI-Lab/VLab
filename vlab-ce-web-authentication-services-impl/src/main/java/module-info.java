import eu.essi_lab.vlab.web.authentication.VLabAuth;
import eu.essi_lab.vlab.web.ce.auth.CEVLabAuth;

/**
 * @author Mattia Santoro
 */
module vlab.ce.web.authentication.services.impl {
	requires vlab.web.authentication.services;
	requires vlab.core.datamodel;

	requires jakarta.servlet;
	requires org.apache.logging.log4j;

	exports eu.essi_lab.vlab.web.ce.auth;

	provides VLabAuth with CEVLabAuth;
}