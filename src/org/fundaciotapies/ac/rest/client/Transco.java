package org.fundaciotapies.ac.rest.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.rest.serializer.TranscoDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Transco {
	private static Logger log = Logger.getLogger(Transco.class);

	public String addTransco(TranscoEntity transco) {
		try {
		    // Construct data
		    String data = new Gson().toJson(transco);

		    // Send data
		    URL url = new URL(Constants.VIDEO_SERVICES_URL + "add");
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestProperty("Content-Type", "application/json");
		    conn.setRequestMethod("POST");
		    //conn.setRequestProperty("Accept", "application/json");
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    log.debug(data);
		    wr.write(data);
		    wr.flush();
		    wr.close();

		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String str;
		    StringBuffer sb = new StringBuffer();
		    while ((str = rd.readLine()) != null) {
		    	sb.append(str);
		    	sb.append("\n");
		    }

		    rd.close();
		    
		    return sb.toString();
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public String getStatusQueue() {
		try {
		    // Send data
		    URL url = new URL(Constants.VIDEO_SERVICES_URL + "status");
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
		    URL url = new URL(Constants.VIDEO_SERVICES_URL);
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
		    URL url = new URL(Constants.VIDEO_SERVICES_URL + id);
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestProperty("Content-Type", "application/json");
		    conn.setRequestMethod("GET");
		    //conn.setRequestProperty("Accept", "application/json");
		    
		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String str;
		    StringBuffer sb = new StringBuffer();
		    while ((str = rd.readLine()) != null) {
		    	sb.append(str);
		    	sb.append("\n");
		    }

		    rd.close();
		    
		    GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(TranscoEntity.class, new TranscoDeserializer());
		    TranscoEntity ent = gson.create().fromJson(sb.toString(), TranscoEntity.class);
		    return ent;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
}
