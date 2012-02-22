package org.fundaciotapies.ac.rest;

import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.ObjectFile;

import com.google.gson.Gson;

@Path("/")
public class GetMediaFile {
	private static Logger log = Logger.getLogger(GetMediaFile.class);
	
	@GET
	@Produces("application/json")
	@Path("media/{id}")
	public String getMediaFile(@Context HttpServletResponse response, @PathParam("id") String id, @QueryParam("s") String s, @QueryParam("pag") String pag) {
		
		try {
			if ("list".equals(id)) {
				if ("".equals(pag) || pag == null) pag = "0";
				return getMediaList(s, pag);
			}
		} catch (Exception e) {
			log.error("Error ",e);
			return "";
		}
		
		try {
			ObjectFile objectFile = new Request().getMediaFile(id, null);
			if (objectFile==null) {
				log.warn("There is no media file for: " + id);
				return "";
			}
			response.setContentType(objectFile.getContentType());
			
			// Get the response
		    FileInputStream rd = (FileInputStream)objectFile.getInputStream();
		    int len;
		    byte[] buffer = new byte[512];
		    OutputStream out = response.getOutputStream();
		    while ((len = rd.read(buffer)) > 0) {
		    	out.write(buffer, 0, len);
		    }
		    
	        out.close();
		} catch (Exception e) {
			log.error("Error ",e);
		}
		
		return "";
	}
	
	private String getMediaList(String s, String pag) throws Exception {
		return new Gson().toJson(new Request().listMedia(s,pag));
	}
}
 