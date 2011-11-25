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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Mapping;
import org.fundaciotapies.ac.model.support.TemplateSection;

import com.google.gson.Gson;

public class SolrManager {
	private static Logger log = Logger.getLogger(SolrManager.class);
	
	public Map<String, CustomMap> documents = null;
		
	public void generateSchema() throws Exception {
		// TODO: set deafaultSearchField in Schema.xml
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
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
		fin = new BufferedReader(new FileReader(Cfg.SOLR_PATH + "conf/schema.xml-EMPTY"));
		
		StringBuffer sb2 = new StringBuffer();
		String str = null;
		while ((str = fin.readLine()) != null) sb2.append(str+"\n");
		
		int idx = sb2.indexOf("<!-- FIELDS_INSERTION_MARK -->") + 31;
		if (idx!=-1) sb2.insert(idx, sb);
		
		FileWriter fout = new FileWriter(Cfg.SOLR_PATH + "conf/schema.xml");
		fout.write(sb2.toString());
		fout.close();
	}
	
	public String createDocumentEntry_(String id, String className, Mapping mapping) {
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
							if (r!=null) xml += "		<field name='"+m.getName()+"'><![CDATA["+r+"]]></field>\n";
						}
					}
				}
			}
		}
		
		xml += "	</doc>\n";
		
		return xml;
	}
	
	public void createDocumentEntry(String id, String className, Mapping mapping) {
		CustomMap doc = documents.get(id);
		if (doc==null) doc = new CustomMap();
		
		for(DataMapping m : mapping.getData()) {
			Boolean isMultilingual = "yes".equals(m.getMultilingual());
			if (doc.get(m.getName())==null) {
				if (m.getPath()!=null) {
					for (String path : m.getPath()) {
						String currentClassName = path.split("\\.")[0].trim();
						
						if (className.equals(currentClassName) || "*".equals(currentClassName)) {
							String[] result = new Request().resolveModelPath(path, id, false, true, isMultilingual);
							for (String r : result) {
								if (r!=null) doc.put(m.getName(), r);
							}
						}
					}
				}
			}
		}
		
		documents.put(id, doc);
	}
	
	public void indexate() throws Exception {
		documents = new HashMap<String, CustomMap>();
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		fin.close();
		
		Request request = new Request();
		Set<String> objectTypesIndexed = new TreeSet<String>();
		
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
			for(String id : list) createDocumentEntry(id, className, mapping);
		}
		
		String xml = "<add>\n";
		for(Map.Entry<String, CustomMap> ent1 : documents.entrySet()) {
			String id = ent1.getKey();
			CustomMap doc = ent1.getValue();
			xml += "	<doc>\n";
			xml += "		<field name='id'>" + id +"</field>\n";
			for(Map.Entry<String, Object> ent2 : doc.entrySet()) {
				String name = ent2.getKey();
				Object val = ent2.getValue();
				if (val instanceof String) {
					xml +=	"		<field name='"+name+"'><![CDATA["+val+"]]></field>\n";
				} else if (val instanceof String[]) {
					String[] vals = (String[])val;
					for (String v : vals) xml +=	"		<field name='"+name+"'><![CDATA["+v+"]]></field>\n";
				}
			}
			xml += "	</doc>\n";
		}
		xml += "</add>";
		
		try {
			PrintWriter fout = new PrintWriter(Cfg.SOLR_PATH + "data/data.xml");
			fout.print(xml);
			fout.close();
		} catch (Exception e) {
			System.out.println("Error saving indexation data.xml");
			e.printStackTrace();
		}
		
		// Connect
		URL url = new URL(Cfg.SOLR_URL + "update");
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
		URL url = new URL(Cfg.SOLR_URL + "update");
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
		URL url = new URL(Cfg.SOLR_URL + "update");
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
	
	public String getQueryFilter(List<String> filterValues, String lang) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		String resp = "";
		Collections.sort(filterValues);

		String lastField = null;
		for (String f : filterValues) {
			for (DataMapping d : mapping.getData()) {
				String[] fp = f.split(":");
				if ("yes".equals(d.getMultilingual()))	{
					if (d.getName().equals(fp[0].trim())) f = fp[0] + ":\"LANG"+lang+"__" + fp[1]+"\"";
				} else {
					if (d.getName().equals(fp[0].trim())) f = fp[0] + ":\"" + fp[1] + "\"";
				}
				
			}
			
			if (lastField!=null && f.startsWith(lastField+":")) {
				resp += " OR "  + f;
			} else {
				if (lastField!=null) resp += ") AND (" + f; else resp += "(" + f;
				if (f.indexOf(":")>0)
					lastField = f.substring(0, f.indexOf(":"));
				else lastField = f;
			}
		}
		if (filterValues.size()>0)	resp += ")";
		
		return resp;
	}

	
	public String search(String searchText, String filter, String start, String rows, String lang, String searchConfig) throws Exception {
		if (searchText==null) searchText = "";
		String solrQuery1 = "";
		
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/search.json"));
		TemplateSection searchConfigurations = new Gson().fromJson(fin, TemplateSection.class);
		
		fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		List<String> searchValues = null;
		
		if (searchConfig==null || "".equals(searchConfig)) searchConfig = "default";
		for (DataMapping searchConf : searchConfigurations.getData()) {
			if (searchConf.getName().equals(searchConfig)) {
				searchValues = searchConf.getValue();
				break;
			}
		}
		
		if (searchValues==null) searchValues = new ArrayList<String>();
		
		if (filter!=null) {
			String tmp[] = filter.split(",");
			for (String f : tmp) searchValues.add(f.trim());
		}
		
		solrQuery1 += getQueryFilter(searchValues, lang);
		
		String solrQuery2 = "&fl=id&facet=true&wt=json";
		
		if (start!=null) solrQuery2 += "&start="+start;
		if (rows!=null) solrQuery2 += "&rows="+rows;

		boolean firstTime = true;
		for (DataMapping m : mapping.getData()) {
			if ("yes".equals(m.getSearch()) && !"".equals(searchText)) {
				if (!"".equals(solrQuery1)) {
					if (firstTime) solrQuery1 += " AND ("; else solrQuery1 += " OR ";
					firstTime = false;
				}
				if ("yes".equals(m.getMultilingual())) {  
					solrQuery1 += m.getName() + ":\"LANG" + lang + "__" + searchText+"\"";
				} else {
					solrQuery1 += m.getName() + ":\"" + searchText + "\"";
				}
			}
			if ("yes".equals(m.getCategory())) {	// TODO: Intersect with search configuration categories
				solrQuery2 += "&facet.field="+m.getName();
				if ("yes".equals(m.getMultilingual())) solrQuery2 += "&f."+m.getName()+".facet.prefix=LANG"+lang+"__";
				if ("value".equals(m.getSort())) solrQuery2 += "&f."+m.getName()+".facet.sort=index";
			}
		}
		
		if (firstTime == false) solrQuery1 += ")";
		solrQuery2 += "&facet.mincount=1";
		
		if (solrQuery1 == null || "".equals(solrQuery1)) solrQuery1 = "*:*";
		URL url = new URL(Cfg.SOLR_URL + "select/?q=" + URLEncoder.encode(solrQuery1, "UTF-8") + solrQuery2);
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
	    
	    return sb.toString().replaceAll("LANG"+lang+"__", "");
	}
	
	public String autocomplete(String searchText, String start, String rows, String lang, String searchConfig) throws Exception {
		if (searchText==null || searchText.length()==0) return null;
		String solrQuery1 = "";
		
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/search.json"));
		TemplateSection searchConfigurations = new Gson().fromJson(fin, TemplateSection.class);
		
		fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		List<String> searchValues = null;
		
		if (searchConfig==null || "".equals(searchConfig)) searchConfig = "default";
		for (DataMapping searchConf : searchConfigurations.getData()) {
			if (searchConf.getName().equals(searchConfig)) {
				searchValues = searchConf.getValue();
				break;
			}
		}
		
		if (searchValues==null) searchValues = new ArrayList<String>();
		solrQuery1 += getQueryFilter(searchValues, lang);
		
		String solrQuery2 = "&fl=id&facet=true&wt=json";
		if (start!=null) solrQuery2 += "&start="+start;
		if (rows!=null) solrQuery2 += "&rows="+rows;

		if (lang==null || "".equals(lang)) lang = Cfg.LANGUAGE_LIST[0];
		
		for (DataMapping m : mapping.getData()) {
			if ("yes".equals(m.getSearch()) && "yes".equals(m.getAutocomplete())) {
				solrQuery2 += "&facet.field="+m.getName();
				if ("yes".equals(m.getMultilingual())) {
					solrQuery2 += "&f."+m.getName()+".facet.prefix=LANG"+lang+"__"+searchText;
				} else {
					solrQuery2 += "&f."+m.getName()+".facet.prefix="+searchText;
				}
			}
		}
		
		solrQuery2 += "&facet.mincount=1";
		
		URL url = new URL(Cfg.SOLR_URL + "select/?q=" + URLEncoder.encode(solrQuery1, "UTF-8") + solrQuery2);
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
	    
	    return sb.toString().replaceAll("LANG"+lang+"__", "");
	}

}