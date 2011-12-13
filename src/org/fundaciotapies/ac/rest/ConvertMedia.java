package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Upload;

import com.google.gson.Gson;

@Path("media/{id}/convert")
public class ConvertMedia {
	private static Logger log = Logger.getLogger(ConvertMedia.class);
	
	@GET
	@Produces("application/json")
	public String convertMedia(@PathParam("id") String id) {
		try {
			new Upload().convertMediaFile(id);
			return new Gson().toJson("success");
		} catch (Exception e) {
			log.error("Error ", e);
			return new Gson().toJson("error");
		}
	}

}
