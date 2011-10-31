package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.fundaciotapies.ac.model.Request;


@Path("/backup")
public class SaveBackup {
	
	@GET
	@Produces("application/json")
	public String saveBackup() {
		new Request().saveBackup();
		return "success";
	}

}