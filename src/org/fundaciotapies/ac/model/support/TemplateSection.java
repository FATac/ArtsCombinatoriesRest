package org.fundaciotapies.ac.model.support;

import java.util.List;

public class TemplateSection {

	private String name;
	private List<DataMapping> data;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setData(List<DataMapping> data) {
		this.data = data;
	}
	public List<DataMapping> getData() {
		return data;
	}
}
