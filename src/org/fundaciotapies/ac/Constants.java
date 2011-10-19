package org.fundaciotapies.ac;


public class Constants {
	public static final String RDFDB_URL = "jdbc:virtuoso://localhost:1111";
	public static final String REST_URL = "http://stress.upc.es:8080/ArtsCombinatoriesRest/";
	
	public static final String FILE_DIR = "./ac_media/";
	public static final String[] VIDEO_FILE_EXTENSIONS = {"dv", "mpg", "avi"};
	
	public static final String AC_URI_NS = "http://www.fundaciotapies.org/ontologies/2011/4/ac.owl#";
	//public static final String RDF_URI_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS_URI_NS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String RESOURCE_BASE_URI = "http://www.artscombinatories.cat/resource/";		// This cannot be changed without updating all existing objects IDs
	
	public static final String[] LANG_LIST = { "ca", "en", "es", "fr", "it", "de" };
	
	public static final String[] userRoles = { "Contributor" , "Editor" , "Member" , "Reader", "Reviewer", "Site Administrator", "Manager" };
	
	public static final String JSON_PATH = "/home/jordi.roig.prieto/workspace/ArtsCombinatoriesRest/json/";
	public static final String SOLR_PATH = "/home/jordi.roig.prieto/solr/";
	

}