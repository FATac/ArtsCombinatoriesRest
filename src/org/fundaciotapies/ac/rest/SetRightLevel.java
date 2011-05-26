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
import org.fundaciotapies.ac.logic.Legal;

@Path("setRightLevel")
public class SetRightLevel {
	private static Logger log = Logger.getLogger(SetRightLevel.class);	
	
	@POST
	@Produces("text/plain")
	@Consumes("application/json")
	public String setRightLevel(@Context HttpServletRequest httpRequest, String request) {
	
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonRequest = m.readValue(request, JsonNode.class);
			String color = jsonRequest.path("color").getTextValue();
			
			List<String> objectIdList = new ArrayList<String>();
			for (Iterator<JsonNode> it = jsonRequest.path("objectIdList").getElements();it.hasNext();) {
				JsonNode node = it.next();
				objectIdList.add(node.getTextValue());
			}
			
			new Legal().setObjectsRight(objectIdList, color);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
		
		return "success";
	}
}
