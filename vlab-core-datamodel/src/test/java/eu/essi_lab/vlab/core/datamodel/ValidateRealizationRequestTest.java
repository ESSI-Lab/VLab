package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class ValidateRealizationRequestTest {

	@Test
	public void test() throws BPException {
		String uri = "http://example.com/git";

		ValidateRealizationRequest request = new ValidateRealizationRequest();

		BPRealization realization = new BPRealization();

		realization.setRealizationURI(uri);

		request.setRealization(realization);

		String serialized = new JSONSerializer().serialize(request);

		System.out.println(serialized);

		ValidateRealizationRequest deserialized = new JSONDeserializer().deserialize(serialized, ValidateRealizationRequest.class);

		Assert.assertEquals(uri, deserialized.getRealization().getRealizationURI());

	}

	@Test
	public void deserialize() throws BPException {
		String req = "{\"realization\":{\"realizationURI\":\"pii\"},\"modelName\":\"mlmlm\",\"modelDescription\":\"mlml\","
				+ "\"modelDeveloper\":\"mlml\",\"modelDeveloperEmail\":\"mlml\",\"modelDeveloperOrg\":\"mlml\"}";

		ValidateRealizationRequest deserialized = new JSONDeserializer().deserialize(req, ValidateRealizationRequest.class);
	}

}