package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WMSValue {

	@JsonInclude
	private String protocol;

	@JsonInclude
	private String name;

	@JsonInclude
	private String legend;

	@JsonInclude
	private List<WMSLegend> legendList;

	@JsonInclude
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public List<WMSLegend> getLegendList() {
		return legendList;
	}

	public void setLegendList(List<WMSLegend> legendList) {
		this.legendList = legendList;
	}
}
