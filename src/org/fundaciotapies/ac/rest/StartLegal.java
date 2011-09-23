package org.fundaciotapies.ac.rest;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.logic.LegalProcess;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Call: http://{host:port}/legal/start
 * <br>
 * Starts a legal process. It returns the user code that must be provided in every call to LegalNext function.<br>
 * Return: "success" or "error"
 */
@Path("/legal/start")
public class StartLegal {
	
	/**
	 * Starts a legal process. It returns the user code that must be provided in every call to LegalNext function.
	 * @return "success" or "error"
	 */
	@POST
	@Produces("application/json")
	public String startLegal(@Context HttpServletRequest httpRequest,  String request) {
		Type listType = new TypeToken<List<String>>() {}.getType();
		List<String> idList = new Gson().fromJson(request, listType);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("userId", new LegalProcess().startLegal(idList));
		return new Gson().toJson(result);
	}
	
}
