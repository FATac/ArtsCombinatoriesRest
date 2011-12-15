package org.fundaciotapies.ac.model.support;

import java.util.List;

public class Mapping {
	
	private String xmlHeader;
	private String xmlFooter;
	private String xmlPrefix;

	private List<DataMapping> data = null;

	public void setData(List<DataMapping> data) {
		this.data = data;
	}

	public List<DataMapping> getData() {
		return data;
	}

	public void setXmlHeader(String xmlHeader) {
		this.xmlHeader = xmlHeader;
	}

	public String getXmlHeader() {
		return xmlHeader;
	}

	public void setXmlFooter(String xmlFooter) {
		this.xmlFooter = xmlFooter;
	}

	public String getXmlFooter() {
		return xmlFooter;
	}

	public void setXmlPrefix(String xmlPrefix) {
		this.xmlPrefix = xmlPrefix;
	}

	public String getXmlPrefix() {
		return xmlPrefix;
	}
}
