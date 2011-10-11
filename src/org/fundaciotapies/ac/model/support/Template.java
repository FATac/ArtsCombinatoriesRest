package org.fundaciotapies.ac.model.support;

import java.util.List;

public class Template {

	private String className;
	private List<DataMapping> header;
	private List<DataMapping> body;
	private String isMedia = null;
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public List<DataMapping> getHeader() {
		return header;
	}
	public void setHeader(List<DataMapping> header) {
		this.header = header;
	}
	public List<DataMapping> getBody() {
		return body;
	}
	public void setBody(List<DataMapping> body) {
		this.body = body;
	}
	public String getIsMedia() {
		return isMedia;
	}
	public void setIsMedia(String isMedia) {
		this.isMedia = isMedia;
	}

}
