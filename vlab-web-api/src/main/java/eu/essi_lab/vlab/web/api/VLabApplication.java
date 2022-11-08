package eu.essi_lab.vlab.web.api;

import eu.essi_lab.vlab.web.api.exception.BPExceptionMapper;
import eu.essi_lab.vlab.web.api.exception.GenericExceptionMapper;
import eu.essi_lab.vlab.web.api.exception.JsonExceptionMapper;
import eu.essi_lab.vlab.web.api.servlet.filter.CORSFilter;
import eu.essi_lab.vlab.web.api.servlet.filter.ContentTypeFixInterceptor;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
@ApplicationPath("/")
public class VLabApplication extends Application {

	private Logger logger = LogManager.getLogger(VLabApplication.class);

	public VLabApplication(@Context ServletConfig servletConfig) {
		super();

		String cp = servletConfig.getServletContext().getContextPath();
		logger.info("OpenAPI configuration at Context Path {}", cp);

		List<Server> servers = Arrays.asList(new Server().url(cp));
		OpenAPI oas = new OpenAPI().servers(servers);

		SwaggerConfiguration oasConfig = new SwaggerConfiguration();
		oasConfig.alwaysResolveAppPath(Boolean.TRUE).prettyPrint(Boolean.TRUE).openAPI(oas);

		try {
			new JaxrsOpenApiContextBuilder().servletConfig(servletConfig).application(this).openApiConfiguration(oasConfig).buildContext(
					true);
		} catch (OpenApiConfigurationException e) {

			throw new RuntimeException(e.getMessage(), e);
		}

		logger.info("OpenAPI configured");

	}

	@Override
	public Set<Class<?>> getClasses() {

		final Set<Class<?>> classes = new HashSet<Class<?>>();
		// register root resource
		classes.add(VLabAPI.class);

		//register additional features
		classes.add(CORSFilter.class);
		classes.add(BPExceptionMapper.class);
		classes.add(JsonExceptionMapper.class);
		classes.add(GenericExceptionMapper.class);
		classes.add(ContentTypeFixInterceptor.class);
		classes.add(OpenApiResource.class);

		classes.add(UIDoc.class);

		return classes;
	}
}
