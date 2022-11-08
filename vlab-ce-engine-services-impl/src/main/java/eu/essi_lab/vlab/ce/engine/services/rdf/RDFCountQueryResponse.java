package eu.essi_lab.vlab.ce.engine.services.rdf;

import java.util.Iterator;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

/**
 * @author Mattia Santoro
 */
public class RDFCountQueryResponse {
	private Integer count = 0;

	public RDFCountQueryResponse(TupleQueryResult ev) {

		Iterator<BindingSet> it = ev.iterator();

		if (it.hasNext()) {
			Binding binding = it.next().getBinding("cnt");

			if (binding != null) {
				count = Integer.valueOf(binding.getValue().stringValue());
			}
		}

	}

	public Integer getCount() {
		return count;
	}

}
