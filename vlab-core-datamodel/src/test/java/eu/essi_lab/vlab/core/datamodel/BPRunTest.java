package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.io.InputStream;
import java.util.Date;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Mattia Santoro
 */
public class BPRunTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testDeserializeOldRuns() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("nouserRun.json");

		BPRun deserialized = new JSONDeserializer().deserialize(stream, BPRun.class);

		Assert.assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", deserialized.getRunid());

		Assert.assertNull(deserialized.getOwner());

		Assert.assertNull(deserialized.getExecutionInfrastructure());

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", deserialized.getWorkflowid());

		Assert.assertEquals(3, deserialized.getInputs().size());

		Assert.assertFalse(deserialized.isPublicRun());
	}

	@Test
	public void testDeserializeNewRuns() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithUser.json");

		BPRun deserialized = new JSONDeserializer().deserialize(stream, BPRun.class);

		Assert.assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", deserialized.getRunid());

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", deserialized.getWorkflowid());

		Assert.assertEquals(3, deserialized.getInputs().size());

		Assert.assertEquals("owner@mail.com", deserialized.getOwner());

		Assert.assertFalse(deserialized.isPublicRun());

	}

	@Test
	public void testDeserializeNewRunsWithInfra() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithUserInfra.json");

		BPRun deserialized = new JSONDeserializer().deserialize(stream, BPRun.class);

		Assert.assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", deserialized.getRunid());

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", deserialized.getWorkflowid());

		Assert.assertEquals(3, deserialized.getInputs().size());

		Assert.assertEquals("owner@mail.com", deserialized.getOwner());

		Assert.assertFalse(deserialized.isPublicRun());

		Assert.assertEquals("cloud3", deserialized.getExecutionInfrastructure());

	}

	@Test
	public void testDeserializeNewRunsPublic() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithUserPublic.json");

		BPRun deserialized = new JSONDeserializer().deserialize(stream, BPRun.class);

		Assert.assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", deserialized.getRunid());

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", deserialized.getWorkflowid());

		Assert.assertEquals(3, deserialized.getInputs().size());

		Assert.assertEquals("owner@mail.com", deserialized.getOwner());

		Assert.assertTrue(deserialized.isPublicRun());

		Assert.assertTrue(deserialized.getSharedWith().isEmpty());

	}

	@Test
	public void testDeserializeNewRunsPublicShared() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithUserPublicShared.json");

		BPRun deserialized = new JSONDeserializer().deserialize(stream, BPRun.class);

		Assert.assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", deserialized.getRunid());

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", deserialized.getWorkflowid());

		Assert.assertEquals(3, deserialized.getInputs().size());

		Assert.assertEquals("owner@mail.com", deserialized.getOwner());

		Assert.assertTrue(deserialized.isPublicRun());

		Assert.assertEquals(3, deserialized.getSharedWith().size());

		Assert.assertEquals("shared2@mail.com", deserialized.getSharedWith().get(1));

		deserialized.shareWithUser("newshare@mail.com");

		Assert.assertEquals(4, deserialized.getSharedWith().size());

		BPRun d2 = new JSONDeserializer().deserialize(new JSONSerializer().serialize(deserialized), BPRun.class);

		Assert.assertEquals(4, d2.getSharedWith().size());

		Assert.assertEquals("newshare@mail.com", deserialized.getSharedWith().get(3));

		d2.deleteShareWithUser("shared2@mail");

		Assert.assertEquals(4, d2.getSharedWith().size());

		d2.deleteShareWithUser("shared2@mail.com");

		Assert.assertEquals(3, d2.getSharedWith().size());

		BPRun d3 = new JSONDeserializer().deserialize(new JSONSerializer().serialize(d2), BPRun.class);

		Assert.assertEquals(3, d3.getSharedWith().size());
	}

	@Test
	public void addTooManyUsers() throws BPException {

		expectedException.expect(BPException.class);

		BPRun run = new BPRun();

		run.deleteShareWithUser("pippo");

		for (int i = 0; i < 102; i++) {

			run.shareWithUser("user" + i + "@mail.com");

		}
	}

	@Test
	public void testDeserializeNewRunsPublicSharedWithDate() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runWithUserPublicShared.json");

		BPRun deserialized = new JSONDeserializer().deserialize(stream, BPRun.class);

		Assert.assertEquals("00e4d805-bcd5-489d-a1ea-e67a8dbd6889", deserialized.getRunid());

		Assert.assertEquals("http://eu.essi_lab.core/test/wfid", deserialized.getWorkflowid());

		Assert.assertEquals(3, deserialized.getInputs().size());

		Assert.assertEquals("owner@mail.com", deserialized.getOwner());

		Assert.assertTrue(deserialized.isPublicRun());

		Assert.assertEquals(3, deserialized.getSharedWith().size());

		Assert.assertEquals("shared2@mail.com", deserialized.getSharedWith().get(1));

		deserialized.shareWithUser("newshare@mail.com");

		Assert.assertEquals(4, deserialized.getSharedWith().size());

		deserialized.setCreationTime(new Date().getTime());

		String s2 = new JSONSerializer().serialize(deserialized);

		System.out.println(s2);

		BPRun d2 = new JSONDeserializer().deserialize(s2, BPRun.class);

		Assert.assertNotNull(d2.getCreationTime());

		Assert.assertNotNull(d2.getEsCreationTime());

		Assert.assertEquals(4, d2.getSharedWith().size());

		Assert.assertEquals("newshare@mail.com", deserialized.getSharedWith().get(3));

		d2.deleteShareWithUser("shared2@mail");

		Assert.assertEquals(4, d2.getSharedWith().size());

		d2.deleteShareWithUser("shared2@mail.com");

		Assert.assertEquals(3, d2.getSharedWith().size());

		BPRun d3 = new JSONDeserializer().deserialize(new JSONSerializer().serialize(d2), BPRun.class);

		Assert.assertEquals(3, d3.getSharedWith().size());
	}

}