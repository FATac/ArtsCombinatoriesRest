package org.fundaciotapies.ac.view;

import java.util.List;

import org.fundaciotapies.ac.view.fields.GenericInput;
import org.fundaciotapies.ac.view.forms.InsertObjectForm;

public class UploadDataFormFactory {
	
	public static InsertObjectForm insertObjectForm(String className, List<GenericInput> inputList) {
		InsertObjectForm form = new InsertObjectForm();
		form.setClassName(className);
		form.setInputList(inputList);
		return form;
	}
}
