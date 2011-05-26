package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.fundaciotapies.ac.logic.Legal;
import org.fundaciotapies.ac.logic.support.Question;

import com.google.gson.Gson;

@Path("legalNext")
public class LegalNext {
	private static Logger log = Logger.getLogger(LegalNext.class);
	
	@POST
	@Produces("application/json") 
	public String legalNext(@Context HttpServletRequest httpRequest, String request) {
		Question result = null;
		
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonRequest = m.readValue(request, JsonNode.class);
			
			String responseId = jsonRequest.path("responseId").getTextValue();
			String userId = jsonRequest.path("userId").getTextValue();
			
			result = new Legal().nextQuestion(responseId, userId);
		} catch (Exception e) {
			log.error("Error ", e);
			return "Error";
		}
	
		return new Gson().toJson(result);
	}
}
