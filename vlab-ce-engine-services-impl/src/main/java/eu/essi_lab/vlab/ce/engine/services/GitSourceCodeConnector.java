package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.ce.engine.services.sourcecode.SourceCodeConventionParser;
import eu.essi_lab.vlab.core.utils.TrustedHttpClientBuilder;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;

/**
 * @author Mattia Santoro
 */
public class GitSourceCodeConnector extends SourceCodeConnector implements ISourceCodeConnector {

	public static final String GIT_ADAPTER_TEMP_DIRECTORY = "gitadapterclones";
	public static final String GIT_CLONE_DIRECTORY_PREFIX = "clone";
	private String gitURL;
	private String defaultBranch;
	private String commit;
	private final Logger logger = LogManager.getLogger(GitSourceCodeConnector.class);

	private int cloneTries = 0;

	public GitSourceCodeConnector() {

		String targetDirectory = System.getProperty("java.io.tmpdir") + File.separator + GIT_ADAPTER_TEMP_DIRECTORY + File.separator
				+ GIT_CLONE_DIRECTORY_PREFIX + "-" + UUID.randomUUID().toString() + File.separator;

		setDir(new File(targetDirectory));
	}

	@Override
	public SupportResponse supports(BPRealization realization) {

		SupportResponse resp = new SupportResponse();

		setGitURL(realization.getRealizationURI());

		try {

			cloneRepository();

		} catch (BPException e) {

			resp.setCanRead(false);
			resp.setConventionError("Can't clone from " + gitURL + " with error: " + e.getMessage());

			return resp;

		}

		resp.setCanRead(true);

		return validate(resp);

	}

	private void installAllTrustingManager() {

		try {
			TrustedHttpClientBuilder.installTrustingManager(TrustedHttpClientBuilder.createTrustManagerCerts());
		} catch (GeneralSecurityException e) {

			logger.warn("Exception installing trust manager", e);

		}
	}

	public void cloneRepository() throws BPException {

		if (getDir().exists()) {

			logger.debug("Code directory already exists. Deleting");

			deleteFolder(getDir());
		}

		installAllTrustingManager();

		String absPath = getDir().getAbsolutePath();

		logger.debug("Trying to clone {} to {}", gitURL, absPath);

		CloneCommand clone = initCloneCommand();

		try {

			executeCloneCommand(clone);

			logger.debug("Successfully cloned {} to {}", gitURL, absPath);

		} catch (GitAPIException e) {

			String msg = e.getMessage();

			logger.warn("Exception cloning from {} [{}] on try n. {} - verifying no space left on device", gitURL, msg, cloneTries);

			if (msg != null && msg.toLowerCase().contains("No space left on device".toLowerCase())) {

				logger.debug("Detected no space left, trying to clean tmp");

				if (cloneTries <= 3) {

					String tmp = System.getProperty("java.io.tmpdir") + File.separator;

					File directory = new File(tmp);

					File[] subdirs = directory.listFiles();

					for (File f : subdirs)
						deleteFolder(f);

					File d = new GitSourceCodeConnector().getDir();

					setDir(d);

					cloneTries = cloneTries + 1;

					cloneRepository();

					return;
				} else {
					logger.warn("Too many clone tries {} - returning unsupported", cloneTries);

					throw new BPException("Exception cloning from " + gitURL + " [" + msg + "] - returning unsupported",
							BPException.ERROR_CODES.ERROR_GIT_CLONE_NO_SPACE_LEFT);
				}
			} else {

				logger.debug("No space left was not detected, returning unsupported");

				throw new BPException("Exception cloning from " + gitURL + " [" + msg + "] - returning unsupported",
						BPException.ERROR_CODES.ERROR_GIT_CLONE);

			}

		}
	}

	public CloneCommand initCloneCommand() {
		return Git.cloneRepository().setURI(gitURL).setBranch(defaultBranch).setDirectory(getDir());
	}

	void executeCloneCommand(CloneCommand clone) throws GitAPIException {
		clone.call();
	}

	public SupportResponse validate(SupportResponse resp) {

		try {

			if (commit != null)
				switchToCommit(commit);

		} catch (BPException e) {

			logger.warn("Exception switching to commit {} [{}] - returning uspported", commit, e.getMessage());

			resp.setCanRead(false);
			resp.setConventionError("Unable to switch to commit " + commit + " - with error " + e.getMessage());

		}

		SupportResponse valid = validateDirContent(resp);

		if (!valid.isConventionsOk())
			deleteFolder(getDir());

		return valid;

	}

	private boolean deleteFile1(File file) {

		return file.delete();
	}

	private boolean deleteFile2(File file) {
		try {
			FileUtils.delete(file);
			return true;
		} catch (IOException e) {
			logger.warn("Failed to delete {}", file.getName());
		}

		return false;
	}

	protected void deleteFolder(File dir) {

		File[] files = dir.listFiles();

		for (File file : files) {

			logger.trace("Deleting {}", file.getAbsolutePath());

			if (file.isDirectory())
				deleteFolder(file);

			if (!deleteFile1(file))
				if (!deleteFile2(file))
					logger.error("Failed Delete {}", file.getName());
		}

		if (dir.exists()) {

			String dn = dir.getName();

			if (!deleteFile1(dir))
				if (!deleteFile2(dir))
					logger.error("Failed Delete {}", dir.getName());
		}

	}

	private SupportResponse validateDirContent(SupportResponse resp) {
		return new SourceCodeConventionParser().validate(getDir(), resp);
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	private void switchToCommit(String commit) throws BPException {

		try (Git git = Git.open(getDir())) {

			logger.info("Switching {} to commit {}", gitURL, commit);

			ObjectId obj = git.getRepository().resolve(commit);

			RevCommit rev = git.getRepository().parseCommit(obj);

			git.branchCreate().setName("tocheckout").setStartPoint(rev).call();

			git.checkout().setName("tocheckout").call();

			logger.info("Successufully switched {} to commit {}", gitURL, commit);

		} catch (GitAPIException | IOException e) {

			String msg = e.getMessage();

			logger.error("Can not switch {} to commit {}: {}", gitURL, commit, msg, e);
			throw new BPException("Can not switch " + gitURL + " to commit " + commit + ": " + msg);

		}

	}

	public void setDefaultBranch(String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	public String getDefaultBranch() {
		return this.defaultBranch;
	}

	public void setGitURL(String gitURL) {
		this.gitURL = gitURL;
	}

	public String getGitURL() {
		return this.gitURL;
	}

	@Override
	public void deleteCodeFolder() {

		logger.debug("Delete source code folder requested");

		deleteFolder(getDir());
	}

}
