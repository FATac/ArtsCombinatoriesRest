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

public class Media implements Serializable {
	private static final long serialVersionUID = 944291245319558902L;
	
	private Long sid = null;
	private String mediaId = null;
	private String path;
	private String moment;
	
	public String getMoment() {
		return moment;
	}
	public Long getSid() {
		return sid;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public List<String> list() throws Exception {
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
		      
		    stmt = conn.prepareStatement("SELECT path FROM _media ORDER BY path ");
		      
		    rs = stmt.executeQuery();
		    while (rs.next()) result.add(rs.getString("path"));
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
		      
		    stmt = conn.prepareStatement("SELECT * FROM _media WHERE sid = ? ");
		    stmt.setLong(1, sid);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = sid;
		    	this.path = rs.getString("path");
		    	this.mediaId = rs.getString("mediaId");
		    	this.moment = rs.getString("moment");
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
	
	public void load(String mediaId) throws Exception {
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
		      
		    stmt = conn.prepareStatement("SELECT * FROM _media WHERE mediaId = ? ");
		    stmt.setString(1, mediaId);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = rs.getLong("sid");
		    	this.path = rs.getString("path");
		    	this.mediaId = rs.getString("mediaId");
		    	this.moment = rs.getString("moment");
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
		      
		    String sql = "UPDATE _media SET path = ?,mediaId=? WHERE sid = ? ";
		    if (sid ==null) sql = "INSERT INTO _media (path,mediaId) VALUES (?,?)";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setString(1, path);
		    stmt.setString(2, mediaId);
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
		    	if (mediaId!=null) {
		    		String sql = "DELETE FROM _media WHERE mediaId = ? ";
		    		stmt = conn.prepareStatement(sql);
		    		stmt.setString(1, mediaId);
		    	} else throw new Exception("Either sid or mediaId values must be set to perform deletion");
		    } else {
		    	String sql = "DELETE FROM _media WHERE sid = ? "; 
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
		      
		    conn.createStatement().executeUpdate("DELETE FROM _media");
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			} catch (Exception e) { throw e; }
		}   
	}
	
	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}
	
	public String getMediaId() {
		return mediaId;
	}
	
	
}
