package org.fundaciotapies.ac.logic.solr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Mapping;

import com.google.gson.Gson;

public class SolrManager {
	private static Logger log = Logger.getLogger(SolrManager.class);
		
	public void generateSchema() throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(Constants.JSON_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		fin.close();
		
		StringBuffer sb = new StringBuffer();
		sb.append(" <fields> \n");
		sb.append(" 	<field name=\"id\" type=\"string\" indexed=\"true\" stored=\"true\" required=\"true\" /> \n");
		sb.append(" 	<field name=\"objectType\" type=\"string\" indexed=\"true\" stored=\"true\" required=\"true\" /> \n");
		
		for(DataMapping m : mapping.getData()) {
			String type = "text_general";
			if ("date.year".equals(m.getType())) type = "int";								// TODO: add more types
			
			sb.append("		<field name=\""+m.getName()+"\" type=\""+type+"\" indexed=\"true\" stored=\"true\" multiValued=\"true\" /> \n");
		}
		
		sb.append(" </fields> \n\n\n ");
		
		fin = new BufferedReader(new FileReader(Constants.SOLR_PATH + "conf/schema.xml-EMPTY"));
		
		StringBuffer sb2 = new StringBuffer();
		String str = null;
		while ((str = fin.readLine()) != null) sb2.append(str);
		
		int idx = sb2.indexOf("<!-- FIELDS_INSERTION_MARK -->") + 31;
		if (idx!=-1) sb2.insert(idx, sb);
		
		PrintWriter fout = new PrintWriter(Constants.SOLR_PATH + "conf/schema.xml");
		fout.print(sb2.toString());
		fout.close();
	}
	
	public String indexate() throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(Constants.JSON_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		fin.close();
		
		Request request = new Request();
		Set<String> objectTypesIndexed = new TreeSet<String>();
		
		String xml = "<add>\n";
		
		for(DataMapping m : mapping.getData()) {
			if (m.getPath()!=null) {
				for (String path : m.getPath()) {
					String className = path.split("\\.")[0].trim();
					objectTypesIndexed.add(className);
				}
			}
		}
		
		for(String className : objectTypesIndexed) {
			List<String> list = request.listObjectsId(className);
			
			for(String id : list) {
				xml += "	<doc>\n";
				xml += "		<field name='id'>"+id+"</field>\n";
				xml += "		<field name='objectType'>"+className+"</field>\n";
				
				for(DataMapping m : mapping.getData()) {
					if (m.getPath()!=null) {
						for (String path : m.getPath()) {
							if (className.equals(path.split("\\.")[0].trim())) {
								String[] result = request.resolveModelPath(path, id);
								for (String r : result) {
									xml += "		<field name='"+m.getName()+"'><![CDATA["+r+"]]></field>\n";
								}
							}
						}
					}
				}
				
				xml += "	</doc>\n";
			}
		}
		
		xml += "</add>";
		
		try {
			PrintWriter fout = new PrintWriter(Constants.SOLR_PATH + "data/data.xml");
			fout.print(xml);
			fout.close();
		} catch (Exception e) {
			System.out.println("Error desant data.xml");
			e.printStackTrace();
		}
		
		// Connect
		URL url = new URL("http://localhost:8080/solr/update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Send data
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(xml);
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

	    log.info("Indexation Solr Response " + sb.toString());
	    rd.close();
	    
	    return xml;
	}
	
	public void commit() throws Exception {
		// Connect
		URL url = new URL("http://localhost:8080/solr/update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Send data
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write("<commit/>");
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

	    log.info(sb.toString());
	    rd.close();
	}
	
	public void deleteAll() throws Exception {
		// Connect
		URL url = new URL("http://localhost:8080/solr/update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Send data
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write("<delete><query>*:*</query></delete>");
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

	    log.info(sb.toString());
	    rd.close();
	}

}