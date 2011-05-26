package org.fundaciotapies.ac.view.forms;

import java.util.List;

import org.fundaciotapies.ac.view.fields.GenericInput;

public class InsertObjectForm {

	private String className;
	private List<GenericInput> inputList;

	public void setInputList(List<GenericInput> inputList) {
		this.inputList = inputList;
	}

	public List<GenericInput> getInputList() {
		return inputList;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
}
