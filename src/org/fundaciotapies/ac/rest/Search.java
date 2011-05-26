package org.fundaciotapies.ac.rest;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("search")
public class Search {

	@GET
	@Produces("application/json")
	public String search(@QueryParam("s") String word, @QueryParam("c") String className) {
		Map<String, Map<String, String>> result = new Request().search(word, className);
		return new Gson().toJson(result);
	}
}
