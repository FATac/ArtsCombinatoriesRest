package org.fundaciotapies.ac.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.logic.support.LegalAutodata;
import org.fundaciotapies.ac.logic.support.LegalBlock;
import org.fundaciotapies.ac.logic.support.LegalBlockData;
import org.fundaciotapies.ac.logic.support.LegalBlockRules;
import org.fundaciotapies.ac.logic.support.LegalDefinition;
import org.fundaciotapies.ac.logic.support.LegalExpressionCompiler;
import org.fundaciotapies.ac.model.bo.Right;


import com.google.gson.Gson;

public class LegalProcess {
	private static Logger log = Logger.getLogger(LegalProcess.class);
	
	private Connection sqlConnector = null;
	
	public void setObjectsRight(List<String> objectIdList, String color) {
		try {
			Right r = new Right();
			for (String id : objectIdList) {
				if ("".equals(id.trim())) continue;
				
				r.setObjectId(id);
				r.delete();
				
				if ("red".equals(color)) {
					r.setRightLevel(4);
				} else if ("orange".equals(color)) {
					r.setRightLevel(3);
				} else if ("yellow".equals(color)) {
					r.setRightLevel(2);
				} else {
					r.setRightLevel(1);
				}
				
				r.saveUpdate();
			}
		} catch (Exception e) {
			log.error("Exception ", e);
		}
	}
	
	public String startLegal(List<String> objectIdList) {
		String result = null;
		
		try {
			result = "user_" + Math.round(Math.random()*100000000);
			String idList = "";
			for (String id: objectIdList) idList += id + ",";
			
			Properties prop = new Properties();
			prop.setProperty("___lastBlock", "");
			prop.setProperty("___objects", idList);
			
			prop.store(new FileOutputStream(result + ".properties"), null);
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		
		return result;
	}
	
	private String abortLegal(String user) {
		new File(user + ".properties").delete();
		return null;
	}
	
	
	// TODO: extensively test expression evaluator
	private Boolean evalExpression(String exp, Properties data) throws Exception {
		LegalExpressionCompiler compiler = new LegalExpressionCompiler();
		compiler.setData(data);
		return (Boolean)compiler.eval(exp);		
	}
	
	public List<LegalBlockData> nextBlockData(Map<String, String> data, String user) {
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(user + ".properties"));
			
			FileReader f = new FileReader(new File("/home/jordi.roig.prieto/workspace/ArtsCombinatoriesRest/json/legal4.json"));
			LegalDefinition def = new Gson().fromJson(f, LegalDefinition.class);
			f.close();
			
			String lastBlock = prop.getProperty("___lastBlock");
			
			/*for(Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();it.hasNext();) {
				Map.Entry<String, String> d = it.next();
				
				if (d.getValue()!=null)
					prop.setProperty(d.getKey(), d.getValue());
			}*/
			
			if ("".equals(lastBlock) || lastBlock == null) {
				LegalBlock b = def.getBlock(def.getStartBlock());
				prop.setProperty("___lastBlock", b.getName());
				prop.store(new FileOutputStream(user + ".properties"), null);
				return restoreData(b, prop);
			}
			
			
			LegalBlock b = def.getBlock(lastBlock);
			
			for (LegalBlockData d : b.getData()) {
				if (d==null) break;
				String value = data.get(d.getName());
				if (value!=null) {
					prop.setProperty(d.getName(), value);
				} else if (d.getType().equals("boolean")) {
					prop.setProperty(d.getName(), "false");
				} else {
					prop.setProperty(d.getName(), "");
				}
			}
			
			storeData(b, data, prop);
			
			for (LegalBlockRules r : b.getRules()) {
				Boolean res = evalExpression(r.getExp(), prop);
				if (res!=null && res) {
					if (r.getResult().getBlock()!=null) {
						LegalBlock b2 = def.getBlock(r.getResult().getBlock());
						if (b2==null) throw new Exception("Cannot fin block " + r.getResult().getBlock());
						prop.setProperty("___lastBlock", b2.getName());
						prop.store(new FileOutputStream(user + ".properties"), null);
						return restoreData(b2, prop);
					} else {
						//TODO: Save legal data into ontology
						String color = r.getResult().getColor();
						String objectIds = prop.getProperty("___objects");
						setObjectsRight(Arrays.asList(objectIds.split(",")), color);
						
						new File(user + ".properties").delete();
						
						return null;
					}
				}
			}
			
		} catch (Exception e) {
			abortLegal(user);
			log.error("Exception ", e);
		} finally {
			try { if (!sqlConnector.isClosed()) sqlConnector.close(); } catch (Exception e) {}
		}
		
		return null;
	}
	
	private void storeData(LegalBlock b, Map<String, String> data, Properties prop) throws Exception {
		if (b.getAutodata()==null) return;
		if (sqlConnector==null) throw new Exception("Sql connector must be provided when autodata feature is used!");
		
		LegalAutodata lad = b.getAutodata();
		String keyVal = prop.getProperty(lad.getKey());
		
		PreparedStatement pstmt = null;
		
		try {
			for (LegalBlockData d : b.getData()) {
				if (d==null) break;
				pstmt = sqlConnector.prepareStatement("DELETE FROM autodata_table WHERE keyName = ? AND keyValue = ? AND name = ? ");
				pstmt.setString(1, lad.getKey());
				pstmt.setString(2, keyVal);
				pstmt.setString(3, d.getName());
				pstmt.executeUpdate();
				pstmt.close();
				
				pstmt = sqlConnector.prepareStatement("INSERT INTO autodata_table (keyName,keyValue,name,defaultValue) VALUES (?,?,?,?) ");
				pstmt.setString(1, lad.getKey());
				pstmt.setString(2, keyVal);
				pstmt.setString(3, d.getName());
				pstmt.setString(4, data.get(d.getName()));
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (pstmt!=null) try { pstmt.close(); } catch (Exception e) {}
		}
	}
	
	private List<LegalBlockData> restoreData(LegalBlock b, Properties prop) throws Exception {
		if (b.getAutodata()==null) return b.getData();
		if (sqlConnector==null) throw new Exception("Sql connector must be provided when autodata feature is used!");
		
		LegalAutodata lad = b.getAutodata();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = sqlConnector.prepareStatement("SELECT defaultValue FROM autodata_table WHERE keyName = ? AND keyValue = ? AND name = ? ");
			for (LegalBlockData d : b.getData()) {
				if (d==null) break;
				
				pstmt.setString(1, lad.getKey());
				pstmt.setString(2, prop.getProperty(lad.getKey()));
				pstmt.setString(3, d.getName());
				
				rs = pstmt.executeQuery();
				if (rs.next()) d.setDefaultValue(rs.getString("defaultValue"));
				
				if (lad.getKey().equals(d.getName())) d.setAutodata(Boolean.TRUE);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (pstmt!=null) try { pstmt.close(); } catch (Exception e) {}
			if (rs!=null) try { rs.close(); } catch (Exception e) {}
		}
		
		return b.getData();
	}
	
	public List<LegalBlockData> restoreData(String key, String keyValue, String user) throws Exception {
		if (sqlConnector==null) throw new Exception("Sql connector must be provided when autodata feature is used!");
				
		List<LegalBlockData> result = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		/*try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(user + ".properties"));
		} catch (FileNotFoundException e) {
			return null;
		}*/
		
		try {
			pstmt = sqlConnector.prepareStatement("SELECT name, defaultValue FROM autodata_table WHERE keyName = ? AND keyValue = ? ");
			pstmt.setString(1, key);
			pstmt.setString(2, keyValue);
			rs = pstmt.executeQuery();
			
			result = new ArrayList<LegalBlockData>();
			while(rs.next()) {
				String name = rs.getString("name");
				String val = rs.getString("defaultValue");
				
				LegalBlockData d = new LegalBlockData();
				d.setName(name);
				d.setDefaultValue(val);
				
				result.add(d);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			if (pstmt != null) { try { pstmt.close(); } catch (Exception e) {} }
			if (rs != null) { try { rs.close(); } catch (Exception e) {} }
		}
	}

	public void setSqlConnector(Connection sqlConnector) {
		this.sqlConnector = sqlConnector;
	}

	public Connection getSqlConnector() {
		return sqlConnector;
	}

}
