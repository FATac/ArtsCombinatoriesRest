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
import org.fundaciotapies.ac.view.fields.TextAreaInput;
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
public class GetClassForm {

	@GET
	@Produces("application/json")
	public String getInsertObjectForm(@PathParam("class") String className) {
		List<String[]> fieldList = new Request().listClassProperties(className);
		List<GenericInput> inputList = new ArrayList<GenericInput>();
		
		for (String[] f: fieldList) {
			String prop = f[0];
			String range = f[1];
			String dType = f[2];
			
			GenericInput input = null;
			
			if (range==null || dType==null) {
				input = new TextInput(prop);				
			} else if (dType.contains("ObjectProperty")) {
				input = new ObjectInput(prop, range);
			} else if (dType.contains("DatatypeProperty")) {
				range = range.substring(0, range.length());
				if (range.contains("boolean")) {
					input = new CheckInput(prop);
				} else if (range.contains("float") || range.contains("Integer") || range.contains("nonNegativeInteger")) {
					input = new NumericInput(prop);
				} else if (range.contains("date") || range.contains("dateTime")) {
					input = new DateInput(prop);
				} else if (range.contains("time")) {
					input = new TimeInput(prop);
				} else if (range.contains("string")) {
					input = new TextAreaInput(prop); 
				} else if (range.contains("anyURI")) {
					input = new FileInput(prop);
				} else {
					input = new TextInput(prop);
				}
			}
			
			if (input != null) {
				if (!dType.contains("FunctionalProperty")) input.setMultiValue(true); else input.setMultiValue(false);
				inputList.add(input);
			}
		}
		
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(GenericInput.class, new GenericInputSerializer());
		
		return gson.create().toJson(UploadDataFormFactory.insertObjectForm(className, inputList));
	}
}