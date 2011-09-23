package org.fundaciotapies.ac.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;

import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.rest.serializer.GenericInputSerializer;
import org.fundaciotapies.ac.view.UploadDataFormFactory;
import org.fundaciotapies.ac.view.fields.CheckInput;
import org.fundaciotapies.ac.view.fields.DateInput;
import org.fundaciotapies.ac.view.fields.FileInput;
import org.fundaciotapies.ac.view.fields.GenericInput;
import org.fundaciotapies.ac.view.fields.NumericInput;
import org.fundaciotapies.ac.view.fields.ObjectInput;
import org.fundaciotapies.ac.view.fields.TextInput;
import org.fundaciotapies.ac.view.fields.TimeInput;

import com.google.gson.GsonBuilder;

/**
 * Call: http://{host:port}/classes/{class}/form
 * <br>
 * Get all fields and its types of given class <br>
 * Params class: Class name
 */
@Path("/classes/{class}/form")
public class GetInsertObjectForm {

	@GET
	@Produces("application/json")
	public String getInsertObjectForm(@PathParam("class") String className) {
		List<String> fieldList = new Request().listClassProperties(className);
		List<GenericInput> inputList = new ArrayList<GenericInput>();
		
		// TODO: Base class/es that should be considered as Media class/es should be taken from properties file  
		boolean isMediaObject = new Request().listSubclasses("Media", false).contains(className);
		
		for (String f: fieldList) {
			String[] s = f.split(" ");
			
			String prop = s[0];
			String range = s[1];
			String dType = s[2].charAt(0)+"";
			
			if (range==null || dType==null) {
				inputList.add(new TextInput(prop));				
			} else if ("O".equals(dType)){
				inputList.add(new ObjectInput(prop, range));
			} else if ("D".equals(dType)) {
				range = range.substring(0, range.length()-1);
				if ("boolean".equals(range)) {
					inputList.add(new CheckInput(prop));
				} else if ("float".equals(range) || "Integer".equals(range) || "nonNegativeInteger".equals(range)) {
					inputList.add(new NumericInput(prop));
				} else if ("date".equals(range) || "dateTime".equals(range)) {
					inputList.add(new DateInput(prop));
				} else if ("time".equals(range)) {
					inputList.add(new TimeInput(prop));
				} else {
					inputList.add(new TextInput(prop));
				}
			}
		}
		
		if (isMediaObject)
			inputList.add(new FileInput("filePath"));
		
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(GenericInput.class, new GenericInputSerializer());
		
		return gson.create().toJson(UploadDataFormFactory.insertObjectForm(className, inputList));
	}
}