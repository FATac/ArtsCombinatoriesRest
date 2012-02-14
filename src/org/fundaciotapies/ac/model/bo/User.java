package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.fundaciotapies.ac.Cfg;

import virtuoso.jdbc3.VirtuosoConnection;
import virtuoso.jdbc3.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc3.VirtuosoPooledConnection;

public class User implements Serializable {
	private static final long serialVersionUID = 1609363309553899275L;
	
	private Long sid = null;
	private String userName = null;
	private String userRole = null;
	
	public Long getSid() {
		return sid;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void load(String login) throws Exception {
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
		      
		    stmt = conn.prepareStatement("SELECT id, login FROM users WHERE login = ? ");
		    stmt.setString(1, login);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = rs.getLong("id");
		    	this.userName = rs.getString("login");
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
			} catch (SQLException e) { throw e; }
		}
		
		if (this.sid==null) return;
		
		try {
		    VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		    String[] serverPort = Cfg.getRdfDatabaseHostPort(); 
		    ds.setServerName(serverPort[0]);
		    ds.setPortNumber(Integer.parseInt(serverPort[1]));
		    ds.setUser(Cfg.RDFDB_USER);
		    ds.setPassword(Cfg.RDFDB_PASS);
		    VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) ds.getPooledConnection();
		    conn = pooledConnection.getVirtuosoConnection();
		      
		    stmt = conn.prepareStatement("SELECT name FROM role_assignments WHERE principal_id = ? ");
		    stmt.setLong(1, sid);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.userRole = rs.getString("name");
		    } else {
		    	this.userRole = null;
		    }
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
				if (rs!=null) rs.close();
			} catch (SQLException e) { throw e; }
		}
		
	}
	
	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	public String getUserRole() {
		return userRole;
	}
	
	
}
