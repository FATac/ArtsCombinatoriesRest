package org.fundaciotapies.ac.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.logic.support.LegalBlock;
import org.fundaciotapies.ac.logic.support.LegalBlockData;
import org.fundaciotapies.ac.logic.support.LegalBlockRules;
import org.fundaciotapies.ac.logic.support.LegalDefinition;
import org.fundaciotapies.ac.logic.support.LegalExpressionCompiler;
import org.fundaciotapies.ac.model.bo.Right;


import com.google.gson.Gson;

public class LegalProcess {
	private static Logger log = Logger.getLogger(LegalProcess.class);
	
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
	
	public String abortLegal(String user) {
		new File(user + ".properties").delete();
		return null;
	}
	
	
	// TODO: fix expression evaluation since it is not working at all
	private Boolean evalExpression(String exp, Properties data) throws Exception {
		LegalExpressionCompiler compiler = new LegalExpressionCompiler();
		compiler.setData(data);
		return (Boolean)compiler.eval(exp);		
	}
	
	public List<LegalBlockData> nextBlockData(Map<String, String> data, String user) {
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(user + ".properties"));
			
			FileReader f = new FileReader(new File("/home/jordi.roig.prieto/workspace/ArtsCombinatoriesRest/json/prova.json"));
			LegalDefinition def = new Gson().fromJson(f, LegalDefinition.class);
			f.close();
			
			for(Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();it.hasNext();) {
				Map.Entry<String, String> d = it.next();
				
				if (d.getValue()!=null)
					prop.setProperty(d.getKey(), d.getValue());
			}
			
			String lastBlock = prop.getProperty("___lastBlock");
			
			if ("".equals(lastBlock) || lastBlock == null) {
				LegalBlock b = def.getBlock(def.getStartBlock());
				prop.setProperty("___lastBlock", b.getName());
				prop.store(new FileOutputStream(user + ".properties"), null);
				return b.getData();
			}
			
			LegalBlock b = def.getBlock(lastBlock);
			for (LegalBlockRules r : b.getRules()) {
				if (evalExpression(r.getExp(), prop)) {
					if (r.getResult().getBlock()!=null) {
						LegalBlock b2 = def.getBlock(r.getResult().getBlock());
						prop.setProperty("___lastBlock", b2.getName());
						prop.store(new FileOutputStream(user + ".properties"), null);
						return b2.getData();
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
		}
		
		return null;
	}
}
