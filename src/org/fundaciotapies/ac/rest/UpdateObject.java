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

/**
 * Call: http://{host:port}/objects/{class}/{id}/update
 * <br>
 * Updates object data. Object data must be provided as field-value pairs list in JSON. Updates only the specified fields (non-specified fields are left untouched)<br>
 * Params:<br>
 *  - id: object identifier<br>
 * Return: "success" or "error"
 */
@Path("/resource/{id}/update")
public class UpdateObject {
	private static Logger log = Logger.getLogger(UpdateObject.class);
	

	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String updateObject(@Context HttpServletRequest httpRequest,  String request, @PathParam("id") String id) {
		String result = "error";
		if (request==null || "".equals(request.trim())) return result;
		
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonRequest = m.readValue(request, JsonNode.class);
			
			List<String> propertiesList = new ArrayList<String>();
			List<String> propertyValuesList = new ArrayList<String>();
			
			for (Iterator<String> it = jsonRequest.getFieldNames();it.hasNext();) {
				String s = it.next();
				if (!"type".equals(s) && !"id".equals(s) && !"filePath".equals(s)) {
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
			
			result = new Upload().updateObject(id, properties, propertyValues);
		} catch (Exception e) {
			log.error("Error ", e);
			return new Gson().toJson(result);
		}
		
		return result;
	}
}
