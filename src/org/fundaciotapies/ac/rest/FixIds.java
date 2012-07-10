package org.fundaciotapies.ac.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Upload;

@Path("assignred")
public class FixIds {
	
	@GET
	public String fixids(@QueryParam("q") String q) {
		new Upload().fixIds(q);
		return "ok";
	}
}
