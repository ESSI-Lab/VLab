package eu.essi_lab.vlab.web.api.servlet.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * This filter is used to fix the following SwaggerUIResourceFilter bug: the PATTERN defined at line 294 excludes the /swagger-ui.css file
 * because it is not prefixed by /css. The fix uses an ad-hoc pattern for /swagger-ui.css only and adds the api-docs prefix like in the
 * original filter.
 *
 *
 * <p>
 * The bugged version is:
 * <p>
 * org.apache.cxf:cxf-rt-rs-service-description-swagger
 *
 * @author Mattia Santoro
 */
@PreMatching
@Priority(10000)
public class SwaggerUIFilterFix implements ContainerRequestFilter {

	private static final Pattern PATTERN = Pattern.compile(".*swagger-ui.css");

	private static final Pattern PATTERN2 = Pattern.compile("^((?!api-docs).)*$");
	private static final String SLASH_STRING = "/";

	@Override
	public void filter(ContainerRequestContext rc) {
		if ("GET".equals(rc.getRequest().getMethod())) {
			UriInfo ui = rc.getUriInfo();
			String path = SLASH_STRING + ui.getPath();
			if (PATTERN.matcher(path).matches() && PATTERN2.matcher(path).matches()) {
				rc.setRequestUri(URI.create("api-docs" + path));
			}
		}
	}

}
