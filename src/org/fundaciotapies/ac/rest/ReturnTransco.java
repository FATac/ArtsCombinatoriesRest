package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.logic.VideoManager;
import org.fundaciotapies.ac.rest.client.Profile;
import org.fundaciotapies.ac.rest.client.TranscoEntity;
import org.fundaciotapies.ac.rest.serializer.TranscoDeserializer;

import com.google.gson.GsonBuilder;

/**
 * DO NOT USE
 */
@Path("returnTransco")
public class ReturnTransco {
	
	public void returnTransco(@Context HttpServletRequest httpRequest, String request) {
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(TranscoEntity.class, new TranscoDeserializer());
	    TranscoEntity ent = gson.create().fromJson(request, TranscoEntity.class);
	    for (Profile p : ent.getProfiles()) {
	    	new VideoManager().putVideo(p.getDst_path());
	    }
	}
}