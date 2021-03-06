package org.fundaciotapies.ac.view.fields;

public class ObjectInput extends GenericInput {
	
	private String controlType = "objectInput";
	private String objectClass = "";

	public void setControlType(String controlType) {
		this.controlType = controlType;
	}

	public String getControlType() {
		return controlType;
	}
	
	public ObjectInput(String name, String objectClass) {
		setName(name);
		setObjectClass(objectClass);
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public String getObjectClass() {
		return objectClass;
	}


}
