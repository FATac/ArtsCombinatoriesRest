package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

@Path("getClassesTree")
public class GetClassesTree {
	
	@GET
	@Produces("text/plain")
	public String getClassesTree(@QueryParam("c") String rootClass) {
		if (rootClass == null) return null;
		
		String json = "\""+rootClass+"\"";
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