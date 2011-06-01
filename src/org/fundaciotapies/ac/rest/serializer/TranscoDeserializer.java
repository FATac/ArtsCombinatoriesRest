package org.fundaciotapies.ac.rest.serializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import org.fundaciotapies.ac.rest.client.Profile;
import org.fundaciotapies.ac.rest.client.TranscoEntity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class TranscoDeserializer implements JsonDeserializer<TranscoEntity> {

	@Override
	public TranscoEntity deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		TranscoEntity ent = new TranscoEntity();
		
		if (arg0.isJsonObject()) {
			JsonObject jsob = arg0.getAsJsonObject();
			
			if (jsob.get("id")!=null) ent.setId(jsob.get("id").getAsString());
			if (jsob.get("priority")!=null) ent.setPriority(jsob.get("priority").getAsString());
			if (jsob.get("src_path")!=null) ent.setSrc_path(jsob.get("src_path").getAsString());
			if (jsob.get("profiles")!=null) {
				JsonElement elm = jsob.get("profiles");
				ent.setProfiles(new ArrayList<Profile>());
				if (elm.isJsonArray()) {
					JsonArray jarr = elm.getAsJsonArray();
					for (Iterator<JsonElement> it = jarr.iterator();it.hasNext();) {
						JsonElement ielm = it.next();
						Profile prof = new Gson().fromJson(ielm, Profile.class);
						ent.getProfiles().add(prof);
					}
				} else {
					Profile prof = new Gson().fromJson(elm, Profile.class);
					ent.getProfiles().add(prof);
				}
			}
		}
		
		return ent;
	}
}
