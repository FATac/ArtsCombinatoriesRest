package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Upload;

@Path("/solarize")
public class Solarize {
	@GET
	@Produces("text/xml")
	public String solarize(@QueryParam("option") String option) {
		try {
			if ("commit".equals(option)) {
				new Upload().solrCommit();
			} else if ("clear".equals(option)) {
				new Upload().solrDeleteAll();
			} else {
				return new Upload().solarize();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return null;
	}
}
