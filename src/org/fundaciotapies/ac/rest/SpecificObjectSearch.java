package org.fundaciotapies.ac.rest;

import java.net.URLDecoder;
import java.util.List;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("/specific")
public class SpecificObjectSearch {

	@GET
	@Produces("application/json")
	public String specificObjectSearch(@QueryParam("f") String field,  @QueryParam("v") String value, @QueryParam("c") String className) {
		List<String> result = new Request().specificObjectSearch(field, value, className);
		return new Gson().toJson(result);
	}
}
