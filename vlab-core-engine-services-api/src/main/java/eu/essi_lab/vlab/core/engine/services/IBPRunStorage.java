package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.SharedBPRun;
import java.util.Optional;

/**
 * An interface to be implemented to manage the queueing and storing of {@link BPRun}s. When a {@link BPRun} is queued {@link
 * #queue(BPRun)}, this is stored in the system queue AND in the {@link BPRun#getOwner()}'s workspace. When the next {@link BPRun} is read
 * from the queue ({@link #nextQuequedBPRun()}), the run is temporarily from the queue (for a time which is implementation dependent). After
 * this time has expired, if the run has been triggered (i.e. if {@link #moveToTriggered(BPRun)} has been invoked) then the run is
 * permanently removed from the queue (but remains available in the {@link BPRun#getOwner()}'s workspace); otherwise the run goes back to
 * the queue.
 *
 * @author Mattia Santoro
 */
public interface IBPRunStorage extends IBPConfigurableService {

	/**
	 * Adds the provided {@link BPRun} to the queue of runs to be executed and to the {@link BPRun#getOwner()}'s workspace.
	 *
	 * @param run
	 * @return true if the run has been correctly queued, false otherwise
	 */
	boolean queue(BPRun run);

	/**
	 * Deletes the provided {@link BPRun} from the {@link BPRun#getOwner()}'s workspace and the queue (if needed, i.e. if the run has not
	 * been executed yet). Deletes also the {@link SharedBPRun}s referencing the run (//TODO)
	 *
	 * @param run
	 * @param requestingUserEmail
	 * @return true if the deletion has been successfully executed, false otherwise
	 * @throws BPException if the requesting user is not allowed to perform this operation.
	 */
	boolean remove(BPRun run, String requestingUserEmail) throws BPException;

	/**
	 * Returns the {@link BPRun} with the provided runid.
	 *
	 * @param runid
	 * @param requestingUserEmail
	 * @return
	 * @throws BPException if the requesting user is not allowed to retrieve the {@link BPRun}, or if no run exists with the given runid
	 */
	BPRun get(String runid, String requestingUserEmail) throws BPException;

	/**
	 * Returns true if the {@link BPRun} with the provided runi exists in storage, false otherwise.
	 *
	 * @param runid
	 * @return
	 * @throws BPException if an error occurs during the check of existence.
	 */
	boolean exists(String runid) throws BPException;

	/**
	 * Finds the {@link BPRun} referenced by the provided {@link SharedBPRun}.
	 *
	 * @param sharedBPRun
	 * @param requestingUserEmail
	 * @return
	 * @throws BPException if the requesting user is not allowed to retrieve the run.
	 */
	BPRun resolveSharedBPRun(SharedBPRun sharedBPRun, String requestingUserEmail) throws BPException;

	/**
	 * Retrieves the next {@link BPRun} to be executed.
	 *
	 * @return
	 * @throws BPException
	 */
	Optional<BPRun> nextQuequedBPRun() throws BPException;

	/**
	 * Permanently removes the provided {@link BPRun} from the queue of runs to be executed, leaving it in the {@link BPRun#getOwner()}'s
	 * workspace.
	 *
	 * @param run
	 * @return true if the run has been correctly removed from the queue, false otherwise
	 */
	boolean moveToTriggered(BPRun run);

	/**
	 * Shares the provided {@link BPRun} with user identified by userToShareWithEmail.
	 *
	 * @param run
	 * @param requestingUserEmail
	 * @param userToShareWithEmail
	 * @return
	 * @throws BPException
	 */
	boolean shareWith(BPRun run, String requestingUserEmail, String userToShareWithEmail) throws BPException;

	/**
	 * Revokes the sharing of the provided {@link BPRun} with user identified by userToShareWithEmail.
	 *
	 * @param run
	 * @param requestingUserEmail
	 * @param userToShareWithEmail
	 * @return
	 * @throws BPException
	 */
	boolean revokeShare(BPRun run, String requestingUserEmail, String userToShareWithEmail) throws BPException;

	/**
	 * Makes the provided {@link BPRun} public, all users can visualize it.
	 *
	 * @param run
	 * @param requestingUserEmail
	 * @return
	 * @throws BPException if the requesting user is not allowed to perform this action
	 */
	boolean makePublic(BPRun run, String requestingUserEmail) throws BPException;

	/**
	 * Makes the provided {@link BPRun} non-public.
	 *
	 * @param run
	 * @param requestingUserEmail
	 * @return
	 * @throws BPException if the requesting user is not allowed to perform this action
	 */
	boolean revokePublic(BPRun run, String requestingUserEmail) throws BPException;

	BPRuns search(BPUser user, String text, Integer start, Integer count, String wfid) throws BPException;

	boolean udpdateRun(BPRun run, String requestingUserEmail) throws BPException;

	/**
	 * Extends the visibility timeout (i.e., the time the run stays invisible in the queue) of the run in the queue. The timeout is extend
	 * of the number of seconds provided in the seconds parameter. WARNING: The interface of this method is clear, but implementations might
	 * differ due to underlying technology. Known differences include:
	 * <ul>
	 *     <li>
	 *         AWS SQS implementation use the ChangeMessageVisibility method. This actually sets a new visibility timeout for the message.
	 *          However, this seems to the trick because the t0 of the new timeout SHOULD be the time in which the
	 *          ChangeMessageVisibility method is invoked. Knowing this, you can schedule the invocation of this method properly.
	 *     </li>
	 *     <li>KubeMQ has a method which is called ExtendVisibility but that has not been tested yet.</li>
	 * </ul>
	 *
	 * @param run
	 * @param seconds
	 */
	void extendVisibilityTimeout(BPRun run, Integer seconds) throws BPException;

	void setQueueClient(IBPQueueClient client);
}
