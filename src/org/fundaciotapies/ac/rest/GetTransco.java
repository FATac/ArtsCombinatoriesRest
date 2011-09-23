package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.rest.client.Transco;
import org.fundaciotapies.ac.rest.client.TranscoEntity;

import com.google.gson.Gson;

/**
 * DO NOT USE
 */
@Path("getTransco")
public class GetTransco {

	@GET
	@Produces("application/json")
	public String getTransco(@QueryParam("id") String id) {
		TranscoEntity ent = new Transco().getTransco(id);
		return new Gson().toJson(ent);
	}
}
