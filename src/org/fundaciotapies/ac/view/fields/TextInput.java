package org.fundaciotapies.ac.view.fields;

public class TextInput extends GenericInput {
	
	private String controlType = "textInput";
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

	public TextInput(String name) {
		setName(name);
	}
}
