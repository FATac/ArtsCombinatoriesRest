package org.fundaciotapies.ac.view.fields;

public class FileInput extends GenericInput {
	
	private String controlType = "fileInput";
	private String name;
	private String filePath;
	
	public FileInput(String name) {
		this.name = name;
	}
	
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
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFilePath() {
		return filePath;
	}
}
