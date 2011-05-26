package org.fundaciotapies.ac.rest;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("getObject")
public class GetObject {
	
	@GET
	@Produces("application/json")
	public String getObject(@QueryParam("id") String id) {
		Map<String, String> result = new Request().getObject(id);
		if (result.isEmpty()) new Gson().toJson("Error: Object does not exists");
		return new Gson().toJson(result);
	}

}
