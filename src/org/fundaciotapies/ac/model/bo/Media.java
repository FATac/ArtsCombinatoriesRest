package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class Media implements Serializable {
	private static final long serialVersionUID = 944291245319558902L;
	
	private Long sid = null;
	private String objectId;
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
	
	public void load(Long sid) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    stmt = conn.prepareStatement("SELECT * FROM _media WHERE sid = ? ");
		    stmt.setLong(1, sid);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = sid;
		    	this.path = rs.getString("path");
		    	this.objectId = rs.getString("objectId");
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
	
	public void load(String objectId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    stmt = conn.prepareStatement("SELECT * FROM _media WHERE objectId = ? ");
		    stmt.setString(1, objectId);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = rs.getLong("sid");
		    	this.path = rs.getString("path");
		    	this.objectId = rs.getString("objectId");
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
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    String sql = "UPDATE _media SET path = ?,objectId=? WHERE sid = ? ";
		    if (sid ==null) sql = "INSERT INTO _media (path,objectId) VALUES (?,?)";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setString(1, path);
		    stmt.setString(2, objectId);
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
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    if (sid==null) {
		    	if (objectId!=null) {
		    		String sql = "DELETE FROM _media WHERE objectId = ? ";
		    		stmt = conn.prepareStatement(sql);
		    		stmt.setString(1, objectId);
		    	} else throw new Exception("Either sid or objectId values must be set to perform deletion");
		    } else {
		    	String sql = "DELETE FROM _media WHERE sid = ? "; 
		    	stmt = conn.prepareStatement(sql);
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
	
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	public String getObjectId() {
		return objectId;
	}
	
	
}
