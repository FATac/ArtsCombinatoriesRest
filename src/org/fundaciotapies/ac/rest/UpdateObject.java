package org.fundaciotapies.ac.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.fundaciotapies.ac.model.Upload;

import com.google.gson.Gson;

@Path("/objects/{class}/{id}/update")
public class UpdateObject {
	private static Logger log = Logger.getLogger(UpdateObject.class);
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String updateObject(@Context HttpServletRequest httpRequest,  String request, @PathParam("class") String c, @PathParam("id") String id) {
		String result = "error";
		
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonRequest = m.readValue(request, JsonNode.class);
			
			List<String> propertiesList = new ArrayList<String>();
			List<String> propertyValuesList = new ArrayList<String>();
			
			for (Iterator<String> it = jsonRequest.getFieldNames();it.hasNext();) {
				String s = it.next();
				if (!"className".equals(s) && !"id".equals(s) && !"filePath".equals(s)) {
					propertiesList.add(s);
					propertyValuesList.add(jsonRequest.path(s).getTextValue());
				}
			}
			
			String[] properties = new String[propertiesList.size()];
			String[] propertyValues = new String[propertiesList.size()];
			propertiesList.toArray(properties);
			propertyValuesList.toArray(propertyValues);
			
			result = new Upload().updateObject(c+"/"+id, properties, propertyValues);
		} catch (Exception e) {
			log.error("Error ", e);
			return new Gson().toJson(result);
		}
		
		return result;
	}
}
