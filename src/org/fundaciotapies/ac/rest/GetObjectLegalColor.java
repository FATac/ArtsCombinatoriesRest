package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.model.Request;

@Path("getObjectLegalColor")
public class GetObjectLegalColor {
	
	@GET
	public String getObjectLegalColor(@Context HttpServletResponse response, @QueryParam("id") String id) {
		return new Request().getObjectLegalColor(id);
	}

}
