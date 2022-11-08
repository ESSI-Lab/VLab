package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPUser;
import java.util.Arrays;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mattia Santoro
 */
public class ESQueryBuilderTest {

	@Test
	public void test1() {
		JSONObject json = new ESQueryBuilder().//
				setTimeField("esCreationTime").//
				build();

		Assert.assertEquals("{\"size\":5,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},"
				+ "\"must\":{\"query_string\":{\"query\":\"\"}}}},\"from\":0,"
				+ "\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}", json.toString());
	}

	@Test
	public void test1_1() {
		JSONObject json = new ESQueryBuilder().//
				build();

		Assert.assertEquals(
				"{\"size\":5,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},\"must\":{\"query_string\":{\"query\":\"\"}}}},\"from\":0}",
				json.toString());
	}

	@Test
	public void test2() {
		JSONObject json = new ESQueryBuilder().//
				setTimeField("esCreationTime").//
				setStart(3).//
				build();

		Assert.assertEquals("{\"size\":5,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},"
				+ "\"must\":{\"query_string\":{\"query\":\"\"}}}},\"from\":3,"
				+ "\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}", json.toString());
	}

	@Test
	public void test3() {
		JSONObject json = new ESQueryBuilder().//
				setCount(3).//
				setTimeField("esCreationTime").//
				build();

		Assert.assertEquals("{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},"
				+ "\"must\":{\"query_string\":{\"query\":\"\"}}}},\"from\":0,"
				+ "\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}", json.toString());
	}

	@Test
	public void test4() {
		JSONObject json = new ESQueryBuilder().//
				setTimeField("esCreationTime").//
				setCount(3).//
				setStart(1).//
				build();

		Assert.assertEquals("{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},"
				+ "\"must\":{\"query_string\":{\"query\":\"\"}}}},\"from\":1,"
				+ "\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}", json.toString());

		System.out.println(json.toString());
	}

	@Test
	public void test5() {
		String umail = "mail@mail.com";
		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setCount(3).//
				setStart(1).//
				setUser(user).//
				build();

		Assert.assertEquals("{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},"
				+ "\"must\":{\"query_string\":{\"query\":\"(owner.keyword:mail@mail.com) OR (sharedWith.keyword:mail@mail.com) OR (publicRun:true)\"}}}},"
				+ "\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}", json.toString());
	}

	@Test
	public void test6() {
		String umail = "mail@mail.com";
		String wfid = "http://example.com/uuid-198894949";

		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setCount(3).//
				setStart(1).//
				setUser(user).//
				addMustConstraint("workflowid.keyword", wfid).//
				build();

		Assert.assertEquals("{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},"
				+ "\"must\":{\"query_string\":{\"query\":\"((owner.keyword:mail@mail.com) OR (sharedWith.keyword:mail@mail.com) OR "
				+ "(publicRun:true)) AND (workflowid.keyword:\\\"http://example.com/uuid-198894949\\\")\"}}}},"
				+ "\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}", json.toString());
	}

	@Test
	public void test7() {
		String umail = "mail@mail.com";
		String wfid = "http://example.com/uuid-198894949";

		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setCount(3).//
				setStart(1).//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				addMustConstraint("workflowid.keyword", wfid).//
				build();

		Assert.assertEquals("{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"match_all\":{}},"
				+ "\"must\":{\"query_string\":{\"query\":\"(workflowid.keyword:\\\"http://example.com/uuid-198894949\\\")\"}}}},"
				+ "\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}", json.toString());
	}

	@Test
	public void test8() {
		String umail = "mail@mail.com";
		String wfid = "http://example.com/uuid-198894949";

		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setCount(3).//
				setStart(1).//
				setUser(user).//
				addMustConstraint("workflowid.keyword", wfid).//
				setQueryText("searchtext").//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setExactTextSearchFields(Arrays.asList("name", "inputs.name", "inputs.description", "inputs.value", "inputs.valueArray")).//
				build();

		Assert.assertEquals(
				"{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"match\":{\"name\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.name\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.description\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.value\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.valueArray\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}}]}},\"must\":{\"query_string\":{\"query\":\"((owner.keyword:mail@mail.com) OR (sharedWith.keyword:mail@mail.com) OR (publicRun:true)) AND (workflowid.keyword:\\\"http://example.com/uuid-198894949\\\")\"}}}},\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}",
				json.toString());
	}

	@Test
	public void test9() {
		String umail = "mail@mail.com";
		String wfid = "http://example.com/uuid-198894949";

		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setCount(3).//
				setStart(1).//
				setUser(user).//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setQueryText("searchtext").//
				setExactTextSearchFields(Arrays.asList("name", "inputs.name", "inputs.description", "inputs.value", "inputs.valueArray")).//
				build();

		Assert.assertEquals(
				"{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"match\":{\"name\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.name\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.description\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.value\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.valueArray\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}}]}},\"must\":{\"query_string\":{\"query\":\"(owner.keyword:mail@mail.com) OR (sharedWith.keyword:mail@mail.com) OR (publicRun:true)\"}}}},\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}",
				json.toString());
	}

	@Test
	public void test10() {
		String umail = "mail@mail.com";
		String wfid = "http://example.com/uuid-198894949";

		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setCount(3).//
				setStart(1).//
				setUser(user).//
				addMustConstraint("workflowid.keyword", wfid).//
				setQueryText("searchtext").//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setWildCardTextSearchFields(Arrays.asList("name", "inputs.name", "inputs.description")).//
				build();

		Assert.assertEquals(
				"{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(name:*searchtext*)\"}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(inputs.name:*searchtext*)\"}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(inputs.description:*searchtext*)\"}}]}}]}},\"must\":{\"query_string\":{\"query\":\"((owner.keyword:mail@mail.com) OR (sharedWith.keyword:mail@mail.com) OR (publicRun:true)) AND (workflowid.keyword:\\\"http://example.com/uuid-198894949\\\")\"}}}},\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}",
				json.toString());
	}

	@Test
	public void test11() {
		String umail = "mail@mail.com";
		String wfid = "http://example.com/uuid-198894949";

		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setCount(3).//
				setStart(1).//
				setUser(user).//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setQueryText("searchtext").//
				setWildCardTextSearchFields(Arrays.asList("name", "inputs.name", "inputs.description")).//
				setExactTextSearchFields(Arrays.asList("inputs.value", "inputs.valueArray")).//
				build();

		Assert.assertEquals(
				"{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"match\":{\"inputs.value\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.valueArray\":{\"query\":\"searchtext\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(name:*searchtext*)\"}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(inputs.name:*searchtext*)\"}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(inputs.description:*searchtext*)\"}}]}}]}},\"must\":{\"query_string\":{\"query\":\"(owner.keyword:mail@mail.com) OR (sharedWith.keyword:mail@mail.com) OR (publicRun:true)\"}}}},\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}",
				json.toString());
	}

	@Test
	public void test12() {
		String umail = "mail@mail.com";
		String wfid = "http://example.com/uuid-198894949";

		BPUser user = Mockito.mock(BPUser.class);
		Mockito.doReturn(umail).when(user).getEmail();

		JSONObject json = new ESQueryBuilder().//
				setCount(3).//
				setStart(1).//
				setUser(user).//
				setTimeField("esCreationTime").//
				setOwnerFiled("owner").//
				setSharedWithFiled("sharedWith").//
				setPublicFiled("publicRun").//
				setQueryText(" https://s3.amazonaws.com/vlabdocumentationfiles/s2.zip").//
				setWildCardTextSearchFields(Arrays.asList("name", "inputs.name", "inputs.description")).//
				setExactTextSearchFields(Arrays.asList("inputs.value", "inputs.valueArray")).//
				build();

		Assert.assertEquals(
				"{\"size\":3,\"query\":{\"bool\":{\"filter\":{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"match\":{\"inputs.value\":{\"query\":\" https://s3.amazonaws.com/vlabdocumentationfiles/s2.zip\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"inputs.valueArray\":{\"query\":\" https://s3.amazonaws.com/vlabdocumentationfiles/s2.zip\",\"operator\":\"and\"}}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(name:* https\\\\\\\\\\\\:\\\\/\\\\/s3.amazonaws.com\\\\/vlabdocumentationfiles\\\\/s2.zip*)\"}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(inputs.name:* https\\\\\\\\\\\\:\\\\/\\\\/s3.amazonaws.com\\\\/vlabdocumentationfiles\\\\/s2.zip*)\"}}]}},{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"(inputs.description:* https\\\\\\\\\\\\:\\\\/\\\\/s3.amazonaws.com\\\\/vlabdocumentationfiles\\\\/s2.zip*)\"}}]}}]}},\"must\":{\"query_string\":{\"query\":\"(owner.keyword:mail@mail.com) OR (sharedWith.keyword:mail@mail.com) OR (publicRun:true)\"}}}},\"from\":1,\"sort\":[{\"esCreationTime\":{\"unmapped_type\":\"boolean\",\"order\":\"desc\"}}]}",
				json.toString());
	}

}