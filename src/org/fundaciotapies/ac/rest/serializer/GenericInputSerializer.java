package org.fundaciotapies.ac.rest.serializer;

import java.lang.reflect.Type;

import org.fundaciotapies.ac.view.fields.CheckInput;
import org.fundaciotapies.ac.view.fields.DateInput;
import org.fundaciotapies.ac.view.fields.FileInput;
import org.fundaciotapies.ac.view.fields.GenericInput;
import org.fundaciotapies.ac.view.fields.NumericInput;
import org.fundaciotapies.ac.view.fields.ObjectInput;
import org.fundaciotapies.ac.view.fields.TextInput;
import org.fundaciotapies.ac.view.fields.TimeInput;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GenericInputSerializer implements JsonSerializer<GenericInput> {

	@Override
	public JsonElement serialize(GenericInput arg0, Type arg1,
			JsonSerializationContext arg2) {
	
		if (arg0 instanceof CheckInput) {
			return new Gson().toJsonTree(arg0, CheckInput.class);
		} else if (arg0 instanceof DateInput) {
			return new Gson().toJsonTree(arg0, DateInput.class);
		} else if (arg0 instanceof FileInput) {
			return new Gson().toJsonTree(arg0, FileInput.class);
		} else if (arg0 instanceof NumericInput) {
			return new Gson().toJsonTree(arg0, NumericInput.class);
		} else if (arg0 instanceof ObjectInput) {
			return new Gson().toJsonTree(arg0, ObjectInput.class);
		} else if (arg0 instanceof TextInput) {
			return new Gson().toJsonTree(arg0, TextInput.class);
		} else if (arg0 instanceof TimeInput) {
			return new Gson().toJsonTree(arg0, TimeInput.class);
		} else return null;
	}

}
