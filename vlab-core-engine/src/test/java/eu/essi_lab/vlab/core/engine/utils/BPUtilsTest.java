package eu.essi_lab.vlab.core.engine.utils;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.VLabDockerContainer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class BPUtilsTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	private String user = "user";
	private String pwd = "fake";
	private String ouser;
	private String opwd;

	@Before
	public void before() throws IOException {
		ouser = System.getenv("eu.essi_lab.vlab.scihub.user");
		opwd = System.getenv("eu.essi_lab.vlab.scihub.pwd");

		environmentVariables.set("eu.essi_lab.vlab.scihub.user", user);
		environmentVariables.set("eu.essi_lab.vlab.scihub.pwd", pwd);
	}

	@After
	public void after() throws IOException {
		
		environmentVariables.set("eu.essi_lab.vlab.scihub.user", ouser);
		environmentVariables.set("eu.essi_lab.vlab.scihub.pwd", opwd);
	}

	@Test
	public void testCreateCoimmand() {
		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		List<String> commands = Arrays.asList("cmd1 arg1");

		Mockito.doReturn(commands).when(container).getCommand();

		List<String> created = BPUtils.createCommand(container, "");

		Assert.assertEquals(1, created.size());

		Assert.assertEquals("cmd1 arg1", created.get(0));
	}

	@Test
	public void testCreateCoimmand3() {
		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		List<String> commands = Arrays.asList("cmd1 arg1 && cmd2 arg2");

		Mockito.doReturn(commands).when(container).getCommand();

		List<String> created = BPUtils.createCommand(container, null);

		Assert.assertEquals(1, created.size());

		Assert.assertEquals("cmd1 arg1 && cmd2 arg2", created.get(0));
	}

	@Test
	public void testCreateCoimmand3_1() {
		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		List<String> commands = Arrays.asList("cmd1 arg1 && cmd2 arg2");

		Mockito.doReturn(commands).when(container).getCommand();

		List<String> created = BPUtils.createCommand(container, "");

		Assert.assertEquals(1, created.size());

		Assert.assertEquals("cmd1 arg1 && cmd2 arg2", created.get(0));
	}

	@Test
	public void testCreateCoimmand2() {
		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		List<String> commands = Arrays.asList("source activate testenv", "changeDetectionRun.sh");

		Mockito.doReturn(commands).when(container).getCommand();

		List<String> created = BPUtils.createCommand(container, "runfolder");

		Assert.assertEquals(1, created.size());

		Assert.assertEquals("cd runfolder && source activate testenv && changeDetectionRun.sh", created.get(0));
	}

	@Test
	public void extractFromFilePath() {
		String targetPath = "/dir/path/file.dat";

		List<String> list = BPUtils.extractDirs(targetPath);

		List<String> expectedList = Arrays.asList(new String[] { "/dir", "/dir/path" });

		assertEquals(expectedList, list);

	}

	@Test
	public void extractFromDirectoryPath() {
		String targetPath = "/dir/path/folder/";

		List<String> list = BPUtils.extractDirs(targetPath);

		List<String> expectedList = Arrays.asList(new String[] { "/dir", "/dir/path", "/dir/path/folder" });

		assertEquals(expectedList, list);

	}

	@Test
	public void testCreateSignedRequest() {

		String s3Req = "https://s3.amazonaws.com/testb/folder/P1/Thematic/Dbaresur.kea";

		String bucket = "testb";

		String objkey = "folder/P1/Thematic/Dbaresur.kea";

		assertTrue(BPUtils.isS3URL(s3Req));

		assertEquals(bucket, BPUtils.extractBucketNameFromURL(s3Req));

		assertEquals(objkey, BPUtils.extractObjectKeyFromURL(s3Req));

		String signed = BPUtils.createS3SignedRequest(s3Req, "ptest", "plutest", "region");

		System.out.println(signed);

	}

	@Test
	public void testCreateSignedRequest2() {

		String s3Req = "https://testbucketname.s3.amazonaws.com/folder/P1/Thematic/Dbaresur.kea";

		String bucket = "testbucketname";

		String objkey = "folder/P1/Thematic/Dbaresur.kea";

		assertTrue(BPUtils.isS3URL(s3Req));

		assertEquals(bucket, BPUtils.extractBucketNameFromURL(s3Req));

		assertEquals(objkey, BPUtils.extractObjectKeyFromURL(s3Req));

		String signed = BPUtils.createS3SignedRequest(s3Req, "ptest", "plutest", "region");

		System.out.println(signed);

	}

	@Test
	public void testSentinelNoAuthentication() throws BPException {

		String sentinelReq = "https://scihub.copernicus.eu/dhus/odata/v1/Products('f892e68e-624d-43c7-ad0e-e76e24648364')/$value";
		String auth = "https://" + user + ":" + pwd + "@scihub.copernicus.eu/dhus/odata/v1/Products('f892e68e-624d-43c7-ad0e-e76e24648364')"
				+ "/$value";

		assertTrue(BPUtils.isUnAuthenticatedSentinelURL(sentinelReq));

		String authenticated = BPUtils.authenticate(sentinelReq);
		System.out.println(authenticated);

		assertEquals(auth, authenticated);

	}

	@Test
	public void testUnauthSentinelCreateCommand() throws BPException {

		String s2link = "https://scihub.copernicus.eu/dhus/odata/v1/Products('25a3229e-da8c-4cbc-bb99-e464e0a4bf1c')/$value";

		List<String> command = BPUtils.createCommand(s2link, "s2.zip", "ptest", "plutest", Optional.empty());

		assertEquals(1, command.size());

		assertEquals(
				"wget -O s2.zip \"https://user:fake@scihub.copernicus.eu/dhus/odata/v1/Products('25a3229e-da8c-4cbc-bb99-e464e0a4bf1c')/\\$value\"",
				command.get(0));

	}

	@Test
	public void testAuthSentinelCreateCommand() throws BPException {

		String s2link = "https://pippo:plutest@scihub.copernicus.eu/dhus/odata/v1/Products('25a3229e-da8c-4cbc-bb99-e464e0a4bf1c')/$value";

		List<String> command = BPUtils.createCommand(s2link, "s2.zip", "ptest", "plutest", Optional.empty());

		assertEquals(1, command.size());

		assertEquals(
				"wget -O s2.zip \"https://pippo:plutest@scihub.copernicus.eu/dhus/odata/v1/Products('25a3229e-da8c-4cbc-bb99-e464e0a4bf1c')/\\$value\"",
				command.get(0));

	}

	@Test
	public void testS3FileCreateCommand() throws BPException {

		String s2link = "https://s3.amazonaws.com/bucketname/file.dat";

		List<String> command = BPUtils.createCommand(s2link, "s2.zip", "ptest", "plutest", Optional.empty());

		assertEquals(1, command.size());

		//this is to be checked
		assertEquals("wget -O s2.zip \"https://s3.amazonaws.com/bucketname/file.dat\"", command.get(0));

	}

	@Test
	public void testHttpFileWithFolderTarget() throws BPException {

		String s2link = "https://awstestshare.s3-eu-west-1.amazonaws.com/test.csv";

		List<String> command = BPUtils.createCommand(s2link, "/abs/folder/", "ptest", "plutest", Optional.empty());

		assertEquals(1, command.size());

		//this is to be checked
		assertEquals("wget -O /abs/folder/test.csv \"https://awstestshare.s3-eu-west-1.amazonaws.com/test.csv\"",
				command.get(0));

	}

	@Test
	public void test4() throws BPException {

		String file = "p.json";

		String value = "value2";
		String key = "key2";
		String result = BPUtils.createCommand(key, value, file, "dirScript.sh");

		assertEquals(
				"echo \"if [ ! -e \\$1 ]; then\" > dirScript.sh && echo \"echo {} > \\$1\" >> dirScript.sh && echo \"fi\" >> dirScript.sh && echo \"sed -i 's/}/,\\\"key2\\\":\\\"value2\\\",}/g' \\$1\" >> dirScript.sh&& echo \"sed -i 's/,}/}/g' \\$1\" >> dirScript.sh&& echo \"sed -i 's/{,/{/g' \\$1\" >> dirScript.sh && chmod 777 dirScript.sh && /bin/sh dirScript.sh p.json",
				result);

	}

	@Test
	public void test5() throws BPException {

		String file = "p.json";

		String value = "ndvi<0.15&ndbi>0";
		String key = "artific_urb_cat";
		String result = BPUtils.createCommand(key, value, file, "dirScript.sh");

		assertEquals(

				"echo \"if [ ! -e \\$1 ]; then\" > dirScript.sh && echo \"echo {} > \\$1\" >> dirScript.sh && echo \"fi\" >> dirScript.sh"
						+ " && echo \"sed -i 's/}/,\\\"artific_urb_cat\\\":\\\"ndvi\\<0.15\\&ndbi\\>0\\\",}/g' \\$1\" >> dirScript.sh&& "
						+ "echo \"sed -i 's/,}/}/g' \\$1\" >> dirScript.sh&& echo \"sed -i 's/{,/{/g' \\$1\" >> dirScript.sh && chmod 777 dirScript.sh && /bin/sh dirScript.sh p.json",
				result);

	}

	@Test
	public void testToText() throws BPException {

		String s = "7";
		assertEquals(s, BPUtils.toText(BPUtils.toNumber(s)));

		s = "0.4";
		assertEquals(s, BPUtils.toText(BPUtils.toNumber(s)));

		s = "1.30978979";
		assertEquals(s, BPUtils.toText(BPUtils.toNumber(s)));

		s = ".6";
		assertEquals("0" + s, BPUtils.toText(BPUtils.toNumber(s)));

	}

	@Test
	public void testlocalcmd() throws BPException {

		BPUtils.executeLocalCommand("ls -l", "Error ls -l");
	}

	@Test
	public void testCreateReserveCommand() {

		assertEquals(
				"echo -n \"for i in \"  >> idleScript.sh && echo -n \"$\"  >> idleScript.sh && echo -n \"(seq \"  >> idleScript.sh &&  "
						+ "echo -n \"$\"  >> idleScript.sh && echo \"(( \"  >> idleScript.sh &&echo -n \"$\"  >> idleScript.sh && echo \"(getconf _NPROCESSORS_ONLN) / 2)) ); do\" >> idleScript.sh && echo \"yes > /dev/null &\" >> idleScript.sh && echo \"done\" >> idleScript.sh && echo \"while true; do\" >> idleScript.sh && echo \"sleep 5m\" >> idleScript.sh && echo \"done\" >> idleScript.sh && chmod 777 idleScript.sh && less idleScript.sh && /bin/sh idleScript.sh chmod 777 idleScript.sh && /bin/sh idleScript.sh ",
				BPUtils.createReserveCommand());

	}

}