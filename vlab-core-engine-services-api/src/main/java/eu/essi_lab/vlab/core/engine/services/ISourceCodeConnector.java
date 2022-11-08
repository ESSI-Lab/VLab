package eu.essi_lab.vlab.core.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import java.io.File;

/**
 * @author Mattia Santoro
 */
public interface ISourceCodeConnector {
	File getDir();

	SupportResponse supports(BPRealization realization);

	void deleteCodeFolder();
}
