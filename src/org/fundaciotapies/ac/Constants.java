package org.fundaciotapies.ac;


public class Constants {
	public static final String RDFDB_URL = "jdbc:virtuoso://localhost:1111";
	
	public static final String FILE_DIR = "";
	public static final String[] VIDEO_FILE_EXTENSIONS = {"dv", "mpg", "avi"};
	
	public static final String OWL_URI_NS = "http://www.fundaciotapies.org/ontologies/2011/4/ac.owl#";
	public static final String RDFS_URI_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String baseURI = "http://www.artscombinatories.cat/objects/";		// This cannot be changed without updating all existing objects IDs
	
	public static final String[] userRoles = { "Contributor" , "Editor" , "Member" , "Reader", "Reviewer", "Site Administrator", "Manager" };

}