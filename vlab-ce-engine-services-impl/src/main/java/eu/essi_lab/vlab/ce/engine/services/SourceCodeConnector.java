package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.ce.engine.services.sourcecode.SourceCodeConventionParser;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class SourceCodeConnector implements ISourceCodeConnector {

	private Logger logger = LogManager.getLogger(SourceCodeConnector.class);

	private File dir;

	@Override
	public SupportResponse supports(BPRealization realization) {

		SupportResponse resp = new SupportResponse();

		setDir(new File(realization.getRealizationURI()));

		return new SourceCodeConventionParser().validate(getDir(), resp);

	}

	@Override
	public void deleteCodeFolder() {
		logger.debug("Delete source code folder requested, this connector skips this operation");
	}

	public File getDir() {
		return dir;
	}

	public void setDir(File dir) {
		this.dir = dir;
	}
}
