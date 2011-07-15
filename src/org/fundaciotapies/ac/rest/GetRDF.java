package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.fundaciotapies.ac.model.Request;


@Path("/rdf")
public class GetRDF {
	
	@GET
	@Produces("text/plain; charset=UTF-8")
	public String getRdf() {
		return new Request().getRdf();
	}

}
