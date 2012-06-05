package org.fundaciotapies.ac.rest;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Upload;

@Path("/media/upload")
public class UploadMedia {
	private static Logger log = Logger.getLogger(UploadMedia.class);

	@POST
	@Produces("application/json")
	public String uploadMedia(@Context HttpServletRequest request, @QueryParam("fn") String fn) {
		try {
			InputStream in = request.getInputStream();
			return new Upload().addMediaFile(in, fn);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
	}

}
