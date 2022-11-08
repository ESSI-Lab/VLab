package eu.essi_lab.vlab.ce.engine.services.rdf;

import eu.essi_lab.vlab.core.datamodel.BPUser;
import java.util.Arrays;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Mattia Santoro
 */
public class RDFQueryBuilderTest {

	@Test
	public void test() {

		String expected = "PREFIX : <http://essi-lab.eu/2020/02/d2k/core#>\n" + "PREFIX eskos: <http://essi-lab.eu/2020/02/d2k/eskos#>\n"
				+ "PREFIX dce: <http://purl.org/dc/elements/>\n"
				+ "CONSTRUCT { <http://eu.essi_lab.vlab.core/workflow/example-123-process> <http://www.w3.org/2000/01/rdf-schema#label> ?l0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> <http://purl.org/dc/terms/abstract> ?d0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> dce:creator ?cr0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:described_by ?db0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:modelDeveloperEmail ?mde0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:modelDeveloperOrg ?mdo0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:under_test ?ut0 .\n"
				+ "<http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:sharedWith ?sh0 . }\n"
				+ "WHERE { <http://eu.essi_lab.vlab.core/workflow/example-123-process> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t0 .\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> <http://www.w3.org/2000/01/rdf-schema#label> ?l0 . }\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> <http://purl.org/dc/terms/abstract> ?d0 . }\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> dce:creator ?cr0 . }\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:described_by ?db0 . }\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:modelDeveloperEmail ?mde0 . }\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:modelDeveloperOrg ?mdo0 . }\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:under_test ?ut0 . }\n"
				+ "OPTIONAL { <http://eu.essi_lab.vlab.core/workflow/example-123-process> eskos:sharedWith ?sh0 . } }\n";

		RDFQueryBuilder builder = new RDFQueryBuilder();

		RepositoryConnection connection = Mockito.mock(RepositoryConnection.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String query = (String) invocationOnMock.getArguments()[1];

				Assert.assertEquals(expected, query);

				return null;
			}
		}).when(connection).prepareQuery(Mockito.any(), Mockito.any());

		builder.resourceDetailsQuery(connection,
				Arrays.asList("http://eu.essi_lab.vlab.core/workflow/example-123-process"));

		Mockito.verify(connection, Mockito.times(1)).prepareGraphQuery(Mockito.any(), Mockito.any());

	}

	@Test
	public void test2() {

		String expected = "PREFIX : <http://essi-lab.eu/2020/02/d2k/core#>\n" + "PREFIX eskos: <http://essi-lab.eu/2020/02/d2k/eskos#>\n"
				+ "PREFIX dce: <http://purl.org/dc/elements/>\n"
				+ "SELECT ( COUNT( ?identifier ) AS ?cnt ) WHERE { SELECT DISTINCT ?identifier\n"
				+ "WHERE { { { ?identifier eskos:under_test true ;\n"
				+ "    eskos:modelDeveloperEmail \"dev@email.com\" . } UNION { ?identifier eskos:under_test false . } UNION { ?identifier eskos:sharedWith \"dev@email.com\" ;\n"
				+ "    eskos:under_test true . }\n" + "OPTIONAL { ?identifier <http://www.w3.org/2000/01/rdf-schema#label> ?name . }\n"
				+ "OPTIONAL { ?identifier <http://purl.org/dc/terms/abstract> ?desc . }\n"
				+ "FILTER ( ( REGEX( STR( ?name ), \"testbp\", \"i\" ) || REGEX( STR( ?desc ), \"testbp\", \"i\" ) ) ) } }\n"
				+ "GROUP BY ?identifier\n" + "}";

		RDFQueryBuilder builder = new RDFQueryBuilder();

		RepositoryConnection connection = Mockito.mock(RepositoryConnection.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String query = (String) invocationOnMock.getArguments()[1];

				Assert.assertEquals(expected, query);

				return null;
			}
		}).when(connection).prepareTupleQuery(Mockito.any(), Mockito.any());

		BPUser user = new BPUser();
		user.setEmail("dev@email.com");
		builder.countQuery("testbp", true, user, connection);

		Mockito.verify(connection, Mockito.times(1)).prepareTupleQuery(Mockito.any(), Mockito.any());
	}

	@Test
	public void test3() {

		String expected = "PREFIX : <http://essi-lab.eu/2020/02/d2k/core#>\n" + "PREFIX eskos: <http://essi-lab.eu/2020/02/d2k/eskos#>\n"
				+ "PREFIX dce: <http://purl.org/dc/elements/>\n" + "SELECT DISTINCT ?identifier\n"
				+ "WHERE { { { ?identifier eskos:under_test true ;\n"
				+ "    eskos:modelDeveloperEmail \"dev@email.com\" . } UNION { ?identifier eskos:under_test false . } UNION { ?identifier eskos:sharedWith \"dev@email.com\" ;\n"
				+ "    eskos:under_test true . }\n" + "OPTIONAL { ?identifier <http://www.w3.org/2000/01/rdf-schema#label> ?name . }\n"
				+ "OPTIONAL { ?identifier <http://purl.org/dc/terms/abstract> ?desc . }\n"
				+ "FILTER ( ( REGEX( STR( ?name ), \"testbp\", \"i\" ) || REGEX( STR( ?desc ), \"testbp\", \"i\" ) ) ) } }\n"
				+ "GROUP BY ?identifier\n" + "LIMIT 3\n" + "OFFSET 0\n";

		RDFQueryBuilder builder = new RDFQueryBuilder();

		RepositoryConnection connection = Mockito.mock(RepositoryConnection.class);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				String query = (String) invocationOnMock.getArguments()[1];

				Assert.assertEquals(expected, query);

				return null;
			}
		}).when(connection).prepareTupleQuery(Mockito.any(), Mockito.any());

		BPUser user = new BPUser();
		user.setEmail("dev@email.com");
		builder.matchQuery("testbp", 0, 3, true, user, connection);

		Mockito.verify(connection, Mockito.times(1)).prepareTupleQuery(Mockito.any(), Mockito.any());
	}
}