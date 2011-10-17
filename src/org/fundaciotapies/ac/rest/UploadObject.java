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


/**
 * Call: http://{host:port}/objects/{class}/{id}/upload
 * <br>
 * Uploads object. Class must be specified and data must be provided as field-value list in JSON
 * <br>
 * Return: Identifier of the uploaded object or "error"
 */
@Path("/objects/upload")
public class UploadObject {
	
	private static Logger log = Logger.getLogger(UploadObject.class);
	
	@POST
	@Produces("text/plain")
	@Consumes("application/json")
	public String uploadObject(@Context HttpServletRequest httpRequest, String request) {
		String result = "error";
		if (request==null || "".equals(request.trim())) return result;
		
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonRequest = m.readValue(request, JsonNode.class);
			
			String className = jsonRequest.path("className").getTextValue();
			String about = jsonRequest.path("about").getTextValue();
			List<String> propertiesList = new ArrayList<String>();
			List<String> propertyValuesList = new ArrayList<String>();
			
			for (Iterator<String> it = jsonRequest.getFieldNames();it.hasNext();) {
				String s = it.next();
				if (!"className".equals(s)) {
					if (!jsonRequest.path(s).isArray()) {
						if (!"".equals(jsonRequest.path(s).getTextValue())) {
							propertyValuesList.add(jsonRequest.path(s).getTextValue());
							propertiesList.add(s);
						}
					} else {
						for (Iterator<JsonNode> it2 = jsonRequest.path(s).getElements();it2.hasNext();) {
							propertyValuesList.add(it2.next().getTextValue());
							propertiesList.add(s);
						}
					}
				}
			}
			
			String[] properties = new String[propertiesList.size()];
			String[] propertyValues = new String[propertiesList.size()];
			propertiesList.toArray(properties);
			propertyValuesList.toArray(propertyValues);
			
			result = new Upload().uploadObject(className, about, properties, propertyValues);
		} catch (Exception e) {
			log.error("Error ", e);
			return result;
		}
		
		return result;
	}
}
