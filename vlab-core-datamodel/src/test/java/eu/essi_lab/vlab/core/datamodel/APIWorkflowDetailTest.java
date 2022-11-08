package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class APIWorkflowDetailTest {

	@Test
	public void deserialize() throws BPException {

		String serialized = "{\n" + "  \"id\": \"http://eu.essi_lab.core/test/wfid\",\n"
				+ "  \"bpmn_url\": \"http://ecovrp.geodab.eu/ecopotential-vrp/workflows/eodesm1.bpmn\",\n"
				+ "  \"name\": \"Earth Observation Data for Ecosystem Monitoring (EODESM)\",\n"
				+ "  \"description\": \"The EODESM system classifies land covers according to the Food and Agricultural Organisation’s (FAO’s) Land Cover Classification System (LCCS2) taxonomy. The EODESM system can use, as input, any remote sensing or other spatial datasets (including modelled output) and at any scale of choosing.  The system is designed for use by a wide range of users and is entirely open source and freely available. This document provides a simple summary allowing users to access and easily use the EODESM system.\"\n"
				+ "}";

		APIWorkflowDetail workflow = new JSONDeserializer().deserialize(serialized, APIWorkflowDetail.class);

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", workflow.getId());

		Assert.assertEquals("http://ecovrp.geodab.eu/ecopotential-vrp/workflows/eodesm1.bpmn", workflow.getBpmn_url());

		Assert.assertFalse(workflow.isSharedwithrequester());
		Assert.assertFalse(workflow.isOwnedbyrequester());
	}

	@Test
	public void deserialize2() throws BPException {

		String serialized = "{\n" + "  \"id\": \"http://eu.essi_lab.core/test/wfid\",\n"
				+ "  \"bpmn_url\": \"http://ecovrp.geodab.eu/ecopotential-vrp/workflows/eodesm1.bpmn\",\n"
				+ "  \"name\": \"Earth Observation Data for Ecosystem Monitoring (EODESM)\",\n" + "  \"ownedbyrequester\": true,\n"
				+ "  \"description\": \"The EODESM system classifies land covers according to the Food and Agricultural Organisation’s (FAO’s) Land Cover Classification System (LCCS2) taxonomy. The EODESM system can use, as input, any remote sensing or other spatial datasets (including modelled output) and at any scale of choosing.  The system is designed for use by a wide range of users and is entirely open source and freely available. This document provides a simple summary allowing users to access and easily use the EODESM system.\"\n"
				+ "}";

		APIWorkflowDetail workflow = new JSONDeserializer().deserialize(serialized, APIWorkflowDetail.class);

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", workflow.getId());

		Assert.assertEquals("http://ecovrp.geodab.eu/ecopotential-vrp/workflows/eodesm1.bpmn", workflow.getBpmn_url());

		Assert.assertFalse(workflow.isSharedwithrequester());
		Assert.assertTrue(workflow.isOwnedbyrequester());
	}
}