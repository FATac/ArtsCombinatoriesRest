package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class ObjectCounter implements Serializable {
	private static final long serialVersionUID = -3923708885085790461L;
	
	private String objectClass;
	private Long counter;
	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}
	public String getObjectClass() {
		return objectClass;
	}
	
	public void load(String objectClass) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    stmt = conn.prepareStatement("SELECT * FROM _object_counter WHERE objectClass = ? ");
		    stmt.setString(1, objectClass);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.objectClass = objectClass;
		    	this.setCounter(rs.getLong("counter"));
		    } else {
		    	this.objectClass = objectClass;
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

		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    String sql = "INSERT INTO _object_counter (objectClass, counter) VALUES (?,?)";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setString(1, objectClass);
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

		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    String sql = "UPDATE _object_counter SET counter = ? WHERE objectClass = ? ";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setLong(1, getCounter());
		    stmt.setString(2, objectClass);
		      
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
	public void setCounter(Long counter) {
		this.counter = counter;
	}
	public Long getCounter() {
		return counter;
	}
	
}
