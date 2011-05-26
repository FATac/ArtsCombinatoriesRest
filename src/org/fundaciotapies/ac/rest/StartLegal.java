package org.fundaciotapies.ac.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.fundaciotapies.ac.logic.Legal;

import com.google.gson.Gson;

@Path("startLegal")
public class StartLegal {
	
	@GET
	@Produces("application/json")
	public String startLegal() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("userId", new Legal().startLegal());
		return new Gson().toJson(result);
	}
	
}
