package org.fundaciotapies.ac.model.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class User implements Serializable {
	private static final long serialVersionUID = 1609363309553899275L;
	
	private Long sid = null;
	private String userName;
	private String pwd;
	private String userType;
	
	public Long getSid() {
		return sid;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	public void load(Long sid) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			Context ctx = new InitialContext();
		    DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/virtuosoDB");
		    conn = ds.getConnection();
		      
		    stmt = conn.prepareStatement("SELECT * FROM _user WHERE sid = ? ");
		    stmt.setLong(1, sid);
		      
		    rs = stmt.executeQuery();
		    if (rs.next()) {
		    	this.sid = sid;
		    	this.userName = rs.getString("userName");
		    	this.pwd = rs.getString("pwd");
		    	this.userType = rs.getString("userType");
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
		      
		    String sql = "UPDATE _user SET userName = ?, pwd = ?, userType = ? WHERE sid = ? ";
		    if (sid ==null) sql = "INSERT INTO _user (userName, pwd, userType) VALUES (?,?,?)";
		    stmt = conn.prepareStatement(sql);
		    	  
		    stmt.setString(1, userName);
		    stmt.setString(2, pwd);
		    stmt.setString(3, userType);
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
	
}
