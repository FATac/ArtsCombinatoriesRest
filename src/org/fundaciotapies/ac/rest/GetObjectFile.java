package org.fundaciotapies.ac.rest;

import java.io.DataInputStream;
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
 * Call: http://{host:port}/objects/{class}/{id}/file
 * <br>
 * Get media file linked to specific object. Provided object id must be of a Media type <br>
 * Params class: <br>
 * - class: Class name <br>
 * - id: object identifier <br>
 * Returns: Binary file object by streaming
 */
@Path("/objects/{class}/{id}/file")
public class GetObjectFile {
	private static Logger log = Logger.getLogger(GetObjectFile.class);
	
	@GET
	public String getObjectFile(@Context HttpServletResponse response, @PathParam("class") String c, @PathParam("id") String id, @QueryParam("u") String uid) {
		byte[] content = null;
		
		try {
			
			ObjectFile objectFile = new Request().getObjectFile(c+"/"+id, uid);
			if (objectFile==null) throw new Exception("Object "+c+"/"+id+" has no media file");
			response.setContentType(objectFile.getContentType());
			
			DataInputStream dis = new DataInputStream(objectFile.getInputStream());
	        content = new byte[dis.available()];
	        dis.read(content, 0, dis.available());
	        dis.close();
	        
	        OutputStream out = response.getOutputStream();
	        out.write(content);
	        out.flush();
	        out.close();
		} catch (Exception e) {
			log.error("Error ",e);
		}
		
		return "";
	}
}
 