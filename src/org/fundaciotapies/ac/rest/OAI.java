package org.fundaciotapies.ac.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.oai.OAIFilesGenerator;

import com.google.gson.Gson;

@Path("/oai")
public class OAI {
	private static Logger log = Logger.getLogger(OAI.class);

	@GET
	public String oai() {
		try {
			new OAIFilesGenerator().generate();
			return new Gson().toJson("success");
		} catch (Exception e) {
			log.error("Error ", e);
			return new Gson().toJson("error");
		}
	}
}
