package org.fundaciotapies.ac.view.fields;

public class DateInput extends GenericInput {
	
	private String controlType = "dateInput";
	private String defaultValue;
	
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public String getControlType() {
		return controlType;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public DateInput(String name) {
		setName(name);
	}
}
