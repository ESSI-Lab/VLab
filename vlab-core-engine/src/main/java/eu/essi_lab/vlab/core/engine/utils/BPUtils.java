package eu.essi_lab.vlab.core.engine.utils;

import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationLoader;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.VLabDockerContainer;
import eu.essi_lab.vlab.core.datamodel.WebStorageObject;
import eu.essi_lab.vlab.core.engine.factory.StorageFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * @author Mattia Santoro
 */
public class BPUtils {

	/**
	 * BPENGINE_RUNID_KEY is present for comatibility with first versions of vlab, it will be removed in following releases. //
	 * VLAB_RUNID_KEY should be used instead.
	 */
	public static final String BPENGINE_RUNID_KEY = "BENGINERUNID";
	public static final String VLAB_RUNID_KEY = "vlabrunid";
	public static final String ARRAY_ZIP_FILE_NAME = "array.zip";
	private static Logger logger = LogManager.getLogger(BPUtils.class);
	private static final String S3_BASE_URL = "https://s3.amazonaws.com/";
	private static final String S3_DOMAIN = "s3.amazonaws.com";
	private static final String HTTPS = "https://";
	private static final String WGET_O = "wget -O ";
	private static final String COMMAND_TO_COPY = "Command to copy ";
	private static final String DEBUG_COMMAND_COPY = "{}{} to {}: {}";
	private static final String SENTINEL_BASE_URL = "scihub.copernicus.eu/dhus/odata/v1/Products";

	private BPUtils() {
		//force static usage
	}

	private static void executeLocalCommand(String cmd, String err, int tries) throws BPException {
		logger.trace("Launching local command {} try n. {}", cmd, tries);

		Integer exitcode = 1270;

		Process process = null;

		try {

			process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });

			process.waitFor();

			logger.trace("Completed waiting local command {}", cmd);

			exitcode = process.exitValue();

			logger.trace("Found local command exit code {}", exitcode);

			if (exitcode - 0 == 0)
				return;

			logger.error("Non-zero exit code ({}) for command {}", exitcode, cmd);

			Thread.sleep(2000);

		} catch (IOException e) {

			logger.warn("Exception executing command {} locally", cmd, e);

		} catch (InterruptedException e) {

			logger.warn("Exception executing command {} locally", cmd, e);

			//This is suggested by Sonar as best practice
			Thread.currentThread().interrupt();

		}

		if (tries < 10) {

			tries++;

			executeLocalCommand(cmd, err, tries);

			return;

		}

		if (process != null)
			throw new BPException(err + " [exit " + exitcode + "]" + BPUtils.errorInfo(process),
					BPException.ERROR_CODES.LOCAL_COMMAND_ERROR);

		throw new BPException(err, BPException.ERROR_CODES.LOCAL_COMMAND_ERROR);
	}

	public static void executeLocalCommand(String cmd, String err) throws BPException {

		executeLocalCommand(cmd, err, 0);

	}

	static String errorInfo(Process process) {
		var is = "None";
		var es = "None";
		try {
			is = new String(process.getInputStream().readAllBytes());
			es = new String(process.getErrorStream().readAllBytes());
		} catch (IOException e) {
			logger.warn("Can't read stream of local command", e);
		}

		return "[error: " + es + "] [info: " + is + "]";
	}

	/**
	 * Given a list of directories absolute paths, this method creates the single command to be used by a container to create all the
	 * directories.
	 *
	 * @return
	 */
	public static String createDirectoriesCommand(List<String> dirsToCreate) {

		var sb = new StringBuilder();

		String fname = UUID.randomUUID().toString() + ".sh";

		sb.append("echo \"if [ ! -d \\$1 ]; then\" > " + fname + " && echo \"mkdir \\$1\" >> " + fname + " && echo ")//
				.append("\"fi\" >> " + fname + " && chmod 777 " + fname);

		for (String dir : dirsToCreate) {
			sb.append(" && /bin/sh " + fname + " ").append(dir);
		}

		return sb.toString();
	}

	/**
	 * Given an absolute path, this method creates the ordered list of directories which must be created to have this path available.
	 * Examples:
	 * <ul>
	 *     <li> With targetPath = "/dir/path/file.dat" the method returns [ "/dir", "/dir/path" ] </li>
	 *     <li> With targetPath = "/dir/path/folder/" the method returns [ "/dir", "/dir/path", "/dir/path/folder" ] </li>
	 * </ul>
	 *
	 * @param targetPath
	 * @return
	 */
	public static List<String> extractDirs(String targetPath) {

		String[] split = targetPath.split("/");

		List<String> ret = new ArrayList<>();

		ret.add("/" + split[1]);

		int last = split.length - 1;

		if (targetPath.endsWith("/"))
			last = split.length;

		for (var i = 2; i < last; i++) {

			ret.add(ret.get(i - 2) + "/" + split[i]);

		}

		return ret;
	}

	protected static String escapeForShellJSON(String requestImageURL) {

		if (requestImageURL == null)
			return null;

		return requestImageURL.replace("$", "\\$").replace("&", "\\&").replace("<", "\\<").replace(">", "\\>");

	}

	public static String toText(Number value) throws BPException {

		logger.trace("Converting number {} to text", value);

		if (value instanceof Double || value instanceof Integer)

			return value.toString();

		throw new BPException("Unexpected number type " + value.getClass().getName() + " with value " + value.toString(),
				BPException.ERROR_CODES.UNEXPECTED_NUMBER_FORMAT_FOR_MODEL_PARAM);
	}

	/**
	 * Creates the command to append key=value to file (which is a json file). If file does not exist, it is created
	 *
	 * @param key
	 * @param value
	 * @param file
	 * @return the command to execute
	 */
	public static String createCommand(String key, Number value, String file) throws BPException {

		String numText = toText(value);

		var sb = new StringBuilder();
		String fname = UUID.randomUUID().toString() + ".sh";
		sb.append("echo \"if [ ! -e \\$1 ]; then\" > " + fname + " && echo \"echo {} > \\$1\" >> " + fname + " && echo ")//
				.append("\"fi\" >> " + fname + " && ")//
				.append("echo \"sed -i 's/}/")//
				.append(",\\\"").append(key).append("\\\"").append(":").append(numText).append(",")//
				.append("}/g' \\$1\" >> " + fname + "") //
				.append("&& echo \"sed -i 's/,}/}/g' \\$1\" >> " + fname + "")//
				.append("&& echo \"sed -i 's/{,/{/g' \\$1\" >> " + fname + "")//
				.append(" && chmod 777 " + fname + "");

		sb.append(" && /bin/sh " + fname + " ").append(file);

		return sb.toString();

	}

	/**
	 * Creates the command to append key=value to file (which is a json file). If file does not exist, it is created
	 *
	 * @param key
	 * @param value
	 * @param file
	 * @return the command to execute
	 */
	public static String createCommand(String key, String value, String file, String fname) {

		var sb = new StringBuilder();

		sb.append("echo \"if [ ! -e \\$1 ]; then\" > " + fname + " && echo \"echo {} > \\$1\" >> " + fname + " && echo ")//
				.append("\"fi\" >> " + fname + " && ")//
				.append("echo \"sed -i 's/}/")//
				.append(",\\\"").append(key).append("\\\"").append(":").append("\\\"").append(escapeForShellJSON(value)).append("\\\",")//
				.append("}/g' \\$1\" >> " + fname + "") //
				.append("&& echo \"sed -i 's/,}/}/g' \\$1\" >> " + fname + "")//
				.append("&& echo \"sed -i 's/{,/{/g' \\$1\" >> " + fname + "")//
				.append(" && chmod 777 " + fname + "");

		sb.append(" && /bin/sh " + fname + " ").append(file);

		return sb.toString();

	}

	public static List<String> createCommand(VLabDockerContainer container, String runFolder) {

		var sb = new StringBuilder();

		if (runFolder != null && !"".equalsIgnoreCase(runFolder))
			sb.append("cd ").append(runFolder);

		List<String> containerCommands = container.getCommand();

		if (containerCommands != null)
			for (String c : containerCommands) {

				sb.append(" && ").append(c);

			}

		var toadd = sb.toString();

		if (toadd.startsWith(" && ")) {
			toadd = toadd.substring(4);
		}

		List<String> ret = new ArrayList<>();

		ret.add(toadd);

		logger.trace("Command of container is {}", ret.get(0));

		return ret;

	}

	public static String createMoveCmd(String source, String target) {
		return "mv " + source + " " + target;
	}

	public static boolean isS3URL(String requestImageURL) {

		return requestImageURL != null && (requestImageURL.toLowerCase().contains(S3_BASE_URL) || requestImageURL.toLowerCase().contains(
				S3_DOMAIN));

	}

	private static boolean isDirectory(String requestImageURL) {

		return requestImageURL != null && requestImageURL.toLowerCase().endsWith("/*");

	}

	public static String extractBucketNameFromURL(String url) {
		if (url.toLowerCase().startsWith(S3_BASE_URL)) {
			String nobase = url.replace(S3_BASE_URL, "");

			return nobase.substring(0, nobase.indexOf('/'));
		} else {
			String nobase = url.replace(HTTPS, "");

			return nobase.substring(0, nobase.indexOf('.'));
		}
	}

	static String findBucketRegion(AwsBasicCredentials awsCreds, String bucketName, String objectKey) {

		//TODO implement with IWebStorage
		logger.debug("Searching for region of bucket {}", bucketName);

		List<Region> regions = Region.regions();

		for (Region reg : regions) {

			var region = "us-east-1";

			if (reg != null && reg.toString() != null && !"null".equalsIgnoreCase(reg.toString()) && !reg.toString().contains("us-iso")
					&& !reg.toString().contains("aws-cn-global") && !reg.toString().contains("aws-us-gov-global") && !reg.toString()
					.contains("aws-iso-global") && !reg.toString().contains("aws-iso-b-global")) {
				region = reg.toString();
			}

			logger.trace("Searching bucket {} in region {}", bucketName, region);

			var staticCredentials = StaticCredentialsProvider.create(awsCreds);

			var s3client = S3Client.builder().region(Region.of(region)).credentialsProvider(staticCredentials).build();

			try {

				if (objectKey == null) {
					logger.trace("Testing with listObjects");
					s3client.listObjects(ListObjectsRequest.builder().bucket(bucketName).maxKeys(1).build());
				} else {
					logger.trace("Testing with getObjectMetadata");
					s3client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build());

				}

				logger.debug("Bucket {} was found in {}", bucketName, region);

				return region;

			} catch (S3Exception ex) {

				logger.debug("Bucket {} was not found in {}", bucketName, region);

			}
		}

		return null;
	}

	static String extractObjectKeyFromURL(String url) {
		if (url.toLowerCase().startsWith(S3_BASE_URL)) {
			String nobase = url.replace(S3_BASE_URL, "");

			return nobase.replace(extractBucketNameFromURL(url) + "/", "");
		} else {

			String nobase = url.replace(HTTPS, "");

			return nobase.replace(extractBucketNameFromURL(url) + ".s3.amazonaws.com/", "");

		}

	}

	public static String createS3SignedRequest(String requestImageURL, String ak, String sk) {

		String bucketName = extractBucketNameFromURL(requestImageURL);
		String objectKey = extractObjectKeyFromURL(requestImageURL);

		var awsCreds = AwsBasicCredentials.create(ak, sk);

		String region = findBucketRegion(awsCreds, bucketName, objectKey);

		if (region == null)
			return requestImageURL;

		return createS3SignedRequest(requestImageURL, ak, sk, region);

	}

	public static String createS3SignedRequest(String requestImageURL, String ak, String sk, String region) {

		String bucketName = extractBucketNameFromURL(requestImageURL);
		String objectKey = extractObjectKeyFromURL(requestImageURL);

		S3Presigner presigner = S3Presigner.builder().region(Region.of(region)).credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(ak, sk))).build();

		var getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();

		var getObjectPresignRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofHours(10)).getObjectRequest(
				getObjectRequest).build();

		// Generate the presigned request
		var presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);

		return presignedGetObjectRequest.url().toString();

	}

	public static List<String> createS3DirectoryCopyCommand(String target, String s3url, String ak, String sk, Optional<String> s3Url)
			throws BPException {

		String bucket = extractBucketNameFromURL(s3url);
		var awsCreds = AwsBasicCredentials.create(ak, sk);

		String region = findBucketRegion(awsCreds, bucket, null);

		var bc = new StorageFactory().getWebStorage(bucket);

		String s3Folder = extractObjectKeyFromURL(s3url).replace("*", "");

		List<String> subObjctes = bc.listSubOjects(s3Folder);

		if (subObjctes.isEmpty())
			throw new BPException("Error reading folder " + s3url, BPException.ERROR_CODES.EMPTY_S3_BUCKET);

		List<String> commands = new ArrayList<>();

		for (String obj : subObjctes) {

			obj = obj.replace(s3Folder, "");

			String signed = createS3SignedRequest(s3url.replace("*", "") + obj, ak, sk);

			String command = WGET_O + target + obj + " \"" + escapeForShellURL(signed) + "\"";

			commands.add(command);

			if (logger.isDebugEnabled())
				logger.debug("{}{}{} to {}: {}", COMMAND_TO_COPY, s3url.replace("*", ""), obj, command, command);

		}

		return commands;

	}

	protected static String escapeForShellURL(String requestImageURL) {

		if (requestImageURL == null)
			return null;

		return requestImageURL.replace("$", "\\$");

	}

	static boolean isUnAuthenticatedSentinelURL(String requestImageURL) {

		return requestImageURL != null && requestImageURL.toLowerCase().contains(SENTINEL_BASE_URL.toLowerCase())
				&& !requestImageURL.toLowerCase().contains("@");

	}

	static String authenticate(String requestImageURL) throws BPException {

		return requestImageURL.replace(HTTPS,
				HTTPS + ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.DHUS_USER.getParameter()) + ":"
						+ ConfigurationLoader.loadConfigurationParameter(BPStaticConfigurationParameters.DHUS_PWD.getParameter()) + "@");
	}

	/**
	 * Creates the command to download requestImageURL to targetFile
	 *
	 * @param requestImageURL
	 * @param targetFile
	 * @param ak
	 * @param sk
	 * @return
	 * @throws BPException
	 */
	public static List<String> createCommand(String requestImageURL, String targetFile, String ak, String sk, Optional<String> s3Url)
			throws BPException {

		logger.trace("Creating command to copy {} to {}", requestImageURL, targetFile);

		if (isS3URL(requestImageURL)) {

			if (isDirectory(requestImageURL)) {

				return createS3DirectoryCopyCommand(targetFile, requestImageURL, ak, sk, s3Url);
			}

			String signed = createS3SignedRequest(requestImageURL, ak, sk);

			String command = WGET_O + targetFile + " \"" + escapeForShellURL(signed) + "\"";

			logger.debug(DEBUG_COMMAND_COPY, COMMAND_TO_COPY, requestImageURL, targetFile, command);
			return Arrays.asList(command);

		} else {

			if (isUnAuthenticatedSentinelURL(requestImageURL)) {

				logger.debug("Adding credentials to sentinel download url {}", requestImageURL);

				requestImageURL = authenticate(requestImageURL);

				String command = WGET_O + targetFile + " \"" + escapeForShellURL(requestImageURL) + "\"";
				logger.debug(DEBUG_COMMAND_COPY, COMMAND_TO_COPY, requestImageURL, targetFile, command);
				return Arrays.asList(command);

			} else {
				String escaped = escapeForShellURL(requestImageURL);

				if (escaped != null) {

					String tf = targetFile;
					if (tf.endsWith("/"))
						tf += requestImageURL.split("/")[requestImageURL.split("/").length - 1];

					String command = WGET_O + tf + " \"" + escaped + "\"";

					logger.debug(DEBUG_COMMAND_COPY, COMMAND_TO_COPY, requestImageURL, targetFile, command);

					return Arrays.asList(command);
				}
			}
		}

		throw new BPException("Can't parse request " + requestImageURL, BPException.ERROR_CODES.INVALID_REQUEST);

	}

	public static String createReserveCommand() {
		String command =
				"echo -n \"for i in \"  >> idleScript.sh && echo -n \"$\"  >> idleScript.sh && echo -n \"(seq \"  >> idleScript.sh &&  "
						+ "echo -n \"$\"  >> idleScript.sh && echo \"(( \"  >> idleScript.sh &&"
						+ "echo -n \"$\"  >> idleScript.sh && echo \"(getconf _NPROCESSORS_ONLN) / 2)) ); do\" >> idleScript.sh && echo "
						+ "\"yes > /dev/null &\" >> idleScript.sh && echo \"done\" >> idleScript.sh && echo \"while true; do\" >> idleScript.sh && echo \"sleep 5m\" >> idleScript.sh && echo \"done\" >> idleScript.sh && chmod 777 idleScript.sh && less idleScript.sh && /bin/sh idleScript.sh"
						+ " chmod 777 idleScript.sh";

		command += " && /bin/sh idleScript.sh ";

		return command;
	}

	public static WebStorageObject toS3(File file, String bucketName, String key, String ak, String sk, String bucketRegion,
			Optional<String> s3Url) throws BPException {

		var s3Client = new StorageFactory().getWebStorage(bucketName);

		return s3Client.upload(file, key);
	}

	public static ContainerOrchestratorCommandResult setPermissions(Path destPath) {
		try {
			Set<PosixFilePermission> permissions = new HashSet<>();

			permissions.add(PosixFilePermission.GROUP_EXECUTE);
			permissions.add(PosixFilePermission.OWNER_EXECUTE);

			permissions.add(PosixFilePermission.GROUP_WRITE);
			permissions.add(PosixFilePermission.OWNER_WRITE);

			permissions.add(PosixFilePermission.GROUP_READ);
			permissions.add(PosixFilePermission.OWNER_READ);

			Files.setPosixFilePermissions(destPath, permissions);

			var result = new ContainerOrchestratorCommandResult();
			result.setSuccess(true);

			return result;
		} catch (IOException e) {
			logger.error("Error setting permissions of file {}", destPath.getFileName(), e);

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Error setting permissions of " + destPath.getFileName());

			return result;
		}
	}

	public static ContainerOrchestratorCommandResult copyLocalToLocal(Path sourcePath, Path destPath) {
		try {
			Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);

			return setPermissions(destPath);

		} catch (IOException e) {
			logger.error("Error copying file {} to {}", sourcePath.getFileName(), destPath.getFileName(), e);

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage("Error copying " + sourcePath.getFileName() + " to " + destPath.getFileName());

			return result;
		}
	}

	public static String createS3UploadCommand(String sourceFile, String bucket, String key, boolean publicread,
			Optional<String> s3server) {

		var acl = "";

		if (publicread)
			acl = " --acl public-read";

		return "/root/.local/bin/aws " + addS3Endpoint(s3server) + " s3 mv " + sourceFile + " s3://" + bucket + "/" + key + acl;

	}

	private static String addS3Endpoint(Optional<String> s3server) {

		if (!s3server.isPresent())
			return "";

		String url = s3server.get();

		return "--endpoint-url " + url;

	}

	public static String createS3UploadFolderCommand(String folderPath, String bucket, String s3BaseObjectKey, boolean publicread,
			Optional<String> s3server) {

		var acl = "";

		if (publicread)
			acl = " --acl public-read";

		return "cd " + folderPath + " && zip " + ARRAY_ZIP_FILE_NAME + " * && /root/.local/bin/aws " + addS3Endpoint(s3server)
				+ " s3 sync . " + "s3://" + bucket + "/" + s3BaseObjectKey + acl;

	}

	public static Number toNumber(String s) throws BPException {

		logger.trace("Converting {} to Number", s);

		try {

			var i = Integer.valueOf(s);

			logger.trace("{} is integer", s);

			return i;

		} catch (NumberFormatException nfe) {

			logger.trace("{} not an integer", s);

		}

		try {

			var d = Double.valueOf(s);

			logger.trace("{} is double", s);

			return d;

		} catch (NumberFormatException nfe) {

			logger.trace("{} not a double", s);

		}

		throw new BPException("Bad model parameter, expected number but found a non-parseable content " + s,
				BPException.ERROR_CODES.INVALID_REQUEST);
	}
}
