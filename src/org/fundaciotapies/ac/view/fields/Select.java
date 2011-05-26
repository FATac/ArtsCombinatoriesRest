package org.fundaciotapies.ac.view.fields;

import java.util.List;

public class Select extends GenericInput {
	
	private String controlType = "select";
	private String name;
	private List<String> values;
	private String defaultValue;

	public void setValues(List<String> values) {
		this.values = values;
	}
	public List<String> getValues() {
		return values;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public Select(String name, List<String> values, String defaultValue) {
		setName(name);
		setValues(values);
		setDefaultValue(defaultValue);
	}
	
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public String getControlType() {
		return controlType;
	}

	
}
