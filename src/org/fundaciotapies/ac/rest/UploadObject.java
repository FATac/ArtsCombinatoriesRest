package org.fundaciotapies.ac.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.fundaciotapies.ac.model.Upload;


@Path("uploadObject")
public class UploadObject {
	
	private static Logger log = Logger.getLogger(UploadObject.class);
	
	@POST
	@Produces("text/plain")
	@Consumes("application/json")
	public String uploadObject(@Context HttpServletRequest httpRequest, String request) {
		String result = "error";
		
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonRequest = m.readValue(request, JsonNode.class);
			
			String className = jsonRequest.path("className").getTextValue();
			List<String> propertiesList = new ArrayList<String>();
			List<String> propertyValuesList = new ArrayList<String>();
			
			for (Iterator<String> it = jsonRequest.getFieldNames();it.hasNext();) {
				String s = it.next();
				if (!"className".equals(s)) {
					if (!"".equals(jsonRequest.path(s).getTextValue())) {
						propertiesList.add(s);
						propertyValuesList.add(jsonRequest.path(s).getTextValue());
					}
				}
			}
			
			String[] properties = new String[propertiesList.size()];
			String[] propertyValues = new String[propertiesList.size()];
			propertiesList.toArray(properties);
			propertyValuesList.toArray(propertyValues);
			
			result = new Upload().uploadObject(className, properties, propertyValues);
		} catch (Exception e) {
			log.error("Error ", e);
			return result;
		}
		
		return result;
	}
}
