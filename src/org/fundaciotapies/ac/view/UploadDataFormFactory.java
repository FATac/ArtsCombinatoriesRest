package org.fundaciotapies.ac.view;

import java.util.List;

import org.fundaciotapies.ac.view.fields.GenericInput;
import org.fundaciotapies.ac.view.fields.Select;
import org.fundaciotapies.ac.view.forms.InsertObjectForm;
import org.fundaciotapies.ac.view.forms.PickClassForm;

public class UploadDataFormFactory {
	
	public static PickClassForm pickClassForm(List<String> classesList) {
		PickClassForm form = new PickClassForm();
		form.setClassesList(new Select("classesList", classesList, null));
		return form;
	}
	
	public static InsertObjectForm insertObjectForm(String className, List<GenericInput> inputList) {
		InsertObjectForm form = new InsertObjectForm();
		form.setClassName(className);
		form.setInputList(inputList);
		return form;
	}
}
