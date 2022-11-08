package eu.essi_lab.vlab.web.api;

import eu.essi_lab.vlab.core.client.BPClient;
import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRunRequest;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.BPWorkflowInputsResponse;
import eu.essi_lab.vlab.core.datamodel.BPWorkflowOutputsResponse;
import eu.essi_lab.vlab.core.datamodel.CreateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.DeleteRunResponse;
import eu.essi_lab.vlab.core.datamodel.DeleteWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.Health;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import eu.essi_lab.vlab.core.datamodel.SearchWorkflowsResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateRunResponse;
import eu.essi_lab.vlab.core.datamodel.UpdateWorkflowResponse;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.engine.serviceloader.BPEngineServiceLoader;
import eu.essi_lab.vlab.web.authentication.VLabAuth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
@Path("/")
@OpenAPIDefinition(info = @Info(contact = @Contact(email = "mattia.santoro@cnr.it", name = "Mattia Santoro"), //
		title = "VLab REST API", version = "3.0.0", description = "Swagger documentation of VLab CE REST API"),//
		//				schemes = { OpenAPIDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS },
		security = @SecurityRequirement(scopes = "", name = "") //

)

public class VLabAPI {

	private Logger logger = LogManager.getLogger(VLabAPI.class);

	@GET
	@Path("/health")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Returns the health check of this instance", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Health.class))), tags = "Health")
	public Health health() {

		Health healthy = new Health();
		healthy.setHealthy(true);
		return healthy;

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}")
	@Operation(summary = "Returns the details of a Workflow.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = APIWorkflowDetail.class))), tags = "Workflows")
	public APIWorkflowDetail getworkflow(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id) throws BPException {

		logger.info("Received Get Workflow with id: {}", id);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getWorkflowDetail(id, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}/inputs")
	@Operation(summary = "Retrieves the list of inputs of a Workflow.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = APIWorkflowDetail.class))), tags = "Workflows")
	public BPWorkflowInputsResponse getworkflowinputs(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id) throws BPException {

		logger.info("Received Get Workflow Inputs with workflow id: {}", id);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getBPInputs(id, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}/inputs/{inputid}")
	@Operation(summary = "Retrieves an input of a Workflow.", //
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = APIWorkflowDetail.class))), tags = "Workflows")
	public BPInput getworkflowinput(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id,
			@Parameter(description = "The identifier of the input.", required = true) @PathParam("inputid") String inputid)
			throws BPException {

		logger.info("Received Get Workflow Input with workflow id: {} and input id: {}", id, inputid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getBPInput(id, inputid, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}/outputs")
	@Operation(summary = "Retrieves the list of outputs of a Workflow.", //
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = APIWorkflowDetail.class))), tags = "Workflows")
	public BPWorkflowOutputsResponse getworkflowoutputs(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id) throws BPException {

		logger.info("Received Get Workflow Outputs with workflow id: {}", id);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getBPOutputs(id, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}/outputs/{outputid}")
	@Operation(summary = "Retrieves an output of a Workflow.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = APIWorkflowDetail.class))), tags = "Workflows")
	public BPOutput getworkflowoutput(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id,
			@Parameter(description = "The identifier of the output.", required = true) @PathParam("outputid") String outputid)
			throws BPException {

		logger.info("Received Get Workflow Output with workflow id: {} and output id: {}", id, outputid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getBPOutput(id, outputid, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows")
	@Operation(summary = "Searches available Workflows.", //
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = SearchWorkflowsResponse.class))), tags = "Workflows")
	public SearchWorkflowsResponse searchworkflows(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,

			@Parameter(description = "The index of the first search result desired by the client. First element is at index 0. Default is"
					+ " 0.", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "0", type = "integer")) @DefaultValue("0") @QueryParam("start") Integer start,

			@Parameter(description = "The number of search results per page desired by the client. Default is 5.", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "5", type = "integer")) @DefaultValue("5") @QueryParam("count") Integer count,
			@Parameter(description = "Includes workflows under test in the response (true or false). Default is false.", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "false", type = "boolean")) @DefaultValue("false") @QueryParam("includeUnderTest") Boolean includeUnderTest,
			@Parameter(description = "The search text to filter results (based on workflow title and description).", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "", type = "string")) @DefaultValue("") @QueryParam("searchtext") String searchtext)
			throws BPException {

		logger.info("Received Search Workflows text {} ;; page {}-{} ;; include test {}", searchtext, start, (start + count - 1),
				includeUnderTest);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().searchWorkflows(searchtext, start, count, includeUnderTest, user);

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows")
	@Operation(summary = "Adds a new Workflow.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = SearchWorkflowsResponse.class))), tags = "Workflows")
	public CreateWorkflowResponse createworkflow(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@RequestBody(description = "The new workflow metadata", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = APIWorkflowDetail.class))) APIWorkflowDetail workflowDetail)
			throws BPException {

		logger.info("Received Create Workflow {}", workflowDetail.getId());

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().createWorkflowDetail(workflowDetail, user);

	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}")
	@Operation(summary = "Deletes a Workflow.", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = APIWorkflowDetail.class))), tags = "Workflows")
	public DeleteWorkflowResponse deleteworkflow(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id) throws BPException {

		logger.info("Received Delete Workflow with id: {}", id);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().deleteWorkflow(id, user);

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}")
	@Operation(summary = "Updates a Workflow.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = APIWorkflowDetail.class))), tags = "Workflows")
	public UpdateWorkflowResponse updateworkflow(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id,
			@RequestBody(description = "The updated Workflow metadata", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = APIWorkflowDetail.class))) APIWorkflowDetail workflowDetail)
			throws BPException {

		logger.info("Received Update Workflow with id: {}", id);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().updateWorkflow(id, workflowDetail, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs/{runid}")
	@Operation(summary = "Retrieves a Run",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = BPRun.class))), tags = "Runs")
	public BPRun getrun(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Run", required = true) @PathParam("runid") String runid) throws BPException {

		logger.info("Received GetRun with runid: {}", runid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getRun(runid, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs/{runid}/outputs")
	@Operation(summary = "Retrieves the list of outputs of a Run.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = BPRun.class))), tags = "Runs")
	public BPWorkflowOutputsResponse getrunoutputs(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Run", required = true) @PathParam("runid") String runid) throws BPException {

		logger.info("Received GetRun Outputs with runid: {}", runid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getBPRunOutputs(runid, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs/{runid}/outputs/{outputid}")
	@Operation(summary = "Retrieves an output of a Run.", //
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = BPRun.class))), tags = "Runs")
	public BPOutput getrunoutput(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Run", required = true) @PathParam("runid") String runid,
			@Parameter(description = "The identifier of the output.", required = true) @PathParam("outputid") String outputid)
			throws BPException {

		logger.info("Received GetRun Output with runid: {} and output id {}", runid, outputid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getBPRunOutput(runid, outputid, user);

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs/{runid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Modifies a Run", //
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = UpdateRunResponse.class))), tags = "Runs")
	public UpdateRunResponse putrun(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Run", required = true) @PathParam("runid") String runid,//
			@RequestBody(description = "The updated Run", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BPRun.class))) BPRun run)
			throws BPException {

		logger.info("Received Modify Run with runid: {}", runid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().updateRun(runid, run, user);

	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs/{runid}")
	@Operation(summary = "Deletes a Run",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = DeleteRunResponse.class))), tags = "Runs")
	public DeleteRunResponse deleterun(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Run", required = true) @PathParam("runid") String runid) throws BPException {

		logger.info("Received Delete Run with runid: {}", runid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().deleteRun(runid, user);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs")
	@Operation(summary = "Returns the list of available Runs, based on the provided search criteria.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = BPRuns.class))), tags = "Runs")
	public BPRuns searchruns(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "A search term to search in runs metadata", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "", type = "string"))
			//
			@QueryParam("q") @DefaultValue("") String q,//
			//
			@Parameter(description = "The index of the first search result desired by the client. First element is at index 0. Default is 0",//
					in = ParameterIn.QUERY, schema = @Schema(defaultValue = "0", type = "integer"))

			@QueryParam("start") @DefaultValue("0") Integer start,//
			//
			@Parameter(description = "The number of search results per page desired by the client. Default is 5.", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "5", type = "integer")) @QueryParam("count") @DefaultValue("5") Integer count,

			//
			@Parameter(description = "The identifier of the workflow which generated the runs. Default is null.", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "", type = "string")) @QueryParam("workflowid") @DefaultValue("") String workflowid

	) throws BPException {

		logger.info("Received Search runs with q: {} start: {} count: {} workflowid: {}", q, start, count, workflowid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().searchRuns(q, start, count, user, workflowid);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs/{runid}/status")
	@Operation(summary = "Returns the current status of a Run.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = BPRunStatus.class))), tags = "Runs")
	public BPRunStatus getrunstatus(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Run", required = true) @PathParam("runid") String runid) throws BPException {

		logger.info("Received Get Run Status with runid {}", runid);

		return getBPClient().getStatus(runid);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/runs/{runid}/logs")
	@Operation(summary = "Retrieves workflow execution logs.",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = LogMessagesResponse.class))), tags = "Runs")
	public LogMessagesResponse getrunlog(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Run", required = true) @PathParam("runid") String runid,//
			@Parameter(description = "A boolean indicating if the log is requested from the start (true) or the end (false). Default is "
					+ "true.", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "false", type = "boolean")) @DefaultValue("false") @QueryParam("head") String head,
			@Parameter(description = "A token used for pagination, use the token provided by previous request.") @QueryParam("nextToken") @DefaultValue("") String nextToken)
			throws BPException {

		logger.info("Received Get Run Logs with runid {}", runid);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().getLogs(runid, Boolean.valueOf(head), nextToken, user);

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflows/{id}/run")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Creates a new run of a Workflow",//
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = BPRun.class))), tags = "Runs")
	public BPRun createrun(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
			@Parameter(description = "The identifier of the Workflow", required = true) @PathParam("id") String id,//
			@RequestBody(description = "The Run request", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BPRunRequest.class))) BPRunRequest runRequest)
			throws BPException {

		logger.info("Received Create Run with workflow id: {}", id);

		BPUser user = findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().createRun(id, runRequest, user);

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/realizations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Validates the realization (implementation) of a workflow.", //
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ValidateRealizationResponse.class))), tags = "Realizations")
	public ValidateRealizationResponse validaterealization(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,//
			@RequestBody(description = "The Validation request request", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ValidateRealizationRequest.class))) ValidateRealizationRequest validateRealizationRequest)
			throws BPException {

		logger.info("Received Validation Request for model with name: {}", validateRealizationRequest.getModelName());

		findUserAndThrowUnauthorizedIfNotLoggedIn(httpServletRequest);

		return getBPClient().validateRealization(validateRealizationRequest);

	}

	private VLabAuth vLabAuth() {

		return BPEngineServiceLoader.load(VLabAuth.class).iterator().next();

	}

	protected BPUser findUserAndThrowUnauthorizedIfNotLoggedIn(HttpServletRequest request) throws BPException {

		return vLabAuth().findUserAndThrowUnauthorizedIfNotLoggedIn(request);

	}

	BPClient getBPClient() {
		return new BPClient();
	}

}
