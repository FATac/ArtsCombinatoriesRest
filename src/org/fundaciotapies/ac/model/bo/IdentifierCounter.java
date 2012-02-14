package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.fundaciotapies.ac.Cfg;

import virtuoso.jdbc3.VirtuosoConnection;
import virtuoso.jdbc3.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc3.VirtuosoPooledConnection;

public class IdentifierCounter implements Serializable {
	private static final long serialVersionUID = -3923708885085790461L;
	
	private String identifier;
	private Long counter;
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}
	
	public void load(String identifier) throws Exception {
		VirtuosoConnection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
		    VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		    String[] serverPort = Cfg.getRdfDatabaseHostPort(); 
		    ds.setServerName(serverPort[0]);
		    ds.setPortNumber(Integer.parseInt(serverPort[1]));
		    ds.setUser(Cfg.RDFDB_USER);
		    ds.setPassword(Cfg.RDFDB_PASS);
		    VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) ds.getPooledConnection();
		    conn = pooledConnection.getVirtuosoConnection();
		      
		    stmt = conn.prepareStatement("SELECT * FROM _identifier_counter WHERE identifier = ? ");
		    stmt.setString(1, identifier);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.identifier = identifier;
		    	this.setCounter(rs.getLong("counter"));
		    } else {
		    	this.identifier = identifier;
		    	this.setCounter(0l);
		    }
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
				if (rs!=null) rs.close();				
			} catch (Exception e) { throw e; }
		}
	}
	
	public void save() throws Exception {
		VirtuosoConnection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		    String[] serverPort = Cfg.getRdfDatabaseHostPort(); 
		    ds.setServerName(serverPort[0]);
		    ds.setPortNumber(Integer.parseInt(serverPort[1]));
		    ds.setUser(Cfg.RDFDB_USER);
		    ds.setPassword(Cfg.RDFDB_PASS);
		    VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) ds.getPooledConnection();
		    conn = pooledConnection.getVirtuosoConnection();
		      
		    String sql = "INSERT INTO _identifier_counter (identifier, counter) VALUES (?,?)";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setString(1, identifier);
		    stmt.setLong(2, getCounter());
		      
		    stmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			} catch (Exception e) { throw e; }
		}   
	}
	
	public void update() throws Exception {
		VirtuosoConnection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		    String[] serverPort = Cfg.getRdfDatabaseHostPort(); 
		    ds.setServerName(serverPort[0]);
		    ds.setPortNumber(Integer.parseInt(serverPort[1]));
		    ds.setUser(Cfg.RDFDB_USER);
		    ds.setPassword(Cfg.RDFDB_PASS);
		    VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) ds.getPooledConnection();
		    conn = pooledConnection.getVirtuosoConnection();
		      
		    String sql = "UPDATE _identifier_counter SET counter = ? WHERE identifier = ? ";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setLong(1, getCounter());
		    stmt.setString(2, identifier);
		      
		    stmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			} catch (Exception e) { throw e; }
		}   
	}
	
	public static void clear() throws Exception {
		VirtuosoConnection conn = null;
		
		try {
		    VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		    String[] serverPort = Cfg.getRdfDatabaseHostPort(); 
		    ds.setServerName(serverPort[0]);
		    ds.setPortNumber(Integer.parseInt(serverPort[1]));
		    ds.setUser(Cfg.RDFDB_USER);
		    ds.setPassword(Cfg.RDFDB_PASS);
		    VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) ds.getPooledConnection();
		    conn = pooledConnection.getVirtuosoConnection();
		      
		    conn.createStatement().executeUpdate("DELETE FROM _identifier_counter ");
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (conn!=null) conn.close();
			} catch (Exception e) { throw e; }
		}   
	}
	
	public void setCounter(Long counter) {
		this.counter = counter;
	}
	public Long getCounter() {
		return counter;
	}
	
}
