package org.fundaciotapies.ac.rest;

import javax.ws.rs.DELETE;
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
 * - id: Object identifier<br>
 * Returns "success" or "error"
 */
@Path("/resource/{id}/delete")
public class DeleteObject {

	@DELETE
	@Produces("application/json")
	public String deleteObject(@PathParam("id") String id) {
		String result = new Upload().deleteObject(id);
		return new Gson().toJson(result);
	}
}
