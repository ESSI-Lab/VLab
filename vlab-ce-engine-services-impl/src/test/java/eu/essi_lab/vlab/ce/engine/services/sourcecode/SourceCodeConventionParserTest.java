package eu.essi_lab.vlab.ce.engine.services.sourcecode;

import eu.essi_lab.vlab.core.datamodel.SupportResponse;
import java.io.File;
import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class SourceCodeConventionParserTest {

	@Test
	public void test1() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidMissingFile/");

		SourceCodeConventionParser parser = new SourceCodeConventionParser();
		SupportResponse resp = new SupportResponse();
		SupportResponse r = parser.validate(new File(dirUrl.getPath()), resp);

		assertTrue(r.isCanRead());
		assertFalse(r.isConventionsOk());
		assertEquals("Can't find VLab/dockerImage.json", r.getConventionError());

	}

	@Test
	public void test2() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidMissingFile/");

		SourceCodeConventionParser parser = new SourceCodeConventionParser();
		SupportResponse resp = new SupportResponse();
		SupportResponse r = parser.validate(new File(dirUrl.getPath() + "ddddd/"), resp);

		assertFalse(r.isCanRead());
		assertFalse(r.isConventionsOk());
		assertTrue(r.getConventionError().contains("Can't read source code folder"));

	}

	@Test
	public void test3() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidMissingFileIO/");

		SourceCodeConventionParser parser = new SourceCodeConventionParser();
		SupportResponse resp = new SupportResponse();
		SupportResponse r = parser.validate(new File(dirUrl.getPath()), resp);

		assertTrue(r.isCanRead());
		assertFalse(r.isConventionsOk());
		assertEquals("Can't find VLab/iodescription.json", r.getConventionError());

	}

	@Test
	public void test4() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidMissingFileScr/");

		SourceCodeConventionParser parser = new SourceCodeConventionParser();
		SupportResponse resp = new SupportResponse();
		SupportResponse r = parser.validate(new File(dirUrl.getPath()), resp);

		assertTrue(r.isCanRead());
		assertFalse(r.isConventionsOk());
		assertEquals("Can't find VLab/script.json", r.getConventionError());

	}

	@Test
	public void test5() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModelInvalidMissingVLab/");

		SourceCodeConventionParser parser = new SourceCodeConventionParser();
		SupportResponse resp = new SupportResponse();
		SupportResponse r = parser.validate(new File(dirUrl.getPath()), resp);

		assertTrue(r.isCanRead());
		assertFalse(r.isConventionsOk());
		assertEquals("Can't find VLab", r.getConventionError());

	}

	@Test
	public void test6() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModeFilesok/");

		SourceCodeConventionParser parser = new SourceCodeConventionParser();
		SupportResponse resp = new SupportResponse();
		SupportResponse r = parser.validate(new File(dirUrl.getPath()), resp);

		assertTrue(r.isCanRead());
		assertTrue(r.isConventionsOk());
		assertNull(r.getConventionError());

	}

	@Test
	public void test7() {
		URL dirUrl = this.getClass().getClassLoader().getResource("sampleModeVLabfile/");

		SourceCodeConventionParser parser = new SourceCodeConventionParser();
		SupportResponse resp = new SupportResponse();
		SupportResponse r = parser.validate(new File(dirUrl.getPath()), resp);

		assertTrue(r.isCanRead());
		assertFalse(r.isConventionsOk());
		assertEquals("VLab is not a folder",r.getConventionError());

	}

}