package org.fundaciotapies.ac.rest;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.view.ViewGenerator;

/**
 * Call: http://{host:port}/classes/{class}/thumbnail
 * <br>
 * Get class icon <br>
 * Params class: Class name
 */
@Path("/classes/{class}/thumbnail")
public class GetClassThumbnail {
	private static Logger log = Logger.getLogger(GetClassThumbnail.class);

	@GET
	@Produces("application/json")
	public String getInsertObjectForm(@Context HttpServletResponse response, @PathParam("class") String className) {
		byte[] content = null;
		
		try {
			InputStream in = new FileInputStream(new ViewGenerator().getClassThumbnail(className));
			response.setContentType("image/jpg");
			
			DataInputStream dis = new DataInputStream(in);
	        content = new byte[dis.available()];
	        dis.read(content, 0, dis.available());
	        dis.close();
	        
	        OutputStream out = response.getOutputStream();
	        out.write(content);
	        out.flush();
	        out.close();
	        in.close();
		} catch (Exception e) {
			log.error("Error ",e);
		}
		
		return "";
	}
}