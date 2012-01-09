package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("/legal/list")
public class LegalList {
	
	@GET
	@Produces("application/json")
	public String legalList(@QueryParam("color") String color) {
		return new Gson().toJson(new Request().listObjectLegalColor(color));
	}
}
