package eu.essi_lab.vlab.core.datamodel.utils;

import eu.essi_lab.vlab.core.datamodel.APIWorkflowDetail;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import java.util.ArrayList;

/**
 * @author mattia
 */
public class WorkflowUtils {

	private static final String NOT_ALLOWED_TO_VIEW = "You are not allowed to visualize this private workflow";

	private WorkflowUtils() {
		// force static usage
	}

	public static APIWorkflowDetail removePrivateInfo(APIWorkflowDetail workflow, String requesteremail) {

		workflow.setOwnedbyrequester(
				workflow.getModelDeveloperEmail() != null && workflow.getModelDeveloperEmail().equalsIgnoreCase(requesteremail));

		workflow.setSharedwithrequester(workflow.getSharedWith() != null && workflow.getSharedWith().contains(requesteremail));

		workflow.setSharedWith(new ArrayList<>());
		workflow.setModelDeveloperEmail(null);

		return workflow;
	}

	public static void throwNotAuthorizedExceptionIfNotMyRun(BPUser user, APIWorkflowDetail workflow, String userMsg) throws BPException {
		String requestingUserEmail = user.getEmail();

		if (!isMyWf(workflow, requestingUserEmail)) {

			BPException ex = new BPException("Tried to perform an unauthorized action on workflow " + workflow.getId(),
					BPException.ERROR_CODES.NOT_AUTHORIZED);

			ex.setUserMessage(userMsg);

			throw ex;
		}
	}

	public static void throwExceptionIfCantRead(BPUser user, APIWorkflowDetail workflow) throws BPException {
		String requestingUserEmail = user.getEmail();

		if (isMyWf(workflow, requestingUserEmail))
			return;

		if (isPublic(workflow))
			return;

		if (isSahredWithMe(workflow, requestingUserEmail))
			return;

		BPException ex = new BPException("Tried to perform an unauthorized action on workflow " + workflow.getId(),
				BPException.ERROR_CODES.NOT_AUTHORIZED);

		ex.setUserMessage(NOT_ALLOWED_TO_VIEW);

		throw ex;
	}

	private static boolean isSahredWithMe(APIWorkflowDetail workflow, String requestingUserEmail) {

		return workflow.getSharedWith().contains(requestingUserEmail);
	}

	private static boolean isPublic(APIWorkflowDetail workflow) {
		return !workflow.isUnder_test();
	}

	private static boolean isMyWf(APIWorkflowDetail workflow, String requestingUserEmail) {
		return workflow.getModelDeveloperEmail().equalsIgnoreCase(requestingUserEmail);
	}
}
