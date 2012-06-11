package org.fundaciotapies.ac.rest;

import java.sql.Connection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.logic.legal.LegalProcess;
import org.fundaciotapies.ac.logic.legal.support.LegalBlockData;

import virtuoso.jdbc3.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc3.VirtuosoPooledConnection;

import com.google.gson.Gson;

@Path("/legal/restore")
public class LegalRestoreData {
	private static Logger log = Logger.getLogger(LegalRestoreData.class);
	
	@GET
	@Produces("application/json")
	public String legalRestoreData(@QueryParam("key") String key, @QueryParam("value") String value) {
		
		try {
			LegalProcess process = new LegalProcess(); 
		    
		    VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		    String[] serverPort = Cfg.getRdfDatabaseHostPort(); 
		    ds.setServerName(serverPort[0]);
		    ds.setPortNumber(Integer.parseInt(serverPort[1]));
		    ds.setUser(Cfg.RDFDB_USER);
		    ds.setPassword(Cfg.RDFDB_PASS);
		    VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) ds.getPooledConnection();
		    Connection conn = pooledConnection.getVirtuosoConnection();
			
			process.setSqlConnector(conn);
			
			List<LegalBlockData> data = process.restoreData(key, value);
			if (data==null) return "error";
			return new Gson().toJson(data);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
		
	}
}
