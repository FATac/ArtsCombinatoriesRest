package org.fundaciotapies.ac.rest;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.view.ViewGenerator;

@Path("/resource/{id}/thumbnail")
public class GetObjectThumbnail {
	private static Logger log = Logger.getLogger(GetObjectThumbnail.class);
	
	@GET
	public String getObjectThumbnail(@Context HttpServletResponse response, @PathParam("id") String id, @QueryParam("u") String uid) {
		byte[] content = null;
		
		try {
			InputStream in = new ViewGenerator().getObjectThumbnail(id, uid, true);
			if (in==null) {
				return "";
			}
			response.setContentType("image/jpg");
			
			DataInputStream dis = new DataInputStream(in);
	        content = new byte[dis.available()];
	        dis.read(content, 0, dis.available());
	        dis.close();
	        
	        OutputStream out = response.getOutputStream();
	        out.write(content);
	        out.flush();
	        out.close();
		} catch (Exception e) {
			log.error("Error " + e);
		}
		
		return "";
	}
}
