package eu.essi_lab.vlab.web.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
@Path("/docs")
public class UIDoc {

	private Logger logger = LogManager.getLogger(UIDoc.class);

	private Response doLoadResource(String filename) throws IOException, URISyntaxException {
		logger.info("Received getDocumentation Request {}", filename);

		URL webapp_WEBINF_classes = UIDoc.class.getClassLoader().getResource("/");

		java.nio.file.Path webapp_WEBINF = Paths.get(webapp_WEBINF_classes.toURI()).getParent();

		java.nio.file.Path webapp = webapp_WEBINF.getParent();

		java.nio.file.Path sw_ui = Paths.get(webapp.toString(), "swagger-ui");

		java.nio.file.Path index = Paths.get(sw_ui.toString(), filename);

		String text = new String(Files.readAllBytes(index), StandardCharsets.UTF_8);

		if ("index.html".equalsIgnoreCase(filename))
			text = text.replace("https://petstore.swagger.io/v2/swagger.json", "../openapi.json");

		return Response.ok(text).build();

	}

	@GET
	@Path("/{filename}")
	@Operation(hidden = true)
	public Response getDocumentationFromFile(@Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("filename") String filename)
			throws Exception {

		return doLoadResource(filename);

	}

	@GET
	@Path("/")
	@Operation(hidden = true)
	public Response getDocumentation(@Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("filename") String filename)
			throws Exception {

		return doLoadResource("index.html");

	}
}
