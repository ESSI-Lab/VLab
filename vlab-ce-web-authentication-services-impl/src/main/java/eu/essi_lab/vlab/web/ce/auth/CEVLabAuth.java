package eu.essi_lab.vlab.web.ce.auth;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.web.authentication.VLabAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class CEVLabAuth implements VLabAuth {

	private Logger logger = LogManager.getLogger(CEVLabAuth.class);
	private static final String DEF_USER = "vlab@default.user";

	@Override
	public BPUser findUserAndThrowUnauthorizedIfNotLoggedIn(HttpServletRequest request) throws BPException {
		return findUser(request);
	}

	@Override
	public BPUser findUser(HttpServletRequest request) {

		logger.info("Using default user");

		BPUser user = new BPUser();

		user.setEmail(DEF_USER);

		return user;
	}
}
