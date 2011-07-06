package org.fundaciotapies.ac.rest;

import java.sql.Connection;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.logic.LegalProcess;
import org.fundaciotapies.ac.logic.support.LegalBlockData;

import com.google.gson.Gson;

@Path("legalRestoreData")
public class LegalRestoreData {
	private static Logger log = Logger.getLogger(LegalRestoreData.class);
	
	@GET
	@Produces("application/json")
	public String legalRestoreData(@QueryParam("key") String key, @QueryParam("value") String value, @QueryParam("userId") String userId) {
		
		try {
			LegalProcess process = new LegalProcess(); 
			
			javax.naming.Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    Connection conn = ds.getConnection();
			
			process.setSqlConnector(conn);
			
			List<LegalBlockData> data = process.restoreData(key, value, userId);
			if (data==null) return "error";
			return new Gson().toJson(data);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
		
	}
}
