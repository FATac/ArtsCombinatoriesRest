package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.fundaciotapies.ac.model.Upload;

import com.google.gson.Gson;

/**
 * Call: http://{host:port}/objects/{class}/{id}/delete
 * <br>
 * Deletes referenced object<br>
 * Params:<br>
 * - class: Object class<br>
 * - id: Object identifier<br>
 * Returns "success" or "error"
 */
@Path("/objects/{class}/{id}/delete")
public class DeleteObject {

	@GET
	@Produces("application/json")
	public String deleteObject(@PathParam("class") String c, @PathParam("id") String id) {
		String result = new Upload().deleteObject(c+"/"+id);
		return new Gson().toJson(result);
	}
}
