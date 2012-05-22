package org.fundaciotapies.ac.rest;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Upload2;

@Path("/media/upload")
public class UploadMedia {
	private static Logger log = Logger.getLogger(UploadMedia.class);

	@POST
	@Produces("application/json")
	public String uploadMedia(@Context HttpServletRequest request, @QueryParam("fn") String fn) {
		try {
			//InputStream in = request.getInputStream();
			File file = new File(URLDecoder.decode(fn, "utf-8"));
		    FileInputStream in = new FileInputStream(file);
		    String[] parts = fn.split("\\/");
			String fileNameOnly = parts[parts.length-1];
			return new Upload2().addMediaFile(in, fileNameOnly);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
	}

}
