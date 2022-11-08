package eu.essi_lab.vlab.ce.engine.services.utils;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.SharedBPRun;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;

/**
 * @author Mattia Santoro
 */
public class BPRunStorageUtils {

	private static final String NOT_ALLOWED_TO_VIEW = "You are not allowed to visualize this private run";

	private BPRunStorageUtils() {
		//force static usage
	}

	public static void throwNotAuthorizedExceptionIfNotMyRun(BPRun run, String requestingUserEmail, String userMsg) throws BPException {

		if (!isMyRun(run, requestingUserEmail)) {

			BPException ex = new BPException("Tried to perform an unauthorized action on run " + run.getRunid(),
					BPException.ERROR_CODES.NOT_AUTHORIZED);

			ex.setUserMessage(userMsg);

			throw ex;
		}
	}

	public static void throwNotAuthorizedExceptionIfCantRead(BPRun run, String requestingUserEmail) throws BPException {

		if (isMyRun(run, requestingUserEmail))
			return;

		if (isSahredWithMe(run, requestingUserEmail))
			return;

		if (isPublic(run))
			return;

		BPException ex = new BPException("Tried to perform an unauthorized action on run " + run.getRunid(),
				BPException.ERROR_CODES.NOT_AUTHORIZED);

		ex.setUserMessage(NOT_ALLOWED_TO_VIEW);

		throw ex;

	}

	private static boolean isSahredWithMe(BPRun run, String requestingUserEmail) {
		return run.getSharedWith().contains(requestingUserEmail);
	}

	private static boolean isPublic(BPRun run) {
		return run.isPublicRun();
	}

	public static void throwNotAuthorizedExceptionIfNotSharedWithUser(BPRun run, String requestingUserEmail) throws BPException {

		if (!isPublic(run) && !isSahredWithMe(run, requestingUserEmail)) {

			BPException ex = new BPException(NOT_ALLOWED_TO_VIEW, BPException.ERROR_CODES.NOT_AUTHORIZED);

			ex.setUserMessage(NOT_ALLOWED_TO_VIEW);

			throw ex;
		}

	}

	private static boolean isMyRun(BPRun run, String requestingUserEmail) {
		return requestingUserEmail.equalsIgnoreCase(run.getOwner());
	}

	public static SharedBPRun createSharedRun(BPRun run) {

		SharedBPRun sharedBPRun = new SharedBPRun();

		sharedBPRun.setOwner(run.getOwner());

		sharedBPRun.setRunid(run.getRunid());

		return sharedBPRun;
	}

	public static BPRun resolveSharedBPRun(SharedBPRun sharedBPRun, String requestingUserEmail, IBPRunStorage storage) throws BPException {

		BPRun found = storage.get(sharedBPRun.getRunid(), sharedBPRun.getOwner());

		throwNotAuthorizedExceptionIfNotSharedWithUser(found, requestingUserEmail);

		return found;

	}
}
