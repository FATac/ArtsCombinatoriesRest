package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

@Path("/classes/tree")
public class GetClassesTree {
	
	@GET
	@Produces("application/json")
	public String getClassesTree(@QueryParam("c") String rootClass) {
		String[] rootClassList = new String[0];
		if ("".equals(rootClass)) rootClass = null;
		if (rootClass!=null) rootClassList = rootClass.split(",");
		
		if (rootClassList.length>1) {
			String json = "[";
			for(String r : rootClassList) {
				json += getClassesTree(r.trim()) + ",";
			}
			
			json = json.substring(0,json.length()-1) + "]";
			
			return json;
		} else {
			String json = "\""+rootClass+"\"";
			if (rootClass==null) json = "\"owl:Thing\"";
			
			List<String> classesList = new Request().listSubclasses(rootClass, true);
			String json2 = "";
			for (String c : classesList)
				json2 += getClassesTree(c)+", ";
			
			if (!"".equals(json2)) {
				json = json + ": [ " +  json2.substring(0,json2.length()-2) + " ] ";
				json = " { " + json + " } ";
			}
			
			return json;
		}
	}
}