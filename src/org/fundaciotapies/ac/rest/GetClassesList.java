package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

/**
 * Call: http://{host:port}/classes/list?c=
 * <br>
 * Get a list of all or some classes of the Ontology <br>
 * Param c: Returns subclasses of given class only (optional) <br>
 * Returns JSON list of strings or "error"
 */
@Path("/classes/list")
public class GetClassesList {

	@GET
	@Produces("application/json")
	public String getClassesList(@QueryParam("c") String parentClass) {
		List<String> classesList = null;
		if (parentClass == null)
			classesList = new Request().listOntologyClasses();
		else
			classesList = new Request().listSubclasses(parentClass, false);
			classesList.add(0, parentClass);
			
		return new Gson().toJson(classesList);
	}
}