package org.fundaciotapies.ac.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.model.Request;
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
	public String getObjectView(@Context HttpServletRequest request, @PathParam("id") String id, @QueryParam("section") String section, @QueryParam("u") String uid) {
		String lang = new Request().getCurrentLanguage(request);
		String[] ids = id.split(",");
		if (ids.length==1) {
			Template result = new ViewGenerator().getObjectView(id, section, uid, lang);
			if (result==null) return new Gson().toJson("Error: Object class has no template");
			return new Gson().toJson(result);
		} else {
			List<Template> result = new ArrayList<Template>();
			for (String oid : ids) {
				Template tmp = new ViewGenerator().getObjectView(oid, section, uid, lang);
				result.add(tmp);
			}
			return new Gson().toJson(result);
		}
	}

}
