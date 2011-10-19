package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.model.Request;

/**
 * Call: http://{host:port}/objects/{class}/{id}/color
 * <br>
 * Get legal color which is assigned to this object <br>
 * Params class: <br>
 * - class: Class name <br>
 * - id: object identifier <br>
 * Returns: HTML color code
 */
@Path("/resource/{id}/color")
public class GetObjectLegalColor {
	
	@GET
	public String getObjectLegalColor(@Context HttpServletResponse response, @PathParam("class") String c, @PathParam("id") String id) {
		return new Request().getObjectLegalColor(c+"/"+id);
	}

}
