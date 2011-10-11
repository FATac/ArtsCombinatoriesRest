package org.fundaciotapies.ac.model.support;

import java.util.List;

public class Template {

	private String className;
	private List<TemplateSection> sections;
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public void setSections(List<TemplateSection> sections) {
		this.sections = sections;
	}
	public List<TemplateSection> getSections() {
		return sections;
	}

}
