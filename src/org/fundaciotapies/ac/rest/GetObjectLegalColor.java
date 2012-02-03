package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.model.Request;

@Path("/resource/{id}/color")
public class GetObjectLegalColor {
	
	@GET
	public String getObjectLegalColor(@Context HttpServletResponse response, @PathParam("id") String id) {
		return new Request().getObjectLegalColorRGB(id);
	}

}
