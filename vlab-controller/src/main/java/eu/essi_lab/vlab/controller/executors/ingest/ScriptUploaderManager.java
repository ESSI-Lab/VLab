package eu.essi_lab.vlab.controller.executors.ingest;

import eu.essi_lab.vlab.controller.services.IContainerOrchestratorCommandExecutor;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.ContainerOrchestratorCommandResult;
import eu.essi_lab.vlab.core.datamodel.PathType;
import eu.essi_lab.vlab.core.datamodel.Script;
import eu.essi_lab.vlab.core.engine.conventions.PathConventionParser;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class ScriptUploaderManager {

	private ISourceCodeConventionFileLoader loader;
	private PathConventionParser pathParser;
	private IContainerOrchestratorCommandExecutor dockerHost;

	private Logger logger = LogManager.getLogger(ScriptUploaderManager.class);

	public ContainerOrchestratorCommandResult ingest(Script script) {

		List<File> files = null;

		try {

			files = loader.getScriptFiles(script);

		} catch (FileNotFoundException e) {

			String path = script.getRepoPath();

			logger.error("Could not load file: {}", path, e);

			var result = new ContainerOrchestratorCommandResult();

			result.setSuccess(false);

			result.setMessage(e.getMessage());

			return result;
		}

		String target = pathParser.getDockerContainerScriptAbsolutePath(script);

		for (File file : files) {

			String dest = target;

			if (script.getPathType().compareTo(PathType.DIRECTORY) == 0)
				dest = handleDirectoryPath(script, dest, file);

			try {

				var sourcePath = file.toPath();

				var targetPath = new File(dest).toPath();

				logger.debug("Copying from {} to {}", sourcePath, targetPath);

				doCopyFile(sourcePath, targetPath);

			} catch (BPException e) {
				logger.error("Error copying script {}", script.getRepoPath(), e);

				var result = new ContainerOrchestratorCommandResult();

				result.setSuccess(false);

				result.setMessage("Error copying " + script.getRepoPath());

				return result;
			}

		}

		var result = new ContainerOrchestratorCommandResult();
		result.setSuccess(true);
		return result;
	}

	void doCopyFile(Path sourcePath, Path targetPath) throws BPException {

		ContainerOrchestratorCommandResult res = dockerHost.copyFileTo(new File(sourcePath.toString()), targetPath.toString(), 1000L * 60 * 5);

		if (!res.isSuccess()) {
			throw new BPException("Error copying " + sourcePath, BPException.ERROR_CODES.SCRIPT_COPY_ERROR.getCode());
		}

	}

	private String handleDirectoryPath(Script script, String dest, File file) {

		String repoPath = script.getRepoPath();

		String[] split = file.getAbsolutePath().split(repoPath + File.separator);

		if (split.length > 1) {
			return dest + File.separator + split[1];
		}

		return dest + File.separator + file.getName();
	}

	public PathConventionParser getPathParser() {
		return pathParser;
	}

	public void setPathParser(PathConventionParser pathParser) {
		this.pathParser = pathParser;
	}

	public IContainerOrchestratorCommandExecutor getDockerHost() {
		return dockerHost;
	}

	public void setDockerHost(IContainerOrchestratorCommandExecutor dockerHost) {
		this.dockerHost = dockerHost;
	}

	public ISourceCodeConventionFileLoader getLoader() {
		return loader;
	}

	public void setLoader(ISourceCodeConventionFileLoader loader) {
		this.loader = loader;
	}
}
