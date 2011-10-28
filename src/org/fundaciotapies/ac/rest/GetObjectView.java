package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.support.Template;
import org.fundaciotapies.ac.view.ViewGenerator;

import com.google.gson.Gson;

/**
 * Call: http://{host:port}/object/{class}/{id}/view
 * <br>
 *  Get fields and data of a specific object view as defined on its corresponding template <br>
 * Params <br>
 * - class: Class name <br>
 * - id: Object identifier <br>
 * Returns: field-value pairs list in JSON
 */
@Path("/resource/{id}/view")
public class GetObjectView {

	@GET
	@Produces("application/json")
	public String getObjectView(@PathParam("id") String id, @QueryParam("u") String uid) {
		Template result = new ViewGenerator().getObjectView(id);
		if (result==null) return new Gson().toJson("Error: Object class has no template");
		return new Gson().toJson(result);
	}

}
