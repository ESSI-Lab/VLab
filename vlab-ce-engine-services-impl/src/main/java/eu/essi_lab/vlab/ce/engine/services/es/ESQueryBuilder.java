package eu.essi_lab.vlab.ce.engine.services.es;

import eu.essi_lab.vlab.core.datamodel.BPUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class ESQueryBuilder {

	private Integer start = 0;
	private Integer count = 5;
	private BPUser user = new BPUser();
	private String text;
	private static final String FROM_KEY = "from";
	private static final String SIZE_KEY = "size";
	private static final String BOOL_KEY = "bool";
	private static final String FILTER_KEY = "filter";
	private static final String MUST_KEY = "must";
	private static final String SHOULD_KEY = "should";
	private static final String QUERY_KEY = "query";
	private static final String SORT_KEY = "sort";
	private static final String MATCH_ALL_KEY = "match_all";
	private Map<String, String> map = new HashMap<>();

	private String ownerFiled;
	private String sharedWithFiled;
	private String publicFiled;
	private String timeField;
	private static final String OR_OPERATOR = "OR";
	private static final String AND_OPERATOR = "AND";
	private List<String> exactTextSearchFields = new ArrayList<>();
	private List<String> wildCardTextSearchFields = new ArrayList<>();
	private List<String> esReservedChars = Arrays.asList("+", "-", "=", "&&", "||", ">", "<", "!", "(", ")", "{", "}", "[", "]", "^", "~",
			"*", "?", ":", "\\", "/");

	public ESQueryBuilder setStart(Integer start) {
		this.start = start;

		return this;
	}

	public ESQueryBuilder setCount(Integer count) {
		this.count = count;

		return this;
	}

	public ESQueryBuilder setUser(BPUser user) {
		this.user = user;

		return this;
	}

	public ESQueryBuilder setQueryText(String q) {
		this.text = q;

		return this;
	}

	public JSONObject build() {

		JSONObject json = new JSONObject();

		addPage(json, start, count);

		addQuery(json, user, text);

		return json;
	}

	private void addPage(JSONObject json, int start, int count) {

		json.put(FROM_KEY, start);
		json.put(SIZE_KEY, count);

	}

	public ESQueryBuilder addMustConstraint(String field, String value) {

		if (null != value && !"".equalsIgnoreCase(value))
			map.put(field, value);

		return this;
	}

	protected void addQuery(JSONObject json, BPUser user, String text) {

		JSONObject query = new JSONObject();

		JSONObject bool = new JSONObject();

		JSONObject mustConstraints = mustConstraints(user, map);

		bool.put(MUST_KEY, mustConstraints);

		JSONArray array = new JSONArray();

		if (text != null && !"".equalsIgnoreCase(text)) {

			JSONArray ar = createShouldArray(exactTextSearchFields, text, Boolean.TRUE);

			for (int i = 0; i < ar.length(); i++) {
				array.put(ar.getJSONObject(i));
			}

			ar = createShouldArray(wildCardTextSearchFields, text, Boolean.FALSE);

			for (int i = 0; i < ar.length(); i++) {
				array.put(ar.getJSONObject(i));
			}
		}

		JSONObject filter = new JSONObject();

		if (array.length() > 0) {

			JSONObject should = new JSONObject();

			should.put(SHOULD_KEY, array);

			filter.put(BOOL_KEY, should);

		} else {
			filter.put(MATCH_ALL_KEY, new JSONObject());
		}

		bool.put(FILTER_KEY, filter);

		query.put(BOOL_KEY, bool);

		json.put(QUERY_KEY, query);

		if (notNullOrEmpty(timeField)) {

			JSONArray sort = new JSONArray();
			sort.put(new JSONObject("{\"" + timeField + "\":{\"order\":\"desc\",\"unmapped_type\":\"boolean\"}}"));

			json.put(SORT_KEY, sort);
		}
	}

	private JSONArray createShouldArray(List<String> fields, String text, Boolean exact) {

		JSONArray array = new JSONArray();

		fields.forEach(field -> array.put(createBoolMust(field, text, exact)));

		return array;

	}

	private JSONObject createBoolMust(String field, String text, Boolean exact) {

		JSONObject jsonObject = new JSONObject();

		JSONObject must = new JSONObject();

		JSONArray array = new JSONArray();

		array.put(createSingleFieldMatchClause(field, text, exact));

		must.put(MUST_KEY, array);

		jsonObject.put(BOOL_KEY, must);

		return jsonObject;

	}

	private JSONObject createSingleFieldMatchClause(String field, String text, Boolean exact) {

		if (Boolean.TRUE.equals(exact))
			return new JSONObject(""//
					+ "{" //
					+ " \"match\": {"//
					+ "     \"" + field + "\": {"//
					+ "         \"query\": \"" + text + "\","//
					+ "         \"operator\": \"and\""//
					+ "     }"//
					+ " }"//
					+ "}");

		for (String c : esReservedChars)
			text = text.replace(c, "\\\\" + c);

		return new JSONObject(""//
				+ "{" //
				+ " \"query_string\": {"//
				+ "    \"query\": \"(" + field + ":*" + text + "*)\""//
				+ " }"//
				+ "}");

	}

	private JSONObject mustConstraints(BPUser user, Map<String, String> map) {

		String email = user.getEmail();

		List<String> otherContraints = new ArrayList<>();

		map.entrySet().forEach(entry -> {

			StringBuilder builder = new StringBuilder();

			builder.append(entry.getKey()).append(":\\\"").append(entry.getValue()).//
					append("\\\"");

			otherContraints.add(builder.toString());

		});

		StringBuilder emailBuilder = new StringBuilder();

		if (Boolean.TRUE.equals(requiresEmail(email))) {

			if (ownerFiled != null)
				doAppend(emailBuilder, new StringBuilder().append("(").append(ownerFiled).append(".keyword:").append(email).append(")")
						.toString(), OR_OPERATOR);

			if (sharedWithFiled != null)
				doAppend(emailBuilder, new StringBuilder().append("(").append(sharedWithFiled).append(".keyword:").append(email).append(")")
						.toString(), OR_OPERATOR);

			if (publicFiled != null)
				doAppend(emailBuilder, new StringBuilder().append("(").append(publicFiled).append(":true").append(")").toString(),
						OR_OPERATOR);

		}
		StringBuilder qBuilder = new StringBuilder();

		qBuilder.append("{\"query_string\": ").//
				append("{\"query\": \"");
		StringBuilder builder = new StringBuilder();
		if (otherContraints.isEmpty()) {

			System.out.println("1");

			//only email
			builder.append(emailBuilder.toString());
		} else if (Boolean.FALSE.equals(requiresEmail(email))) {
			//only constr
			System.out.println("2");

			for (String c : otherContraints) {

				doAppend(builder, new StringBuilder().append("(").append(c).append(")").toString(), AND_OPERATOR);

			}

		} else {
			//both
			System.out.println("3");

			doAppend(builder, new StringBuilder().append("(").append(emailBuilder.toString()).append(")").toString(), AND_OPERATOR);

			for (String c : otherContraints) {
				doAppend(builder, new StringBuilder().append("(").append(c).append(")").toString(), AND_OPERATOR);

			}

		}

		qBuilder.append(builder.toString());
		qBuilder.append("\"}}");

		return new JSONObject(qBuilder.toString());
	}

	private void doAppend(StringBuilder builder, String toappend, String operator) {
		if (builder.length() > 0)
			builder.append(" ").append(operator).append(" ").append(toappend);
		else
			builder.append(toappend);

	}

	private boolean notNullOrEmpty(String v) {
		return null != v && !"".equalsIgnoreCase(v);
	}

	private Boolean requiresEmail(String email) {
		return notNullOrEmpty(email) && (notNullOrEmpty(ownerFiled) || notNullOrEmpty(sharedWithFiled) || notNullOrEmpty(publicFiled));
	}

	public ESQueryBuilder setOwnerFiled(String ownerFiled) {
		this.ownerFiled = ownerFiled;
		return this;
	}

	public ESQueryBuilder setSharedWithFiled(String sharedWithFiled) {
		this.sharedWithFiled = sharedWithFiled;
		return this;
	}

	public ESQueryBuilder setPublicFiled(String publicFiled) {
		this.publicFiled = publicFiled;
		return this;
	}

	public ESQueryBuilder setTimeField(String timeField) {
		this.timeField = timeField;

		return this;
	}

	public List<String> getExactTextSearchFields() {
		return exactTextSearchFields;
	}

	public ESQueryBuilder setExactTextSearchFields(List<String> exactTextSearchFields) {
		this.exactTextSearchFields = exactTextSearchFields;

		return this;
	}

	public List<String> getWildCardTextSearchFields() {
		return wildCardTextSearchFields;
	}

	public ESQueryBuilder setWildCardTextSearchFields(List<String> wildCardTextSearchFields) {
		this.wildCardTextSearchFields = wildCardTextSearchFields;

		return this;
	}
}
