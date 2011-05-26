package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


public class Right implements Serializable {
	private static final long serialVersionUID = -4864107758768373929L;
	
	private Long sid = null;
	private String objectId = null;
	private Integer rightLevel;

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
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
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
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
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

		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
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
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		    
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
	
}
