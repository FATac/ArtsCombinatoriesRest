package org.fundaciotapies.ac.model.support;

import java.io.InputStream;

public class ObjectFile {
	private InputStream inputStream;
	private String contentType;
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentType() {
		return contentType;
	}
}
