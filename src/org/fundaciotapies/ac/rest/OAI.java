package org.fundaciotapies.ac.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.oai.OAIFilesGenerator;

import com.google.gson.Gson;

@Path("/oai")
public class OAI {
	private static Logger log = Logger.getLogger(OAI.class);

	@GET
	@Path("{expr}")
	public String oai(@PathParam("expr") String expression) {
		return generateOAI(expression);
	}

	private String generateOAI(String expression) {
		try {
			new OAIFilesGenerator().generate(expression);
			return new Gson().toJson("success");
		} catch (Exception e) {
			log.error("Error ", e);
			return new Gson().toJson("error");
		}
	}
	
	@GET
	public String oai() {
		return generateOAI(null);
	}
}
