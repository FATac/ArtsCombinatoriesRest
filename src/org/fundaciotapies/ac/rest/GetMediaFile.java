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
@Path("/media/{id}")
public class GetMediaFile {
	private static Logger log = Logger.getLogger(GetMediaFile.class);
	
	@GET
	public String getObjectFile(@Context HttpServletResponse response, @PathParam("id") String id, @QueryParam("u") String uid) {
		
		try {
			ObjectFile objectFile = new Request().getMediaFile(id, uid);
			if (objectFile==null) throw new Exception("There is no media file");
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
 