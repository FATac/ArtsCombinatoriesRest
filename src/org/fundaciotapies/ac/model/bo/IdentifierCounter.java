package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

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
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
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

		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
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

		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
		    Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
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
	public void setCounter(Long counter) {
		this.counter = counter;
	}
	public Long getCounter() {
		return counter;
	}
	
}
