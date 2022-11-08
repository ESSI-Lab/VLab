package eu.essi_lab.vlab.web.api.exception;

import eu.essi_lab.vlab.core.datamodel.BPException;
import org.apache.http.HttpStatus;

/**
 * @author Mattia Santoro
 */
public class BPCodesMapper {

	public int toHttpStatusCode(Integer code) {
		return toHttpStatusCode(BPException.ERROR_CODES.decode(code));
	}

	public int toHttpStatusCode(BPException.ERROR_CODES code) {

		switch (code) {
		case INVALID_REQUEST:
		case UNKNOWN_REQUEST_FOUND:
		case NO_REQUEST_FOUND:
		case IO_FILE_NOT_FOUND:
		case NO_BP_REALIZATION:
		case BAD_WORKFLOWID:
		case SHARED_WITH_TOO_MANY:
		case DOUBLE_INPUT_ID:
			return HttpStatus.SC_BAD_REQUEST;
		case OPERATION_NOT_SUPPORTED:
			return HttpStatus.SC_NOT_IMPLEMENTED;
		case NOT_AUTHORIZED:
			return HttpStatus.SC_FORBIDDEN;
		case RESOURCE_NOT_FOUND:
		case BAD_CONFIGURATION:
		case NO_DOCKER_IMAGE_FILE_FUOND:
		case EMPTY_S3_BUCKET:
		case API_CALL_ERROR:
		case BPRUN_PULL_ERROR:
		case AWS_SQS_INIT_ERROR:
		case NO_VALIDATION_PLAN:
		case AWS_S3_BUCKET_INIT_ERROR:
		case BPSTATUSRUN_REGISTRY_ERROR:
		case RESOURCES_RESERVATION_ERROR:
		case UNKNOWN:
		case SERIALIZATION_ERR:
		case BPRUN_REGISTRY_ERROR:
		case CLEAN_RESOURCE_EXCEPTION:
		case BPRUN_PULL_RESULT_HANDLING_ERROR:
		case STATUS_REGISTRY_ERROR:
		case RESOURCES_RELEASE_ERROR:
		case NO_ADAPTER_AVAILABLE:
		default:
			return HttpStatus.SC_INTERNAL_SERVER_ERROR;

		}

	}
}