package org.fundaciotapies.ac;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class Cfg {
	private final static Logger log = Logger.getLogger(Cfg.class);
	
	// Various
	public static Integer THUMBNAIL_WIDTH = 250;
	public static Integer THUMBNAIL_HEIGHT = 180;
	public static String[] MEDIA_CONVERSION_PROFILES = {"avi mov", "mp3 aif"};
	public static String[] MEDIA_PROFILES_DESCRIPTION = {"320p", "80bps"};
	public static Boolean MEDIA_AUTOCONVERT = false;
	public static String DATE_FORMAT = "dd-MM-yyyy";
	public static String YEAR_FORMAT = "yyyy";
	public static String MONTH_FORMAT = "MM";
	public static String DAY_FORMAT = "dd";
	
	public static String[] LANGUAGE_LIST = { "ca", "en", "es", "fr", "it", "de" };							// First language on the list is set as default
	public static String[] USER_LEVEL = { "*", "Member", "Manager+Reviewer", "Site Administrator" };	// From level 1 to level 4 of authorization level
	
	// Services base URLs and connection strings
	public static String RDFDB_URL = "jdbc:virtuoso://localhost:1111";
	public static String RDFDB_USER = "dba";
	public static String RDFDB_PASS = "dba";
	public static String MEDIA_URL = "http://ec2-50-17-94-196.compute-1.amazonaws.com:8080/ArtsCombinatoriesRest/media/";
	public static String SOLR_URL = "http://localhost:8080/solr/";
	public static String USER_ROLE_SERVICE_URL = "http://localhost:8084/fatac/@@RetornaUserGroups?uid=";
	public static String VIDEO_SERVICES_URL = "http://localhost:8080/TapiesWebServices/rest/";
	
	// Ontology namespaces (After any change, all existing triples must be fixed)
	public static String RESOURCE_URI_NS = "http://localhost:8080/ArtsCombinatoriesRest/resource/";		
	public static String RESOURCE_PREFIX = "ac_res";
	
	public static String[] ONTOLOGY_NAMESPACES = {
		"http://localhost:8080/ArtsCombinatoriesRest/ontology/ac#", "ac",
		"http://dublincore.org/2010/10/11/dcterms.rdf#", "dcterms",
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf",
		"http://www.w3.org/2000/01/rdf-schema#", "rdfs",
		"http://www.w3.org/2002/07/owl#", "owl"
	};
	
	// File system paths
	public static String CONFIGURATIONS_PATH = "/home/ubuntu/artscombinatories/config/";
	public static String SOLR_PATH = "/home/ubuntu/artscombinatories/solr/";
	public static String MEDIA_PATH = "/home/ubuntu/artscombinatories/media/";
	public static String ONTOLOGY_PATH = "/home/ubuntu/artscombinatories/ac.owl";
	public static String OAI_PATH = "/home/ubuntu/artscombinatories/oai/";
	
	// Optional advanced defaults
	public static String PATH_PROPERTY_PREFIX = "\\.";
	public static String PATH_OBJECT_REFERENCE_PREFIX = "=";
	
	
	public static String fromNamespaceToPrefix(String namespace) {
		if ("http://www.w3.org/2001/XMLSchema#".equals(namespace) || namespace == null) return "";
		for(int i=0;i<ONTOLOGY_NAMESPACES.length;i+=2) {
			if (ONTOLOGY_NAMESPACES[i].equals(namespace)) return ONTOLOGY_NAMESPACES[i+1] + ":"; 
		}
		
		if (!RESOURCE_URI_NS.equals(namespace))
			log.warn("Could not convert namespace '"+namespace+"' to prefix! Check that variable ONTOLOGY_NAMESPACES is correct");
		
		return "";
	}
	
	public static String fromPrefixToNamespace(String prefix) {
		if (prefix.endsWith(":")) prefix = prefix.substring(0, prefix.length()-1);
		for(int i=0;i<ONTOLOGY_NAMESPACES.length;i+=2) {
			if (ONTOLOGY_NAMESPACES[i+1].equals(prefix)) return ONTOLOGY_NAMESPACES[i]; 
		}
		
		if (!RESOURCE_PREFIX.equals(prefix))
			log.warn("Could not convert prefix '"+prefix+"' to namespace! Check that variable ONTOLOGY_NAMESPACES is correct");
		
		return "";
	}
	
	public static String[] getRdfDatabaseHostPort() {
		String[] parts = RDFDB_URL.split(":");
		String port = parts[parts.length-1];
		String server = parts[parts.length-2].split("//")[1];
		return new String[]{ server, port };
	}
	
	public static boolean USER_ROLE_SERVICE_AVAILABLE = true;
	public static Map<String,Integer> userLevelTmp = new HashMap<String, Integer>(); 
	public static Set<String> objectClassThumbnail = new TreeSet<String>(); 
	
	
	static {
		try {
			log.info("Default Charset: " + Charset.defaultCharset().displayName());
			
			String canonicalPath = new File(".").getCanonicalPath();
			log.info(">>>>>>>>>>>>>>>> LOADING CONFIGURATION <<<<<<<<<<<<<<< (current dir: " + canonicalPath + ")");
			
			File f = new File("config.json");
			if (!f.exists()) throw new Exception("Could not find config.json in current path: " + canonicalPath);
			
			ObjectMapper m = new ObjectMapper();
			JsonNode jsonConfig = m.readValue(new File("config.json"), JsonNode.class);
			
			THUMBNAIL_WIDTH = jsonConfig.path("THUMBNAIL_WIDTH").getIntValue();
			THUMBNAIL_HEIGHT = jsonConfig.path("THUMBNAIL_HEIGHT").getIntValue();
			
			Iterator<JsonNode> it = jsonConfig.path("MEDIA_CONVERSION_PROFILES").getElements();
			List<String> tmplist = new ArrayList<String>();
			while(it.hasNext()) tmplist.add(it.next().getTextValue());
			MEDIA_CONVERSION_PROFILES = tmplist.toArray(new String[0]);
			
			it = jsonConfig.path("MEDIA_PROFILES_DESCRIPTION").getElements();
			tmplist = new ArrayList<String>();
			while(it.hasNext()) tmplist.add(it.next().getTextValue());
			MEDIA_PROFILES_DESCRIPTION = tmplist.toArray(new String[0]);
			
			MEDIA_AUTOCONVERT = "true".equals(jsonConfig.path("MEDIA_AUTOCONVERT").getTextValue());
			
			it = jsonConfig.path("LANGUAGE_LIST").getElements();
			tmplist = new ArrayList<String>();
			while(it.hasNext()) tmplist.add(it.next().getTextValue());
			LANGUAGE_LIST = tmplist.toArray(new String[0]);
			
			it = jsonConfig.path("USER_LEVEL").getElements();
			tmplist = new ArrayList<String>();
			while(it.hasNext()) tmplist.add(it.next().getTextValue());
			USER_LEVEL = tmplist.toArray(new String[0]);

			RDFDB_URL = jsonConfig.path("RDFDB_URL").getTextValue();
			RDFDB_USER = jsonConfig.path("RDFDB_USER").getTextValue();
			RDFDB_PASS = jsonConfig.path("RDFDB_PASS").getTextValue();
			MEDIA_URL = jsonConfig.path("MEDIA_URL").getTextValue();
			SOLR_URL = jsonConfig.path("SOLR_URL").getTextValue();
			VIDEO_SERVICES_URL = jsonConfig.path("VIDEO_SERVICES_URL").getTextValue();
			USER_ROLE_SERVICE_URL = jsonConfig.path("USER_ROLE_SERVICE_URL").getTextValue();
			
			RESOURCE_URI_NS = jsonConfig.path("RESOURCE_URI_NS").getTextValue();
			RESOURCE_PREFIX = jsonConfig.path("RESOURCE_PREFIX").getTextValue();
			
			it = jsonConfig.path("ONTOLOGY_NAMESPACES").getElements();
			tmplist = new ArrayList<String>();
			while(it.hasNext()) tmplist.add(it.next().getTextValue());
			ONTOLOGY_NAMESPACES = tmplist.toArray(new String[0]);
			
			CONFIGURATIONS_PATH = jsonConfig.path("CONFIGURATIONS_PATH").getTextValue();
			if (jsonConfig.path("SOLR_PATH").getTextValue()!=null) SOLR_PATH = jsonConfig.path("SOLR_PATH").getTextValue(); else SOLR_PATH = null;
			MEDIA_PATH = jsonConfig.path("MEDIA_PATH").getTextValue();
			ONTOLOGY_PATH = jsonConfig.path("ONTOLOGY_PATH").getTextValue();
			if (jsonConfig.path("OAI_PATH").getTextValue()!=null) OAI_PATH = jsonConfig.path("OAI_PATH").getTextValue(); else OAI_PATH = null;
			
			if (jsonConfig.path("PATH_PROPERTY_PREFIX").getTextValue()!=null) PATH_PROPERTY_PREFIX = jsonConfig.path("PATH_PROPERTY_PREFIX").getTextValue();
			if (jsonConfig.path("PATH_OBJECT_REFERENCE_PREFIX").getTextValue()!=null) PATH_OBJECT_REFERENCE_PREFIX = jsonConfig.path("PATH_OBJECT_REFERENCE_PREFIX").getTextValue();
		
			if (jsonConfig.path("DATE_FORMAT").getTextValue()!=null) DATE_FORMAT = jsonConfig.path("DATE_FORMAT").getTextValue();
			if (jsonConfig.path("YEAR_FORMAT").getTextValue()!=null) YEAR_FORMAT = jsonConfig.path("YEAR_FORMAT").getTextValue();
			if (jsonConfig.path("MONTH_FORMAT").getTextValue()!=null) MONTH_FORMAT = jsonConfig.path("MONTH_FORMAT").getTextValue();
			if (jsonConfig.path("DAY_FORMAT").getTextValue()!=null) DAY_FORMAT = jsonConfig.path("DAY_FORMAT").getTextValue();
			
		} catch (Exception e) {
			log.error("Error culd not load properties", e);
		}
	}
	
	
}