package eu.essi_lab.vlab.web.api.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPServletListener implements ServletContextListener {

	private Logger logger = LogManager.getLogger(BPServletListener.class);

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		logger.info("Starting servlet listener {} {}", servletContextEvent.getServletContext().getServerInfo(),
				servletContextEvent.getServletContext().getContextPath());

		logger.info("Started servlet listener");

	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

		logger.info("Stopping servlet listener");

		logger.info("Stopped servlet listener");

	}
}
