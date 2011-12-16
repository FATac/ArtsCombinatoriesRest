package org.fundaciotapies.ac.rest;

import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.ObjectFile;

/**
 * Call: http://{host:port}/media/{id}
 * <br>
 * Get media file linked to specific object. Provided object id must be of a Media type <br>
 * Params class: <br>
 * - id: object identifier <br>
 * Returns: Binary file object by streaming
 */
@Path("/")
public class GetMediaFile {
	private static Logger log = Logger.getLogger(GetMediaFile.class);
	
	@GET
	@Path("media/{id}/profile/{profile}")
	public String getMediaFileProfile(@Context HttpServletResponse response, @PathParam("id") String id, @PathParam("profile") String profile, @QueryParam("u") String uid) {
		
		try {
			ObjectFile objectFile = new Request().getMediaFile(id, profile, uid);
			if (objectFile==null) {
				log.warn("There is no media file for: " + id + " profile: " + profile);
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
	
	@GET
	@Path("media/{id}")
	public String getMediaFile(@Context HttpServletResponse response, @PathParam("id") String id, @QueryParam("u") String uid) {
		
		try {
			ObjectFile objectFile = new Request().getMediaFile(id, null, uid);
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
}
 