package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPOutput;
import eu.essi_lab.vlab.core.datamodel.SimpleNameValue;
import eu.essi_lab.vlab.core.datamodel.WMSValue;
import eu.essi_lab.vlab.core.engine.services.IWebStorage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class BPOutputWebStorageTest {

	@Test
	public void test() {

		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);
		BPOutputWebStorage bpOutputWebStorage = Mockito.spy(new BPOutputWebStorage());

		String bucket = "bucket";
		bpOutputWebStorage.setBucket(bucket);

		BPOutput output = Mockito.mock(BPOutput.class);

		Mockito.doReturn("id").when(output).getId();

		Mockito.doReturn("array").when(output).getOutputType();

		String runid = "runid";
		String k = bpOutputWebStorage.getKey(output, runid);

		Assert.assertEquals("runid/id/", k);

	}

	@Test
	public void test2() {

		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);
		BPOutputWebStorage bpOutputWebStorage = Mockito.spy(new BPOutputWebStorage());

		String bucket = "bucket";
		bpOutputWebStorage.setBucket(bucket);

		BPOutput output = Mockito.mock(BPOutput.class);

		Mockito.doReturn("id").when(output).getId();

		Mockito.doReturn("individual").when(output).getOutputType();

		String runid = "runid";
		String k = bpOutputWebStorage.getKey(output, runid);

		Assert.assertEquals("runid/id", k);

	}

	@Test
	public void test3() throws BPException {

		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);
		BPOutputWebStorage bpOutputWebStorage = Mockito.spy(new BPOutputWebStorage());

		String bucket = "bucket";
		bpOutputWebStorage.setBucket(bucket);

		String ak = "ak";
		bpOutputWebStorage.setAccessKey(ak);

		String sk = "sk";
		bpOutputWebStorage.setSecretKey(sk);

		String region = "us-east-1";
		bpOutputWebStorage.setRegion(region);

		String s3url = null;
		bpOutputWebStorage.setServiceUrl(s3url);

		BPOutput output = new BPOutput();

		output.setId("id");

		output.setOutputType("individual");

		output.setValueSchema("url");

		String runid = "runid";

		bpOutputWebStorage.addValue(output, runid, iWebStorage);

		Assert.assertEquals("https://s3.amazonaws.com/bucket/runid/id", output.getValue());

	}

	@Test
	public void test4() throws BPException {
		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);
		BPOutputWebStorage bpOutputWebStorage = Mockito.spy(new BPOutputWebStorage());

		String bucket = "bucket";
		bpOutputWebStorage.setBucket(bucket);

		String ak = "ak";
		bpOutputWebStorage.setAccessKey(ak);

		String sk = "sk";
		bpOutputWebStorage.setSecretKey(sk);

		String region = "us-east-1";
		bpOutputWebStorage.setRegion(region);

		String s3url = "http://52.20.116.19:32042";
		bpOutputWebStorage.setServiceUrl(s3url);

		BPOutput output = new BPOutput();

		output.setId("id");

		output.setOutputType("individual");

		output.setValueSchema("url");

		String runid = "runid";

		bpOutputWebStorage.addValue(output, runid, iWebStorage);

		Assert.assertEquals("http://52.20.116.19:32042/bucket/runid/id", output.getValue());
		Assert.assertEquals("value", output.getValueType());
	}

	@Test
	public void test5() throws BPException {
		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);
		BPOutputWebStorage bpOutputWebStorage = Mockito.spy(new BPOutputWebStorage());

		String bucket = "bucket";
		bpOutputWebStorage.setBucket(bucket);

		String ak = "ak";
		bpOutputWebStorage.setAccessKey(ak);

		String sk = "sk";
		bpOutputWebStorage.setSecretKey(sk);

		String region = "us-east-1";
		bpOutputWebStorage.setRegion(region);

		String s3url = null;
		bpOutputWebStorage.setServiceUrl(s3url);

		BPOutput output = new BPOutput();

		output.setId("id");

		output.setOutputType("individual");

		output.setValueSchema("wms");

		String runid = "runid";

		InputStream wmsStream = this.getClass().getClassLoader().getResourceAsStream("wms.json");

		Mockito.doReturn(wmsStream).when(iWebStorage).read(Mockito.any());

		bpOutputWebStorage.addValue(output, runid, iWebStorage);

		Assert.assertTrue(output.getValue() instanceof WMSValue);

		WMSValue wmsValue = (WMSValue) output.getValue();

		Assert.assertEquals("value", output.getValueType());
		Assert.assertEquals(3, wmsValue.getLegendList().size());
		Assert.assertEquals("https://example.wms.com/alpha/wms?", wmsValue.getUrl());

		Assert.assertEquals("https://example.com/legend.png", wmsValue.getLegend());

		Assert.assertEquals("51cbc67c-2e33-4919-923d-9857853982af", wmsValue.getName());

		Assert.assertEquals("urn:ogc:serviceType:WebMapService:1.1.1:HTTP", wmsValue.getProtocol());

	}

	@Test
	public void test6() throws BPException {
		IWebStorage iWebStorage = Mockito.mock(IWebStorage.class);
		BPOutputWebStorage bpOutputWebStorage = Mockito.spy(new BPOutputWebStorage());

		String bucket = "bucket";
		bpOutputWebStorage.setBucket(bucket);

		String ak = "ak";
		bpOutputWebStorage.setAccessKey(ak);

		String sk = "sk";
		bpOutputWebStorage.setSecretKey(sk);

		String region = "us-east-1";
		bpOutputWebStorage.setRegion(region);

		String s3url = null;
		bpOutputWebStorage.setServiceUrl(s3url);

		BPOutput output = new BPOutput();

		output.setId("id");

		output.setOutputType("array");

		String runid = "runid";

		List<String> contents = new ArrayList<>();

		String c1 = "runid/id/c/file";
		String c2 = "runid/id/c1/file2";

		contents.add(c1);
		contents.add(c2);

		Mockito.doReturn(contents).when(iWebStorage).listSubOjects(Mockito.any());

		bpOutputWebStorage.addValue(output, runid, iWebStorage);

		Assert.assertEquals(2, output.getValueArray().size());

		SimpleNameValue o1 = (SimpleNameValue) output.getValueArray().get(0);

		Assert.assertEquals("file", o1.getName());

		Assert.assertEquals("https://s3.amazonaws.com/bucket/runid/id/c/file", o1.getUrl());

		SimpleNameValue o2 = (SimpleNameValue) output.getValueArray().get(1);
		Assert.assertEquals("file2", o2.getName());
		Assert.assertEquals("https://s3.amazonaws.com/bucket/runid/id/c1/file2", o2.getUrl());

	}

}