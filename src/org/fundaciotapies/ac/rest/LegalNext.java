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
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.logic.legal.LegalProcess;
import org.fundaciotapies.ac.logic.legal.support.LegalBlockData;

import virtuoso.jdbc3.VirtuosoConnection;
import virtuoso.jdbc3.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc3.VirtuosoPooledConnection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Path("/legal/next")
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
			LegalProcess process = new LegalProcess();
			
			VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		    String[] serverPort = Cfg.getRdfDatabaseHostPort(); 
		    ds.setServerName(serverPort[0]);
		    ds.setPortNumber(Integer.parseInt(serverPort[1]));
		    ds.setUser(Cfg.RDFDB_USER);
		    ds.setPassword(Cfg.RDFDB_PASS);
		    VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) ds.getPooledConnection();
		    VirtuosoConnection conn = pooledConnection.getVirtuosoConnection();
			
			process.setSqlConnector(conn);
			result = process.nextBlockData(dataMap, userId);
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