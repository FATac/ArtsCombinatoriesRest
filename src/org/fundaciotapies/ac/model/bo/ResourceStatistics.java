package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import org.fundaciotapies.ac.Cfg;

import virtuoso.jdbc3.VirtuosoConnection;
import virtuoso.jdbc3.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc3.VirtuosoPooledConnection;

public class ResourceStatistics implements Serializable {
	private static final long serialVersionUID = 723650552242888095L;
	
	private String identifier;
	private Long lastMoment;
	private Long creationMoment;
	private Long visitCounter;
	
	public static List<String> listRecentChanges(long updatePeriod) throws Exception {
		List<String> result = new ArrayList<String>();
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
		    
		    long currentMoment = Calendar.getInstance().getTimeInMillis();
		      
		    stmt = conn.prepareStatement("SELECT identifier FROM _resource_statistics WHERE creationMoment > ? OR lastMoment > ? OR deletion > ? ");
		    stmt.setLong(1, currentMoment - updatePeriod);
		    stmt.setLong(2, currentMoment - updatePeriod);
		    stmt.setLong(3, currentMoment - updatePeriod);
		      
		    rs = stmt.executeQuery();
		    while(rs.next()) result.add(rs.getString("identifier"));
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
				if (rs!=null) rs.close();				
			} catch (Exception e) { throw e; }
		}
		
		return result;
	}
	
	public static void creation(String identifier) throws Exception {
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
		      
		    String sql = "INSERT INTO _resource_statistics (identifier, visitCounter, creationMoment) VALUES (?, ?, ?)";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setString(1, identifier);
		    stmt.setLong(2, 0);
		    stmt.setLong(3, Calendar.getInstance().getTimeInMillis());
		      
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
	
	public static void deletion(String identifier) throws Exception {
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
		      
		    String sql = "UPDATE _resource_statistics SET deletion = ? WHERE identifier = ? ";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setLong(1, Calendar.getInstance().getTimeInMillis());
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
	
	public static void visit(String identifier) throws Exception {
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
		      
		    String sql = "UPDATE _resource_statistics SET visitCounter = visitCounter + 1, lastMoment = ? WHERE identifier = ? ";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setLong(1, Calendar.getInstance().getTimeInMillis());
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
	
	public static List<String[]> list() throws Exception {
		List<String[]> result = new ArrayList<String[]>();
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
		      
		    stmt = conn.prepareStatement("SELECT * FROM _resource_statistics");
		      
		    rs = stmt.executeQuery();
		    while(rs.next()) {
		    	String[] reg = { "identifier", "visitCounter", "creationMoment", "lastMoment"};
		    	reg[0] = rs.getString(reg[0]);
		    	reg[1] = rs.getString(reg[1]);
		    	reg[2] = rs.getString(reg[2]);
		    	if (rs.getString(reg[3])!=null) reg[3] = rs.getString(reg[3]); else reg[3] = null;
		    		
		    	result.add(reg);
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
		
		return result;
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
		      
		    stmt = conn.prepareStatement("SELECT * FROM _resource_statistics WHERE identifier = ? ");
		    stmt.setString(1, identifier);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.identifier = identifier;
		    	this.visitCounter = rs.getLong("visitCounter");
		    	this.setLastMoment(rs.getLong("lastMoment"));
		    	this.setCreationMoment(rs.getLong("creationMoment"));
		    } else {
		    	this.identifier = null;
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
	
	public static void clear() throws Exception {
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
		      
		    conn.createStatement().executeUpdate("DELETE FROM _resource_statistics ");
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			} catch (Exception e) { throw e; }
		}   
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setVisitCounter(Long visitCounter) {
		this.visitCounter = visitCounter;
	}

	public Long getVisitCounter() {
		return visitCounter;
	}

	public void setLastMoment(Long lastMoment) {
		this.lastMoment = lastMoment;
	}

	public Long getLastMoment() {
		return lastMoment;
	}

	public void setCreationMoment(Long creationMoment) {
		this.creationMoment = creationMoment;
	}

	public Long getCreationMoment() {
		return creationMoment;
	}

}
