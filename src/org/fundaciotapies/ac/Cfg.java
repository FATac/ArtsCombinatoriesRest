package org.fundaciotapies.ac;

import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Cfg {
	private final static Logger log = Logger.getLogger(Cfg.class);
	
	// Various
	public static Integer THUMBNAIL_WIDTH = 250;
	public static Integer THUMBNAIL_HEIGHT = 180;
	public static String[] VIDEO_FILE_EXTENSIONS = {"dv", "mpg", "avi"};
	public static String[] LANG_LIST = { "ca", "en", "es", "fr", "it", "de" };							// First language on the list is set as default
	
	// Services base URLs and connection strings
	public static String RDFDB_URL = "jdbc:virtuoso://localhost:1111";
	public static String RDFDB_USER = "dba";
	public static String RDFDB_PASS = "dba";
	public static String REST_URL = "http://stress.upc.es:8080/ArtsCombinatoriesRest/";
	public static String SOLR_URL = "http://localhost:8080/solr/";
	public static String VIDEO_SERVICES_URL = "http://tapies.aur.i2cat.net:8080/TapiesWebServices/rest/";
	
	// Ontology namespaces (After any change, all existing triples must be fixed)
	public static String ONTOLOGY_URI_NS = "http://localhost:8080/ArtsCombinatoriesRest/ontology/ac#";
	public static String ONTOLOGY_PREFIX = "ac";
	public static String RDF_URI_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String RDFS_URI_NS = "http://www.w3.org/2000/01/rdf-schema#";
	public static String RESOURCE_URI_NS = "http://localhost:8080/ArtsCombinatoriesRest/resource/";		
	public static String RESOURCE_PREFIX = "ac_res";
	
	// File system paths
	public static String CONFIGURATIONS_PATH = "/home/jordi.roig.prieto/workspace/ArtsCombinatoriesRest/json/";
	public static String SOLR_PATH = "/home/jordi.roig.prieto/solr/";
	public static String MEDIA_PATH = "./ac_media/";
	public static String ONTOLOGY_PATH = "./OntologiaArtsCombinatories.owl";
	
	static {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader("config.properties"));
		
			THUMBNAIL_WIDTH = Integer.parseInt(prop.get("THUMBNAIL_WIDTH")+"");
			THUMBNAIL_HEIGHT = Integer.parseInt(prop.get("THUMBNAIL_HEIGHT")+"");
			
			VIDEO_FILE_EXTENSIONS = (prop.get("VIDEO_FILE_EXTENSIONS")+"").split(",");
			for (int i=0;i<VIDEO_FILE_EXTENSIONS.length;i++) VIDEO_FILE_EXTENSIONS[i] = VIDEO_FILE_EXTENSIONS[i].trim();
			
			LANG_LIST = (prop.get("LANG_LIST")+"").split(",");
			for (int i=0;i<LANG_LIST.length;i++) LANG_LIST[i] = LANG_LIST[i].trim();
			
			RDFDB_URL = prop.get("RDFDB_URL")+"";
			RDFDB_USER = prop.get("RDFDB_USER")+"";
			RDFDB_PASS = prop.get("RDFDB_PASS")+"";
			REST_URL = prop.get("REST_URL")+"";
			SOLR_URL = prop.get("SOLR_URL")+"";
			VIDEO_SERVICES_URL = prop.get("VIDEO_SERVICES_URL")+"";
			
			ONTOLOGY_URI_NS = prop.get("ONTOLOGY_URI_NS")+"";
			ONTOLOGY_PREFIX = prop.get("ONTOLOGY_PREFIX")+"";
			RDF_URI_NS = prop.get("RDFS_URI_NS")+"";
			RESOURCE_URI_NS = prop.get("RESOURCE_URI_NS")+"";
			RESOURCE_PREFIX = prop.get("RESOURCE_PREFIX")+"";
			
			CONFIGURATIONS_PATH = prop.get("CONFIGURATIONS_PATH")+"";
			SOLR_PATH = prop.get("SOLR_PATH")+"";
			MEDIA_PATH = prop.get("MEDIA_PATH")+"";
			ONTOLOGY_PATH = prop.get("ONTOLOGY_PATH")+"";
		
		} catch (Exception e) {
			log.error("Error culd not load properties", e);
		}
	}
	
	
}