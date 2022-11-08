package eu.essi_lab.vlab.controller.factory;

import eu.essi_lab.vlab.controller.serviceloader.BPControllerServiceLoader;
import eu.essi_lab.vlab.controller.services.IndividualInputIngestor;
import eu.essi_lab.vlab.controller.services.OutputIngestor;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import java.util.Iterator;
import java.util.Optional;

/**
 * @author Mattia Santoro
 */
public class IngestorFactory {

	public Optional<IndividualInputIngestor> getIngestor(BPInputDescription input) {

		Iterator<IndividualInputIngestor> ingestorIt = BPControllerServiceLoader.load(IndividualInputIngestor.class).iterator();

		while (ingestorIt.hasNext()) {
			IndividualInputIngestor next = ingestorIt.next();
			if (next.canIngest(input))
				return Optional.of(next);
		}

		return Optional.empty();
	}

	public Optional<OutputIngestor> getIngestor(BPOutputDescription output) {

		Iterator<OutputIngestor> ingestorIt = BPControllerServiceLoader.load(OutputIngestor.class).iterator();

		while (ingestorIt.hasNext()) {
			OutputIngestor next = ingestorIt.next();
			if (next.canIngest(output))
				return Optional.of(next);
		}

		return Optional.empty();
	}
}
