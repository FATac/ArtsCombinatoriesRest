package org.fundaciotapies.ac.logic.solr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
		// TODO: set deafaultSearchField in Schema.xml
		BufferedReader fin = new BufferedReader(new FileReader(Constants.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		fin.close();
		
		StringBuffer sb = new StringBuffer();
		sb.append(" <fields> \n");
		sb.append(" 	<field name=\"id\" type=\"identifier\" indexed=\"true\" stored=\"true\" required=\"true\" /> \n");
		
		for(DataMapping m : mapping.getData()) {
			String type = "string";
			if ("date.year".equals(m.getType())) type = "int";								// TODO: add more types
			if ("text".equals(m.getType())) type = "text_general";
			
			sb.append("		<field name=\""+m.getName()+"\" type=\""+type+"\" indexed=\"true\" stored=\"true\" multiValued=\"true\" /> \n");
		}
		
		sb.append(" </fields> \n\n\n ");
		fin = new BufferedReader(new FileReader(Constants.SOLR_PATH + "conf/schema.xml-EMPTY"));
		
		StringBuffer sb2 = new StringBuffer();
		String str = null;
		while ((str = fin.readLine()) != null) sb2.append(str+"\n");
		
		int idx = sb2.indexOf("<!-- FIELDS_INSERTION_MARK -->") + 31;
		if (idx!=-1) sb2.insert(idx, sb);
		
		FileWriter fout = new FileWriter(Constants.SOLR_PATH + "conf/schema.xml");
		fout.write(sb2.toString());
		fout.close();
	}
	
	public String createDocumentEntry(String id, String className, Mapping mapping) {
		String xml = "	<doc>\n";
		xml += "		<field name='id'>"+id+"</field>\n";
		
		for(DataMapping m : mapping.getData()) {
			Boolean isMultilingual = "yes".equals(m.getMultilingual());
			if (m.getPath()!=null) {
				for (String path : m.getPath()) {
					String currentClassName = path.split("\\.")[0].trim();
					
					if (className.equals(currentClassName) || "*".equals(currentClassName)) {
						String[] result = new Request().resolveModelPath(path, id, false, true, isMultilingual);
						for (String r : result) {
							xml += "		<field name='"+m.getName()+"'><![CDATA["+r+"]]></field>\n";
						}
					}
				}
			}
		}
		
		xml += "	</doc>\n";
		
		return xml;
	}
	
	public void indexate() throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(Constants.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		fin.close();
		
		Request request = new Request();
		Set<String> objectTypesIndexed = new TreeSet<String>();
		
		String xml = "<add>\n";
		
		for(DataMapping m : mapping.getData()) {
			if (m.getPath()!=null) {
				for (String path : m.getPath()) {
					String className = path.split("\\.")[0].trim();
					if (!"*".equals(className) && !objectTypesIndexed.contains(className))	objectTypesIndexed.add(className);
				}
			}
		}
		
		for(String className : objectTypesIndexed) {
			List<String> list = request.listObjectsId(className);
			for(String id : list) 
				xml += createDocumentEntry(id, className, mapping);
		}
		
		xml += "</add>";
		
		try {
			PrintWriter fout = new PrintWriter(Constants.SOLR_PATH + "data/data.xml");
			fout.print(xml);
			fout.close();
		} catch (Exception e) {
			System.out.println("Error saving indexation data.xml");
			e.printStackTrace();
		}
		
		// Connect
		URL url = new URL(Constants.SOLR_URL + "update");
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
	}
	
	public void commit() throws Exception {
		// Connect
		URL url = new URL(Constants.SOLR_URL + "update");
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
		URL url = new URL(Constants.SOLR_URL + "update");
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
	
	public String search(String searchText, String start, String rows) throws Exception {
		String solrQuery = "?q="+URLEncoder.encode(searchText,"UTF-8")+"&fl=id&facet=true&wt=json";
		
		if (start!=null) solrQuery += "&start="+start;
		if (rows!=null) solrQuery += "&rows="+rows;
	
		BufferedReader fin = new BufferedReader(new FileReader(Constants.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		for (DataMapping m : mapping.getData()) {
			if ("yes".equals(m.getCategory())) {
				solrQuery += "&facet.field="+m.getName();
				if ("yes".equals(m.getMultilingual())) {
					String lang = new Request().getCurrentLanguage();
					if (lang==null || "".equals(lang)) lang = Constants.LANG_LIST[0];
					solrQuery += "&f."+m.getName()+".facet.prefix=LANG"+lang+"__";
				}
				if ("value".equals(m.getSort())) solrQuery += "&f."+m.getName()+".facet.sort=index";
			}
		}
		
		URL url = new URL(Constants.SOLR_URL + "select/" + solrQuery);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestMethod("GET");
	    
	    // Get the response
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String str;
	    StringBuffer sb = new StringBuffer();
	    while ((str = rd.readLine()) != null) {
	    	sb.append(str);
	    	sb.append("\n");
	    }
	    
	    return sb.toString();
	}

}