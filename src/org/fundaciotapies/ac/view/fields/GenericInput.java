package org.fundaciotapies.ac.view.fields;

public abstract class GenericInput {
	
	private Boolean multiValue = null;
	private String name = null;

	public void setMultiValue(Boolean multiValue) {
		this.multiValue = multiValue;
	}

	public Boolean getMultiValue() {
		return multiValue;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
