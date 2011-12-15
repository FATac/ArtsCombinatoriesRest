package org.fundaciotapies.ac.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.oai.OAIFilesGenerator;

@Path("/oai")
public class OAI {
	private static Logger log = Logger.getLogger(OAI.class);

	@GET
	public String oai() {
		try {
			new OAIFilesGenerator().generate();
			return "success";
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
	}
}
