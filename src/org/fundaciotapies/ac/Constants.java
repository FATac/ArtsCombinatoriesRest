package org.fundaciotapies.ac;


public class Constants {
	
	// Various
	public static final Integer THUMBNAIL_WIDTH = 250;
	public static final Integer THUMBNAIL_HEIGHT = 180;
	public static final String[] VIDEO_FILE_EXTENSIONS = {"dv", "mpg", "avi"};
	public static final String[] LANG_LIST = { "ca", "en", "es", "fr", "it", "de" };							// First language on the list is set as default
	public static final String[] userRoles = { "Contributor" , "Editor" , "Member" , "Reader", "Reviewer", "Site Administrator", "Manager" };
	
	// Services base URLs and connection strings
	public static final String RDFDB_URL = "jdbc:virtuoso://localhost:1111";
	public static final String RDFDB_USER = "dba";
	public static final String RDFDB_PASS = "dba";
	public static final String REST_URL = "http://stress.upc.es:8080/ArtsCombinatoriesRest/";
	public static final String SOLR_URL = "http://localhost:8080/solr/";
	public static final String VIDEO_SERVICES_URL = "http://tapies.aur.i2cat.net:8080/TapiesWebServices/rest/";
	
	// Ontology namespaces (After any change, all existing triples must be fixed)
	public static final String ONTOLOGY_URI_NS = "http://localhost:8080/ArtsCombinatoriesRest/ontology/ac#";
	public static final String ONTOLOGY_PREFIX = "ac";
	public static final String RDF_URI_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS_URI_NS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String RESOURCE_URI_NS = "http://localhost:8080/ArtsCombinatoriesRest/resource/";		
	public static final String RESOURCE_PREFIX = "ac_res";
	
	// File system paths
	public static final String CONFIGURATIONS_PATH = "/home/jordi.roig.prieto/workspace/ArtsCombinatoriesRest/json/";
	public static final String SOLR_PATH = "/home/jordi.roig.prieto/solr/";
	public static final String MEDIA_PATH = "./ac_media/";
	public static final String ONTOLOGY_PATH = "./OntologiaArtsCombinatories.owl";

}