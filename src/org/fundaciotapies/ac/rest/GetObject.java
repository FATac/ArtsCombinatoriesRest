package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.CustomMap;

import com.google.gson.Gson;

/**
 * Call: http://{host:port}/object/{class}/{id}
 * <br>
 *  Get fields and data of a specific object <br>
 * Params <br>
 * - class: Class name <br>
 * - id: Object identifier <br>
 * Returns: field-value pairs list in JSON
 */
@Path("/objects/{class}/{id}")
public class GetObject {

	@GET
	@Produces("application/json")
	public String getObject(@PathParam("class") String c, @PathParam("id") String id, @QueryParam("u") String uid) {
		CustomMap result = new Request().getObject(c+"/"+id, uid);
		if (result==null) return new Gson().toJson("Error: Object could not be found");
		return new Gson().toJson(result);
	}

}
