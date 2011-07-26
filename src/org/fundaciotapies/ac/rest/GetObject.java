package org.fundaciotapies.ac.rest;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("/objects/{class}/{id}")
public class GetObject {
	
	@GET
	@Produces("application/json")
	public String getObject(@PathParam("class") String c, @PathParam("id") String id, @QueryParam("u") String uid) {
		Map<String, String> result = new Request().getObject(c+"/"+id, uid);
		if (result==null) return new Gson().toJson("Error: Object could not be found");
		return new Gson().toJson(result);
	}

}
