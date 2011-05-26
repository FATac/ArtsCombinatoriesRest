package org.fundaciotapies.ac.rest.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class Transco {
	private static Logger log = Logger.getLogger(Transco.class);

	public String addTransco(TranscoEntity transco) {
		try {
		    // Construct data
		    String data = new Gson().toJson(transco);

		    // Send data
		    URL url = new URL("http://tapies.aur.i2cat.net:8080/TapiesWebServices/rest/add");
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();

		    // Get the response
		    char[] cbuf = null;
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    rd.read(cbuf);
		    wr.close();
		    rd.close();
		    
		    return new String(cbuf);
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public String getStatusQueue() {
		try {
		    // Send data
		    URL url = new URL("http://tapies.aur.i2cat.net:8080/TapiesWebServices/rest/status");
		    URLConnection conn = url.openConnection();
		    
		    // Get the response
		    char[] cbuf = null;
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    rd.read(cbuf);
		    rd.close();
		    
		    return new String(cbuf);
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<TranscoEntity> getAllTransco() {
		try {
			// Send data
		    URL url = new URL("http://tapies.aur.i2cat.net:8080/TapiesWebServices/rest/");
		    URLConnection conn = url.openConnection();
		    
		    // Get the response
		    char[] cbuf = null;
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    rd.read(cbuf);
		    rd.close();
		    
		    String result = new String(cbuf);
		    List<TranscoEntity> listTranscoEntities = new Gson().fromJson(result, List.class);
		    
		    return listTranscoEntities;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public TranscoEntity getTransco(String id) {
		try {
			// Send data
		    URL url = new URL("http://tapies.aur.i2cat.net:8080/TapiesWebServices/rest/"+id);
		    URLConnection conn = url.openConnection();
		    
		    // Get the response
		    char[] cbuf = null;
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    rd.read(cbuf);
		    rd.close();
		    
		    String result = new String(cbuf);
		    TranscoEntity ent = new Gson().fromJson(result, TranscoEntity.class);
		    
		    return ent;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
}
