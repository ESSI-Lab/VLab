package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.engine.services.IESHttpResponseReader;
import java.util.Optional;

/**
 * @author Mattia Santoro
 */
public class QueryDocumentParserFactory {

	public static QueryDocumentParser getParser(IESHttpResponseReader reader, Optional<String> version) throws BPException {

		if (version.isPresent()) {

			String v = version.get();

			if (v.startsWith("7"))
				return new QueryDocumentParser7(reader);
		}

		return new QueryDocumentParser(reader);
	}
}
