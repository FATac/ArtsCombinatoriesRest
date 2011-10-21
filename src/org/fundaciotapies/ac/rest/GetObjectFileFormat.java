package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

/**
 * Call: http://{host:port}/media/{id}/format
 * <br>
 * Get media file format <br>
 * Params class: <br>
 * - id: object identifier <br>
 * Returns: Format
 */
@Path("/media/{id}/format")
public class GetObjectFileFormat {
	private static Logger log = Logger.getLogger(GetObjectFileFormat.class);
	
	@GET
	public String getObjectFile(@Context HttpServletResponse response, @PathParam("id") String id) {
		try {
			return new Gson().toJson(new Request().getObjectFileFormat(id));
		} catch (Exception e) {
			log.error("Error ",e);
		}
		
		return "";
	}
}
 