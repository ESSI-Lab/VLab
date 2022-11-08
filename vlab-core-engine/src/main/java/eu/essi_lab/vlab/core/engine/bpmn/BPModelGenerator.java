package eu.essi_lab.vlab.core.engine.bpmn;

import eu.essi_lab.vlab.core.datamodel.AtomicExecutableBP;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPIOObject;
import eu.essi_lab.vlab.core.datamodel.BPMNConventionParser;
import eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationRequest;
import eu.essi_lab.vlab.core.engine.factory.ISourceCodeConectorFactory;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPModelGenerator {

	private Logger logger = LogManager.getLogger(BPModelGenerator.class);
	private final AtomicExecutableBP bpmn;

	public BPModelGenerator() {

		bpmn = new AtomicExecutableBP(new BPMNConventionParser());

	}

	public AtomicExecutableBP createModel(ValidateRealizationRequest request) throws BPException {

		logger.debug("Model generation start");

		logger.debug("Setting realization {}", request.getRealization().getRealizationURI());

		bpmn.setBPRealization(request.getRealization());

		String modelName = request.getModelName();

		if (modelName != null) {

			logger.debug("Setting model name {}", modelName);

			bpmn.setExecutableTaskLabel(modelName);
		} else {
			logger.debug("Found null model name, laving default");
		}

		logger.debug("Loading adapter");

		ISourceCodeConnector adapter = getAdapter(request);

		if (adapter != null) {

			logger.debug("Enriching from SourceCode Adapter");

			enrich(bpmn, adapter);

		}

		logger.debug("Model generation done");

		return bpmn;
	}

	private void enrich(AtomicExecutableBP bpmn, ISourceCodeConnector adapter) throws BPException {

		ISourceCodeConventionFileLoader loader = getISourceCodeConventionFileLoader(adapter.getDir().getAbsolutePath());

		BPIOObject file;

		try {

			file = loader.loadIOFile();

		} catch (BPException e) {
			logger.error("Error reading IO file from {}", adapter.getDir().getAbsolutePath(), e);

			BPException ex = new BPException("Error reading IO file from " + adapter.getDir().getAbsolutePath(),
					BPException.ERROR_CODES.IO_FILE_NOT_FOUND);

			if (e.getErroCode() - BPException.ERROR_CODES.IO_FILE_NOT_FOUND.getCode() == 0)
				ex.setUserMessage("Can't find " + BPSourceCodeConventions.CONVENTION_FOLDER + "/" + BPSourceCodeConventions.IO_FILE_NAME);

			if (e.getErroCode() - BPException.ERROR_CODES.UNPARSABLE_JSON.getCode() == 0)
				ex.setUserMessage("Error parsing IO file");

			throw ex;
		}

		List<BPInputDescription> inputs = file.getInputs();

		List<String> exisyingIds = new ArrayList<>();

		Boolean[] doubleEntry = new Boolean[] { false };

		String[] doubleId = new String[1];

		inputs.forEach(input -> {
			if (exisyingIds.contains(input.getInput().getId())) {
				doubleEntry[0] = true;
				doubleId[0] = input.getInput().getId();
			}

			exisyingIds.add(input.getInput().getId());
		});

		if (doubleEntry[0]) {

			logger.error("Found double entry in input list: {}", doubleId[0]);

			BPException ex = new BPException("Found double entry in input list", BPException.ERROR_CODES.DOUBLE_INPUT_ID);

			ex.setUserMessage("Some inputs have the same identifier (" + doubleId[0] + ") in " + BPSourceCodeConventions.IO_FILE_NAME);

			throw ex;

		}

		for (BPInputDescription inputDescription : inputs)
			bpmn.addInput(inputDescription, bpmn.getExecutableTasks().iterator().next());

		List<BPOutputDescription> outputs = file.getOutputs();

		for (BPOutputDescription outputDescription : outputs)
			bpmn.addOutput(outputDescription, bpmn.getExecutableTasks().iterator().next());

	}

	public ISourceCodeConnector getAdapter(ValidateRealizationRequest request) throws BPException {
		return new ISourceCodeConectorFactory().getConnector(request.getRealization());
	}

	public ISourceCodeConventionFileLoader getISourceCodeConventionFileLoader(String path) throws BPException {
		return new ISourceCodeConectorFactory().getSourceCodeConventionFileLoader(path);
	}
}
