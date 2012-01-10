package org.fundaciotapies.ac.logic.solr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.bo.ResourceStatistics;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Mapping;
import org.fundaciotapies.ac.model.support.TemplateSection;

import com.google.gson.Gson;

public class SolrManager {
	private static Logger log = Logger.getLogger(SolrManager.class);
	
	public Map<String, CustomMap> documents = null;
	public List<String[]> statistics = null;
		
	/*
	 * Generates Solr schema.xml file mixing mapping.json specifications and schema.xml-EMPTY file
	 */
	public void generateSchema() throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		fin.close();
		
		// insert several fields by default
		StringBuffer sb = new StringBuffer();
		sb.append(" <fields> \n");
		sb.append(" <!-- SYSTEM FIELDS --> \n");
		sb.append(" 	<field name=\"id\" type=\"identifier\" indexed=\"true\" stored=\"true\" required=\"true\" /> \n");
		sb.append(" 	<field name=\"class\" type=\"identifier\" indexed=\"true\" stored=\"true\" required=\"true\" /> \n");
		sb.append(" 	<field name=\"views\" type=\"long\" indexed=\"true\" stored=\"true\" required=\"false\" /> \n");
		sb.append(" 	<field name=\"lastView\" type=\"long\" indexed=\"true\" stored=\"true\" required=\"false\" /> \n");
		sb.append(" 	<field name=\"creation\" type=\"long\" indexed=\"true\" stored=\"true\" required=\"false\" /> \n");
		sb.append(" <!-- CUSTOMIZED FIELDS --> \n");
		
		// insert user defined fields at mapping.json, each data type determines solr field type
		for(DataMapping m : mapping.getData()) {
			String type = "string";
			if ("date.year".equals(m.getType())) type = "int";								// TODO: add more types
			if ("text".equals(m.getType())) type = "text_general";
			if ("yes".equals(m.getSort()))	// sort fields must be single-value to allow sorting
				sb.append("	<field name=\""+m.getName()+"\" type=\""+type+"\" indexed=\"true\" stored=\"true\" multiValued=\"false\" /> \n");
			else
				sb.append("	<field name=\""+m.getName()+"\" type=\""+type+"\" indexed=\"true\" stored=\"true\" multiValued=\"true\" /> \n");
		}
		
		sb.append(" </fields> \n\n\n ");
		
		// load schema.xml-EMPTY that contains field type definitions plus other stuff
		fin = new BufferedReader(new FileReader(Cfg.SOLR_PATH + "conf/schema.xml-EMPTY"));
		
		StringBuffer sb2 = new StringBuffer();
		String str = null;
		while ((str = fin.readLine()) != null) sb2.append(str+"\n");
		
		// uses a string mark to find out where to put field list
		int idx = sb2.indexOf("<!-- FIELDS_INSERTION_MARK -->") + 31;
		if (idx!=-1) sb2.insert(idx, sb);
		
		// save schema.xml overwriting any existing schema
		FileWriter fout = new FileWriter(Cfg.SOLR_PATH + "conf/schema.xml");
		fout.write(sb2.toString());
		fout.close();
	}
	
	
	/*
	 * Implements "date", "date.year", "date.month", "date.day" data types of mapping.json 
	 * Extract the desired part of date value, according to type 
	 */
	private String extractDatePart(String value, String type) {
		try {
			if (type.equals("date")) {
				SimpleDateFormat df = new SimpleDateFormat(Cfg.DATE_FORMAT);
				return df.parse(value).getTime()+"";
			} else {
				SimpleDateFormat df = new SimpleDateFormat(Cfg.DATE_FORMAT);
				try {
					Date d = df.parse(value);
					if (type.equals("date.month")) {
						return new SimpleDateFormat(Cfg.MONTH_FORMAT).format(d);
					} else if (type.equals("date.day")) {
						return new SimpleDateFormat(Cfg.DAY_FORMAT).format(d);
					} else if (type.equals("date.year")) {
						return new SimpleDateFormat(Cfg.YEAR_FORMAT).format(d);
					}
				} catch (ParseException e) {
					if (type.equals("date.month")) {
						new SimpleDateFormat(Cfg.MONTH_FORMAT).parse(value);
					} else if (type.equals("date.day")) {
						new SimpleDateFormat(Cfg.DAY_FORMAT).parse(value);
					} else if (type.equals("date.year")) {
						new SimpleDateFormat(Cfg.YEAR_FORMAT).parse(value);
					}
				}
			}	
		} catch (ParseException e) {
			log.warn("Value '"+value+"' not valid for data type: " + type + ". Please check format in Configuration.");
			return null;
		}
		
		return value;
	}
	
	/*
	 * Create a single object indexing
	 */
	private void createDocumentEntry(String id, String className, Mapping mapping) throws Exception {
		CustomMap doc = documents.get(id);
		if (doc==null) doc = new CustomMap();
		
		for(DataMapping m : mapping.getData()) {
			Boolean isMultilingual = "yes".equals(m.getMultilingual());
			
			// checks that there's no reserved word in mapping
			if ("id,class,views,lastView,creation".contains(m.getName())) throw new Exception(m.getName() + " is a reserved key word ");
			if (doc.get(m.getName())==null) {
				if (m.getPath()!=null) {
					for (String path : m.getPath()) {
						String currentClassName = path.split("\\.")[0].trim();
						
						// if current data path refers to this object class
						// or is *, which refers to all 
						if (className.equals(currentClassName) || "*".equals(currentClassName)) {
							// get path value/s and put it in document indexing info.
							String[] result = new Request().resolveModelPath(path, id, false, true, isMultilingual);
							for (String r : result) {
								if (m.getType().startsWith("date") && r!=null) r = extractDatePart(r, m.getType());
								if (r!=null) {
									doc.put(m.getName(), r);
									if ("yes".equals(m.getSort())) break; // if it is a sort field, we want no more than 1 value
								}
							}
						}
						
						// if it is a sort field, we want no more than 1 value 
						// (otherwise Solr would launch error) 
						if (doc.get(m.getName())!=null && "yes".equals(m.getSort())) {
							break;
						}
					}
				}
			}
		}
		
		// add statistics information using default fields: views, creation and lastView 
		if (statistics != null) {
			for (String[] stat : statistics) {
				if (id.equals(stat[0])) {
					doc.put("views", Long.parseLong(stat[1]));
					doc.put("creation", Long.parseLong(stat[2]));
					if (stat[3]!=null) doc.put("lastView", Long.parseLong(stat[3]));
					break;
				}
			}
		}
		
		// put single document in all documents group to be indexed 
		documents.put(id, doc);
	}
	
	/*
	 * Main indexing function
	 * loops through all indexing data fields defined in mapping.json, determines which class of objects are to be indexed
	 * creates document group and generates resulting xml, which is finally saved and posted to Solr service "update"
	 */
	public void indexate() throws Exception {
		documents = new HashMap<String, CustomMap>();
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		fin.close();
		
		Request request = new Request();
		Set<String> objectTypesIndexed = new TreeSet<String>();
		
		// Collects all classes referenced from root in mapping.json
		for(DataMapping m : mapping.getData()) {
			if (m.getPath()!=null) {
				for (String path : m.getPath()) {
					String className = path.split("\\.")[0].trim();
					if (!"*".equals(className) && !objectTypesIndexed.contains(className))	objectTypesIndexed.add(className);
				}
			}
		}
		
		// loads statistics that will be used to create every document indexes
		statistics = ResourceStatistics.list();
		for(String className : objectTypesIndexed) {
			// for each class eligible for indexing, get all its objects
			List<String> list = request.listObjectsId(className);
			// and generate its index info
			for(String id : list) createDocumentEntry(id, className, mapping);
		}
		
		// render xml index that will feed Solr
		String xml = "<add>\n";
		for(Map.Entry<String, CustomMap> ent1 : documents.entrySet()) {
			String id = ent1.getKey();
			CustomMap doc = ent1.getValue();
			xml += "	<doc>\n";
			xml += "		<field name='id'>" + id +"</field>\n";
			xml += "		<field name='class'>" + request.getObjectClassSimple(id) +"</field>\n";
			for(Map.Entry<String, Object> ent2 : doc.entrySet()) {
				String name = ent2.getKey();
				Object val = ent2.getValue();
				if (val instanceof String) {
					xml +=	"		<field name='"+name+"'><![CDATA["+val+"]]></field>\n";
				} else if (val instanceof String[]) {
					String[] vals = (String[])val;
					for (String v : vals) xml +=	"		<field name='"+name+"'><![CDATA["+v+"]]></field>\n";
				} else {
					xml +=	"		<field name='"+name+"'>"+val+"</field>\n";
				}
			}
			xml += "	</doc>\n";
		}
		xml += "</add>";
		
		// saving data.xml is for information purposes ony
		// so it is not critical if it fails 
		try {
			PrintWriter fout = new PrintWriter(Cfg.SOLR_PATH + "data.xml");
			fout.print(xml);
			fout.close();
		} catch (Exception e) {
			log.warn("Error saving indexation data.xml", e);
		}
		
		// Connect to Solr, service Update
		URL url = new URL(Cfg.SOLR_URL + "update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Feed hungry Solr with all index
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
	
	/*
	 * Initial purpose of commit function was to effectively commit after indexing, 
	 * but it seems unnecessary as Solr commits automatically (???)
	 */
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
	
	/*
	 * Clear all Solr index
	 */
	public void deleteAll() throws Exception {
		// Connect
		URL url = new URL(Cfg.SOLR_URL + "update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Send "everything" query
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
	
	private String getQueryFilter(List<String> filterValues, String lang) throws Exception {
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

	/*
	 * Performs a Solr search
	 */
	public String search(String searchText, String filter, String start, String rows, String lang, String searchConfig, String sort) throws Exception {
		if (searchText==null) searchText = "";
		String solrQuery1 = "";
		
		
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		List<String> searchValues = null;
		
		Mapping searchConfigurations = null;
		
		try {
			fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/search.json"));
			searchConfigurations = new Gson().fromJson(fin, Mapping.class);
		} catch (FileNotFoundException e) {	}
		
		// if solr configurations are specified
		if (searchConfigurations!=null) {
			if (searchConfig==null || "".equals(searchConfig)) searchConfig = "default";
			// get the current solr config and add its specifications to current search options
			for (DataMapping searchConf : searchConfigurations.getData()) {
				if (searchConf.getName().equals(searchConfig)) {
					searchValues = searchConf.getFilter();
					if (searchConf.getValue()!=null) {
						for (String val : searchConf.getValue()) {
							if (!"".equals(searchText))	searchText += " " + val;
							else searchText += val;
						}
					}
					if (searchConf.getSort()!=null) {
						if (sort!=null && !"".equals(sort)) sort = searchConf.getSort() + "," + sort;
						else sort = searchConf.getSort();
					}
					break;
				}
			}
		}
		
		if (searchValues==null) searchValues = new ArrayList<String>();
		
		if (filter!=null) {
			String tmp[] = filter.split(",");
			for (String f : tmp) searchValues.add(f.trim());
		}
		
		solrQuery1 += getQueryFilter(searchValues, lang);
		boolean hasFilter = !"".equals(solrQuery1);
		
		String solrQuery2 = "&fl=id&facet=true&wt=json";
		
		if (start!=null) solrQuery2 += "&start="+start;
		if (rows!=null) solrQuery2 += "&rows="+rows;

		boolean firstTime = true;
		for (DataMapping m : mapping.getData()) {
			// fields with "search" clause are meant to be used on search
			if ("yes".equals(m.getSearch()) && !"".equals(searchText)) {
				if (m.getType().contains("date.") && !searchText.matches("[0-9]+")) {
					// if it is a date type, it is converted to an int field
					// solr fails searching text in numeric fields so we avoid this case
					// do nothing
				} else {
					if (firstTime) {
						if (hasFilter) solrQuery1 += " AND ("; 
					} else {
						solrQuery1 += " OR ";
					}
					
					if ("yes".equals(m.getMultilingual()))
						solrQuery1 += m.getName() + ":LANG" + lang + "__" + searchText + " OR ";
						
					solrQuery1 += m.getName() + ":" + searchText;
					firstTime = false;
				}
			}
			
			// use solr facets to build categories of fields marked as so 
			if ("yes".equals(m.getCategory())) {
				solrQuery2 += "&facet.field="+m.getName();
				if ("yes".equals(m.getMultilingual())) solrQuery2 += "&f."+m.getName()+".facet.prefix=LANG"+lang+"__";		// if field is multilingual consider only current language  
				if ("yes".equals(m.getSortCategory())) solrQuery2 += "&f."+m.getName()+".facet.sort=index";
			}
		}
		
		if (hasFilter == true && !firstTime) solrQuery1 += ")";
		//solrQuery2 += "&facet.mincount=1";
		
		// in case having no search text and filtering, perform a global query
		if (solrQuery1 == null || "".equals(solrQuery1)) solrQuery1 = "*:*";
		URL url = new URL(Cfg.SOLR_URL + "select/?q=" + URLEncoder.encode(solrQuery1, "UTF-8") + (sort!=null?"&sort="+URLEncoder.encode(sort, "UTF-8"):"") + solrQuery2);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestMethod("GET");
	    
	    // Get solr search results
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String str;
	    StringBuffer sb = new StringBuffer();
	    while ((str = rd.readLine()) != null) {
	    	sb.append(str);
	    	sb.append("\n");
	    }
	    
	    
	    // remove language code prefix from results
	    return sb.toString().replaceAll("LANG"+lang+"__", "");
	}
	
	/*
	 * Performs Solr search to autocomplete taking advantage of facets prefix feature, 
	 * which categorizes only values that have the specified prefix
	 */
	public String autocomplete(String searchText, String start, String rows, String lang, String searchConfig) throws Exception {
		if (searchText==null || searchText.length()==0) return null;
		String solrQuery1 = "";
		
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		
		TemplateSection searchConfigurations = null;
		
		try {
			 fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/search.json"));
			searchConfigurations = new Gson().fromJson(fin, TemplateSection.class);
		} catch (FileNotFoundException e) {	}
		
		List<String> searchValues = null;
		
		if (searchConfig==null || "".equals(searchConfig)) searchConfig = "default";
		if (searchConfigurations!=null) {
			for (DataMapping searchConf : searchConfigurations.getData()) {
				if (searchConf.getName().equals(searchConfig)) {
					searchValues = searchConf.getFilter();
					if (searchConf.getValue()!=null) {
						for (String val : searchConf.getValue()) searchText += " " + val;
					}
					break;
				}
			}
		}
		
		if (searchValues==null) searchValues = new ArrayList<String>();
		solrQuery1 += getQueryFilter(searchValues, lang);
		
		String solrQuery2 = "&fl=id&facet=true&wt=json";
		if (start!=null) solrQuery2 += "&start="+start;
		solrQuery2 += "&rows=0";

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
		solrQuery1 = "*:*";
		
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