package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.rest.client.TranscoEntity;

import com.google.gson.Gson;

@Path("returnTransco")
public class ReturnTransco {
	
	public TranscoEntity returnTransco(@Context HttpServletRequest httpRequest, String request) {
		TranscoEntity ent = new Gson().fromJson(request, TranscoEntity.class);
		
		return ent;
	}
}