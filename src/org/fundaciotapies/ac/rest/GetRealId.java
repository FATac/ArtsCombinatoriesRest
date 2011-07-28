package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

@Path("/getRealId")
public class GetRealId {
	
	@GET
	@Produces("application/json")
	public String getRealId(@QueryParam("class") String c, @QueryParam("id") String id) {
		String result = new Request().getRealId(c,id);
		if (result!=null) return null;
		return result;
	}

}
