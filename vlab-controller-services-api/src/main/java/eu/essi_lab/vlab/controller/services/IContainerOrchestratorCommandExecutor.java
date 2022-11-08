package eu.essi_lab.vlab.controller.services;

import eu.essi_lab.vlab.core.datamodel.BPComputeInfrastructure;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.datamodel.BPRunStatus;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorReservationResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import java.io.File;
import java.util.Optional;

/**
 * @author Mattia Santoro
 */
public interface IContainerOrchestratorCommandExecutor {

	void setRootExecutionFolder(String rootFolder);

	void setBPInfrastructure(BPComputeInfrastructure infrastructure) throws BPException;

	/**
	 * Creates a directory on the Container.
	 *
	 * @param target  the absolute path, expressed according to the container directory structure.
	 * @param maxWait
	 * @return
	 */
	ContainerOrchestratorCommandResult createDirectory(String target, Long maxWait);

	/**
	 * Downloads the file referenced by source (which is supposed to a valid http, https, ftp, ftps URL) and stores to destination on the
	 * Container Host. The destination is supposed to be an absolute path expressed according to the container directory structure. If
	 * needed, this method creates the required directory structure for destination.
	 *
	 * @param source
	 * @param destination
	 * @param maxwait
	 * @return
	 */
	ContainerOrchestratorCommandResult downloadFileTo(String source, String destination, Long maxwait);

	/**
	 * Stores the file to destinationFile on the Container Host. The destinationFile is supposed to be an absolute path expressed according
	 * to the container directory structure. If needed, this method creates the required directory structure for destinationFile.
	 *
	 * @param file
	 * @param destinationFile
	 * @param maxwait
	 * @return
	 */
	ContainerOrchestratorCommandResult copyFileTo(File file, String destinationFile, Long maxwait);

	/**
	 * Submits a job based on the provided {@link VLabDockerImage} and waits for its completion (maxwait is expressed in millis). The job is
	 * submitted to the executor environment after releasing the resources acquired with the provided {@link
	 * ContainerOrchestratorReservationResult}. The runFolder parameter is the run root folder on the container host.
	 *
	 * @param image
	 * @param reservation
	 * @param runFolder   The run root folder on the container host
	 * @param maxwait
	 * @return
	 * @throws BPException
	 */
	ContainerOrchestratorCommandResult runImage(VLabDockerImage image, ContainerOrchestratorReservationResult reservation, String runFolder,
			Long maxwait, String runid, BPRunStatus status) throws BPException;

	/**
	 * Deletes folder on the container. Folder is absolute path in Container.
	 *
	 * @param folder
	 * @return
	 */
	ContainerOrchestratorCommandResult removeDirectory(String folder, Long maxWait);

	/**
	 * Saves the file at filePath to the configured Web Storage (e.g. AWS S3), using the provided bucketName and objectKey. The filePath is
	 * assumed to be a valid absolute path for the container.
	 *
	 * @param filePath
	 * @param bucketName
	 * @param objectKey
	 * @param publicread
	 * @param maxwait
	 */
	ContainerOrchestratorCommandResult saveFileToWebStorage(String filePath, String bucketName, String objectKey, Boolean publicread,
			Long maxwait);

	/**
	 * Saves all files in folder folderPath to the configured Web Storage (e.g. AWS S3), in the provided bucketName. Web storage file keys
	 * are baseObjectKey+FILENAME. Note that baseObjectKey is assumed to terminate with slash. The folderPath is assumed to be a valid
	 * absolute path for the container
	 *
	 * @param folderPath
	 * @param bucketName
	 * @param baseObjectKey
	 * @param publicread
	 * @param maxwait
	 */
	ContainerOrchestratorCommandResult saveFolderToWebStorage(String folderPath, String bucketName, String baseObjectKey,
			Boolean publicread, Long maxwait);

	ContainerOrchestratorReservationResult reserveResources(VLabDockerResources resources) throws BPException;

	void release(ContainerOrchestratorReservationResult reservation) throws BPException;

	/**
	 * Appends the "key":"value" to filePath on the Container Host. If filePath does not exist, it is created.
	 *
	 * @param key
	 * @param value
	 * @param filePath
	 * @param maxwait  @return
	 */
	ContainerOrchestratorCommandResult appendParamTo(String key, String value, String filePath, Long maxwait) throws BPException;

	/**
	 * Appends the "key":value to filePath on the Container Host. If filePath does not exist, it is created.
	 *
	 * @param key
	 * @param value
	 * @param filePath
	 * @param maxwait  @return
	 */
	ContainerOrchestratorCommandResult appendParamTo(String key, Number value, String filePath, Long maxwait);

	/**
	 * Executes an mv command
	 *
	 * @param source
	 * @param target
	 * @return
	 */
	ContainerOrchestratorCommandResult move(String source, String target, Long maxwait);

	/**
	 * Reads the logs from the task with id taskId. If containerName is specified, logs of that container will be returned. If containerName
	 * is not specified and the task has only one container, logs of the only running container will be returned. If sinceSeconds != null,
	 * only log events of the last sinceSeconds will be returned.
	 *
	 * @param taskId
	 * @param containerName
	 * @param sinceSeconds
	 * @return An ordered set of log events
	 */
	BPLogChunk readLogChunk(String taskId, Optional<String> containerName, Optional<Integer> sinceSeconds) throws BPException;

	void testConnection() throws BPException;
}
