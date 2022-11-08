package eu.essi_lab.vlab.web.authentication;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mattia Santoro
 */
public interface VLabAuth {

	BPUser findUserAndThrowUnauthorizedIfNotLoggedIn(HttpServletRequest request) throws BPException;

	BPUser findUser(HttpServletRequest request);

}
