package org.fundaciotapies.ac.logic.legal.support;

public class LegalBlockData {
	private String name;
	private String type;
	private String[] values;
	private String defaultValue;
	private String result = null;
	private Boolean autodata = null;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public void setValues(String[] values) {
		this.values = values;
	}
	public String[] getValues() {
		return values;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getResult() {
		return result;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setAutodata(Boolean autodata) {
		this.autodata = autodata;
	}
	public Boolean getAutodata() {
		return autodata;
	}
	
}
