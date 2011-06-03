package org.fundaciotapies.ac.rest;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.fundaciotapies.ac.logic.LegalProcess;
import org.fundaciotapies.ac.logic.support.LegalBlockData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Path("legalNext")
public class LegalNext {
	private static Logger log = Logger.getLogger(LegalNext.class);
	
	@POST
	@Produces("application/json") 
	public String legalNext(@Context HttpServletRequest httpRequest, String request) {
		List<LegalBlockData> result = null;
		
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonRequest = m.readValue(request, JsonNode.class);
			
			String userId = jsonRequest.path("userId").getTextValue();
			
			Type mapType = new TypeToken<Map<String, String>>() {}.getType();
			
			Map<String, String> dataMap = new Gson().fromJson(request, mapType);			
			result = new LegalProcess().nextBlockData(dataMap, userId);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
	
		if (result!=null) {
			Type listType = new TypeToken<List<LegalBlockData>>() {}.getType();
			return new Gson().toJson(result, listType);
		} else {
			return "success";
		}
	}
}
