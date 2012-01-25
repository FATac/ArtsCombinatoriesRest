package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("/resource/{id}/exists")
public class GetObjectExists {
	private static Logger log = Logger.getLogger(GetObjectExists.class);
	
	@GET
	@Produces("application/json")
	public String getObjectExists(@PathParam("id") String id) {
		
		try {
			String resp = new Request().getObjectClass(id);
			if (resp!=null) return new Gson().toJson("true");
			else return new Gson().toJson("false");
		} catch (Exception e) {
			log.error("Error " + e);
		}
		
		return "";
	}
}
