package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

/**
 * Call: http://{host:port}/classes/tree?c=
 * <br>
 * Get class structure tree starting from given root class <br>
 * Params c: root class
 */
@Path("/classes/tree")
public class GetClassesTree {
	
	@GET
	@Produces("application/json")
	public String getClassesTree(@QueryParam("c") String rootClass) {
		String json = "\""+rootClass+"\"";
		if (rootClass==null) json = "\"Thing\"";
		
		List<String> classesList = new Request().listSubclasses(rootClass, true);
		String json2 = "";
		for (String c : classesList)
			json2 += getClassesTree(c)+", ";
		
		if (!"".equals(json2)) {
			if (classesList.size() > 1)
				json = json + ": [ " +  json2.substring(0,json2.length()-2) + " ] ";
			else
				json = json + ":" +  json2.substring(0,json2.length()-2);
			json = " { " + json + " } ";
		}
			
		return json;
	}
}