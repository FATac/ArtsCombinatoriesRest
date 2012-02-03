package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.CustomMap;

import com.google.gson.Gson;

@Path("/resource/{id}")
public class GetObject {

	@GET
	@Produces("application/json")
	public String getObject(@PathParam("id") String id, @QueryParam("u") String uid) {
		CustomMap result = new Request().getObject(id, uid);
		if (result==null) return new Gson().toJson("Error: Object could not be found");
		return new Gson().toJson(result);
	}

}
