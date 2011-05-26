package org.fundaciotapies.ac.view.fields;

public class NumericInput extends GenericInput {
	private String controlType = "numericInput";
	private String name;
	private String defaultValue;
	
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public String getControlType() {
		return controlType;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public NumericInput(String name) {
		setName(name);
	}
}
