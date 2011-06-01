package org.fundaciotapies.ac.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.logic.support.LegalBlock;
import org.fundaciotapies.ac.logic.support.LegalBlockData;
import org.fundaciotapies.ac.logic.support.LegalBlockRules;
import org.fundaciotapies.ac.logic.support.LegalDefinition;


import com.google.gson.Gson;

public class LegalProcess {
	private static Logger log = Logger.getLogger(LegalProcess.class);
	
	public String startLegal() {
		String result = null;
		
		try {
			result = "user_" + Math.round(Math.random()*100000000);
			
			Properties prop = new Properties();
			prop.setProperty("___lastBlock", "");
			
			prop.store(new FileOutputStream(result + ".properties"), null);
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		
		return result;
	}
	
	// TODO: evaluate full-featured boolean expressions (it's currently working only for 'identifier = value' expressions)
	private Boolean evalExpression(String exp, Properties data) {
		
		String[] tokens = exp.split("[=<>!]");
		
		String operator = exp.replaceFirst(tokens[0], "").replaceFirst(tokens[1], "");
		
		tokens[0] = tokens[0].trim();
		tokens[1] = tokens[1].trim();
		
		String lvalue = "";
		
		if (tokens[0].matches("[a-zA-Z][a-zA-Z0-9]+")) 
			lvalue = data.getProperty(tokens[0]);
		else lvalue = null;
		
		if ("=".equals(operator))
			return lvalue.equals(tokens[1]);
		
		return false;
	}
	
	public List<LegalBlockData> nextBlockData(Map<String, String> data, String user) {
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(user + ".properties"));
			
			FileReader f = new FileReader(new File("legal.json"));
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
					LegalBlock b2 = def.getBlock(r.getResult());
					prop.setProperty("___lastBlock", b2.getName());
					prop.store(new FileOutputStream(user + ".properties"), null);
					return b2.getData();
				}
			}
			
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		
		return null;
	}
}
