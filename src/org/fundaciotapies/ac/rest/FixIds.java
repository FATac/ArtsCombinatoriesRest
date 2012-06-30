package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.fundaciotapies.ac.model.Upload;

@Path("fixids")
public class FixIds {
	
	@GET
	public String fixids() {
		new Upload().fixIds("s");
		new Upload().fixIds("o");
		return "ok";
	}
}
