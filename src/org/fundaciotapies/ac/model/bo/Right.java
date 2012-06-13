package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


import org.fundaciotapies.ac.Cfg;

import virtuoso.jdbc3.VirtuosoConnection;
import virtuoso.jdbc3.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc3.VirtuosoPooledConnection;


public class Right implements Serializable {
	private static final long serialVersionUID = -4864107758768373929L;
	
	private Long sid = null;
	private String objectId = null;
	private Integer rightLevel = 4;

	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public void setRightLevel(Integer rightLevel) {
		this.rightLevel = rightLevel;
	}
	public Integer getRightLevel() {
		return rightLevel;
	}
	public Long getSid() {
		return sid;
	}
	
	public void load(Long sid) throws Exception {
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
		      
		    stmt = conn.prepareStatement("SELECT * FROM _right WHERE sid = ? ");
		    stmt.setLong(1, sid);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = sid;
		    	this.objectId = rs.getString("objectId");
		    	this.rightLevel = rs.getInt("rightLevel");
		    } else {
		    	this.sid = null;
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
	
	public void load(String objectId) throws Exception {
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
		    
		    stmt = conn.prepareStatement("SELECT * FROM _right WHERE objectId = ? ");
		    stmt.setString(1, objectId);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = rs.getLong("sid");
		    	this.objectId = rs.getString("objectId");
		    	this.rightLevel = rs.getInt("rightLevel");
		    } else {
		    	this.sid = null;
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
	
	public void saveUpdate() throws Exception {
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
		      
		    String sql = "UPDATE _right SET objectId = ?, rightLevel = ? WHERE sid = ? ";
		    if (sid == null) sql = "INSERT INTO _right (objectId, rightLevel) VALUES (?,?)";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setString(1, objectId);
		    stmt.setInt(2, rightLevel);
		    if (sid != null) stmt.setLong(3, sid);
		      
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
	
	public void delete() throws Exception {
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
		    
		    if (sid==null) {
		    	if (objectId!=null) {
		    		String sql = "DELETE FROM _right WHERE objectId = ? ";
		    		stmt = conn.prepareStatement(sql);
		    		stmt.setString(1, objectId);
		    	} else throw new Exception("Either sid or objectId values must be set to perform deletion");
		    } else {
		    	String sql = "DELETE FROM _right WHERE sid = ? "; 
		    	stmt = conn.prepareStatement(sql);
		    	stmt.setLong(1, sid);
		    }
		    
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
	
	public static List<String> list(Integer rightLevel) throws Exception {
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
		    
    		String sql = "SELECT * FROM _right";
    		if (rightLevel!=null) sql += " WHERE rightLevel = ? ";
		    		
    		stmt = conn.prepareStatement(sql);
    		if (rightLevel!=null) stmt.setInt(1, rightLevel);
    		
    		ResultSet rs = stmt.executeQuery();
    		List<String> result = new ArrayList<String>();
    		
    		while(rs.next()) {
    			result.add(rs.getString("objectId"));
    			result.add(rs.getString("rightLevel"));
    		}
		    
		    return result;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			} catch (Exception e) { throw e; }
		}
		
	}
	
}
