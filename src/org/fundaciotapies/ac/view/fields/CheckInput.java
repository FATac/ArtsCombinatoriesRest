package org.fundaciotapies.ac.view.fields;

public class CheckInput extends GenericInput {
	private String controlType = "checkInput";
	private String name;
	private Boolean defaultValue;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setDefaultValue(Boolean defaultValue) {
		this.defaultValue = defaultValue;
	}
	public Boolean getDefaultValue() {
		return defaultValue;
	}
	public CheckInput(String name) {
		setName(name);
	}
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public String getControlType() {
		return controlType;
	}
}
