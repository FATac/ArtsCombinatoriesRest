package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.model.Request;

@Path("/objects/{class}/{id}/color")
public class GetObjectLegalColor {
	
	@GET
	public String getObjectLegalColor(@Context HttpServletResponse response, @PathParam("class") String c, @PathParam("id") String id) {
		return new Request().getObjectLegalColor(c+"/"+id);
	}

}
