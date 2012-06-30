package org.fundaciotapies.ac.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.bo.ResourceStatistics;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.model.support.ObjectFile;

import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class Request {
	private static Logger log = Logger.getLogger(Request.class);
	
	private String currentLanguage = Cfg.LANGUAGE_LIST[0];
	
	/*
	 * Converts prefix-based className to full qualified notation
	 */
	public String fromClassNameToURI(String className) {
		String classURI = null;
		if (className!=null) {
			String[] classNameParts = className.split(":");
			if (classNameParts.length>1)
				classURI = Cfg.fromPrefixToNamespace(classNameParts[0]) + classNameParts[1];
			else
				classURI = className;
		}
		
		return classURI;
	}
	
	/*
	 * Gets user legal level using an external service that allows accepts user id as parameter
	 * and returns user role or group name. 
	 * User level is determined by USER_LEVEL property of Configuration
	 */
	public int getUserLegalLevel(String userId) {
		
		if (userId==null) return 1;
		if (!Cfg.USER_ROLE_SERVICE_AVAILABLE) return 1;
		
		try {
			Integer result = 1;
			if ((result=Cfg.userLevelTmp.get(userId))!=null) {
				return result;
			}
			
			// Connect
			URL url = new URL(Cfg.USER_ROLE_SERVICE_URL + userId);
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestProperty("Content-Type", "plain/text");
		    conn.setRequestMethod("GET");
	
		    // Get the response
		    InputStream responseStream = conn.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(responseStream));
		    String str;
		    StringBuffer sb = new StringBuffer();
		    while ((str = rd.readLine()) != null) sb.append(str);
		    String userRole = sb.toString();
		    
		    // Group name position in USER_LEVEL array determines user level
		    // if it's not in the array, user level is 1
		    String[] userRoles = userRole.split(",");
		    for (int i=Cfg.USER_LEVEL.length-1;i>=0;i--) {
		    	String l = Cfg.USER_LEVEL[i];
		    	for (String r : userRoles) {
			    	if (l.contains(r)) {
			    		Cfg.userLevelTmp.put(userId, i+1);
			    		return i+1;
			    	}
		    	}
		    }
		    
		    
		    responseStream.close();
		    
		} catch (Exception e) {
			Cfg.USER_ROLE_SERVICE_AVAILABLE = false;
			log.warn("Error obtaining user role. Please make sure that USER_ROLE_SERVICE_URL is correct and restart Tomcat.");
		}
	    return 1;
	}
	
	public void setCurrentLanguage(String currentLanguage) {
		if (currentLanguage==null) currentLanguage = Cfg.LANGUAGE_LIST[0];
		this.currentLanguage = currentLanguage;
	}
	
	public String getCurrentLanguage() {
		return currentLanguage;
	}
	
	
	/*
	 * Gets all objects of an specific legal color
	 */
	public List<String> listObjectLegalColor(String color) {
		try {
			Integer rightLevel = null;
			if (color!=null) {
				if ("red".equals(color)) {
					rightLevel = 4;
				} else if ("orange".equals(color)) {
					rightLevel = 3;
				} else if ("yellow".equals(color)) {
					rightLevel = 2;
				} else if ("green".equals(color)) {
					rightLevel = 1;
				} else {
					rightLevel = 0;
				}
			}
			
			List<String> result = Right.list(rightLevel);
			for (int i=1;i<result.size();i+=2) {
				String sRightLevel = result.get(i);
				if ("4".equals(sRightLevel)) {
					result.set(i, "red");
				} else if ("3".equals(sRightLevel)) {
					result.set(i, "orange");
				} else if ("2".equals(sRightLevel)) {
					result.set(i, "yellow");
				} else if ("1".equals(sRightLevel)) {
					result.set(i, "green");
				} else if ("0".equals(sRightLevel)) {
					result.set(i, "none");
				}
			}
			
			return result;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	/*
	 * Gets object legal color as rgb code
	 */
	public String getObjectLegalColorRGB(String id) {
		
		try {
			Right right = new Right();
			right.load(id);
			Integer level = right.getRightLevel();
			
			if (level==null) return "#00ff00";
			switch (level) {
			case 1: return "#00ff00"; // green
			case 2: return "#ffff00"; // yellow
			case 3: return "#ffa500"; // orange
			case 4: return "#ff0000"; // red
			default: return "#dddddd";
			}
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public String getObjectLegalColorName(String id) {
		
		try {
			Right right = new Right();
			right.load(id);
			Integer level = right.getRightLevel();
			
			if (level==null) return null;
			
			switch (level) {
			case 1: return "green"; // green
			case 2: return "yellow"; // yellow
			case 3: return "orange"; // orange
			case 4: return "red"; // red
			default: return "gray";
			}
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}

	/*
	 * Get all object properties-values
	 */
	public CustomMap getObject(String id, String userId) {
		CustomMap result = null;

		try {
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			
			boolean hideUrl = false;
			/*int userLegalLevel = getUserLegalLevel(userId);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(userId)) {
				hideUrl = true;
			}*/
			
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			// Get object by Id
			QueryExecution qexec = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { <"+ Cfg.RESOURCE_URI_NS+id +"> ?prop ?val } ", model) ;
			ResultSet rs = qexec.execSelect();
			
			result = new CustomMap();
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				Resource prop = qs.get("prop").asResource();
				RDFNode val = qs.get("val");
				
				// URIs are converted to compact format
				String property = Cfg.fromNamespaceToPrefix(prop.getNameSpace())+prop.getLocalName();
				String value = null;
				if (val.isLiteral()) { 
					value = val.asLiteral().toString();
					// we hide URLs (assuming they link to medias) if user is not allowed to access them
					if (value.startsWith("http://") && hideUrl) continue;
				} else {
					value = Cfg.fromNamespaceToPrefix(val.asResource().getNameSpace())+val.asResource().getLocalName();
				}
				
				result.put(property, value);
			}
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public String getRealId(String cls, String id) {
		String result = null;

		try {
			String qc = "";
			if (cls!=null && !"".equals(cls)) {
				qc = " . { ?s rdf:type "+cls+" } ";
					
				List<String> subClassesList = listSubclasses(cls, false);
				for (String sc : subClassesList)
					qc += " UNION { ?s rdf:type "+sc+" } ";
			}
			
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			String query = "SELECT ?s FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { { ?s ac:FatacId \""+id+"\" } " + qc + " } ";
			QueryExecution qexec = VirtuosoQueryExecutionFactory.create(query, model) ;
			ResultSet rs = qexec.execSelect();
			
			if (rs.hasNext()) {
				QuerySolution qs = rs.next();
				result = qs.get("s").asResource().getLocalName();
			}
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	/*
	 * Get media file extension
	 */
	public String getObjectFileFormat(String id) {
		try {
			String[] parts = id.split("\\.");
			return parts[parts.length-1].toLowerCase();
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public ObjectFile getMediaFile(String id, String uid) {
		try {
			// Checks whether user can view object or not -- not used!
			/*Right right = new Right();
			right.load(id);
			
			int userLegalLevel = getUserLegalLevel(uid);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(uid)) {
				throw new Exception("Access to object ("+id+") denied due to legal restrictions");
			} else if (id.contains("_master.") && userLegalLevel < 4) {
				throw new Exception("Access to MASTER ("+id+") denied due to legal restrictions");
			}*/
			
			String ext = id.substring(id.length()-3);
			
			// Assign media type
			ext = ext.toLowerCase();
			String mime = "application/"+ext;
			if ("png,jpg,gif,svg,jpeg".contains(ext)) {
				mime = "image/"+ext;
			} else if ("ogv".equals(ext)) {
				mime = "video/ogg";
			} else if ("oga,ogg".contains(ext)) {
				mime = "audio/ogg";
			} else if ("tml".equals(ext)) {
				mime = "text/html";
			} else if ("txt".contains(ext)) {
				mime = "text/plain";
			} else if ("avi".equals(ext)) {
				mime = "video/x-msvideo";
			} else if ("mpeg,mpg".contains(ext)) {
				mime = "video/mpeg";
			}
			
			// Results back in a specific object containing file stream and mime type
			ObjectFile result = new ObjectFile();
			result.setInputStream(new FileInputStream(Cfg.MEDIA_PATH + id));
			result.setContentType(mime);
			
			return result;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}

	/*
	 * List all classes of current ontology
	 */
	public List<String> listOntologyClasses() {
		List<String> result = null;

		try {
			// Load ontology
			OntModel ont = ModelUtil.getOntology();
			
			Query sparql = QueryFactory.create(" select ?a where { ?a rdf:type owl:Class } "); 
		    QueryExecution qexec = VirtuosoQueryExecutionFactory.create(sparql, ont);
		    ResultSet rs = qexec.execSelect();
		    
		    result = new ArrayList<String>();
		    
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				RDFNode node = qs.get("a");
				result.add(Cfg.fromNamespaceToPrefix(node.asResource().getNameSpace())+node.asResource().getLocalName());
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	
	/*
	 * Checks whether a class is a subclass of another using Virtuosos reasoning
	 */
	public boolean isSubclass(String className, String superClassName) {

		try {
			if (className == null) return false;
			if (superClassName == null) return false;
			if (className.equals(superClassName)) return true;
			
			String prefix = "";
			for(int i=0;i<Cfg.ONTOLOGY_NAMESPACES.length;i+=2) {
				prefix += " prefix " + Cfg.ONTOLOGY_NAMESPACES[i+1] + ": <" + Cfg.ONTOLOGY_NAMESPACES[i] + "> "; 
			}
			
			String query = prefix +
				" select *" +
				" where { "+className+" rdfs:subClassOf "+superClassName+" } ";
			
			OntModel ont = ModelUtil.getOntology();
			QueryExecution qe = QueryExecutionFactory.create(query, ont);
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()) {
				return true;
			}
		} catch (Throwable e) {
			log.error("Error ", e);
			
		}
		
		return false;
	}
	
	
	/*
	 * Get the properties (and their attributes) that correspond to a given class
	 * Function used to generate a class form 
	 */
	public List<String[]> listClassProperties(String className) {
		List<String[]> result = new ArrayList<String[]>();

		try {
			if (className == null) return null;
			
			String prefix = "";
			for(int i=0;i<Cfg.ONTOLOGY_NAMESPACES.length;i+=2) {
				prefix += " prefix " + Cfg.ONTOLOGY_NAMESPACES[i+1] + ": <" + Cfg.ONTOLOGY_NAMESPACES[i] + "> "; 
			}
			
			/*
			 * get all properties and their ranges of a given class and its super classes
			 * also get properties that have no domain, that is- properties that can be used in any class
			 */
			String query = prefix +
				" select ?prop ?type ?range " +
				"		where " +
				"		{ " +
				"		  { ?prop rdf:type owl:ObjectProperty } " + 
				"		  union " +
				"		  { ?prop rdf:type owl:DatatypeProperty } " + 
				"		  union " +
				"		  { ?prop rdf:type rdf:Property } " + 
				"		  . " +
				"		  { ?prop rdfs:domain " + className +
				"		    . ?prop rdf:type ?type " +
				"		  } " +
				"		  union " +
				"		  { ?prop rdf:type ?type . filter not exists { ?prop rdfs:domain ?x } } " +
				"		  union " +
				"		  { "+ className +" rdfs:subClassOf ?super " +
				"		  . ?prop rdfs:domain ?super " +
				"		  . ?prop rdf:type ?type " +
				"		  } " +
				"		  . optional " + 
				"		  { ?prop rdfs:range ?range } " +
				"		} " +
				"		order by ?prop ";
			
			// Virtuoso reasoning does not work here!! We use Jena instead 
			//QueryExecution qe = VirtuosoQueryExecutionFactory.create(query, ModelUtil.getOntology());
			//QueryExecution qe = VirtuosoQueryExecutionFactory.create();
			OntModel ont = ModelUtil.getOntology();
			QueryExecution qe = QueryExecutionFactory.create(query, ont);
			ResultSet rs = qe.execSelect();
			String lastPropName = null;
			String range = "";
			String propType = "";
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				String propName = Cfg.fromNamespaceToPrefix(qs.get("prop").asResource().getNameSpace()) +  qs.get("prop").asResource().getLocalName();
				
				// group triple list by property name
				if (!propName.equals(lastPropName) && lastPropName != null) {
					// pack property
					result.add(new String[] { lastPropName , ((!"".equals(range))?range.substring(1):""),  propType } );
					range = "";
					propType = "";
				}
				
				// response includes property type and value range (coma separated)
				String currentType = qs.get("type").asResource().getLocalName();
				if (qs.get("range")!=null) {
					range += ","+Cfg.fromNamespaceToPrefix(qs.get("range").asResource().getNameSpace())+qs.get("range").asResource().getLocalName();
					if ("Property".equals(currentType)) {
						if (qs.get("range").toString().startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
							currentType = "DatatypeProperty";
						} else {
							currentType = "ObjectProperty";
						}
					}
				}
				if (qs.get("type")!=null) propType += ","+currentType;
				lastPropName = propName;
			}
			
			// pack last property found
			if (lastPropName != null) {
				result.add(new String[] { lastPropName , ((!"".equals(range))?range.substring(1):"") , propType } );
				range = "";
			}
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	/*
	 * Get the properties that correspond to a given class
	 */
	public Set<String> listClassPropertiesSimple(String className) {
		Set<String> result = new TreeSet<String>();

		try {
			if (className == null) return null;
			
			OntModel ont = ModelUtil.getOntology();
			int idx = className.indexOf(':');
			String ns = Cfg.fromPrefixToNamespace(className.substring(0, idx));
			String uri = ns + className.substring(idx+1);
			ExtendedIterator<OntProperty> it = ont.getOntClass(uri).listDeclaredProperties();
			
			while(it.hasNext()) {
				OntProperty p = it.next();
				result.add(Cfg.fromNamespaceToPrefix(p.getNameSpace()) + p.getLocalName());
			}
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	// List all or only direct subclasses of given class
	public List<String> listSubclasses(String className, Boolean direct) {
		List<String> result = new ArrayList<String>();
		String classURI = fromClassNameToURI(className);
		
		try {
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelUtil.getOntology());
			
			ExtendedIterator<OntClass> it = null;
			if (className != null) {
				OntClass ontClass = ont.getOntClass(classURI);
				it = ontClass.listSubClasses(direct);
			} else {
				it = ont.listHierarchyRootClasses();
				if (!it.hasNext()) {
					ont = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM, ModelUtil.getOntology());
					it = ont.listHierarchyRootClasses();
				}
			}
			
			// Use recursive calls to navigate through the class tree starting form given root class
			while (it.hasNext()) {
				OntClass cls = it.next();
				result.add(Cfg.fromNamespaceToPrefix(cls.getNameSpace())+cls.getLocalName());
			}	
			
		} catch (Throwable e) {
			System.out.println(classURI);
			log.error("Error ", e);
		}

		return result; 
	}
	
	/*public String getLegalObjectId(String referredObjectId) {
		String result = null;
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT ?s FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s <"+Cfg.ONTOLOGY_URI_NS+"isAssignedTo> <"+Cfg.RESOURCE_URI_NS+referredObjectId+"> . { ?s <"+Cfg.RDF_URI_NS+"type> <"+Cfg.ONTOLOGY_URI_NS+"Rights> } } ORDER BY ?s ", model);
			ResultSet rs = vqe.execSelect();
			
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				result = r.get("s").asResource().getLocalName();
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;	
	}*/
	
	/*
	 * List all object of a given class name 
	 */
	public Map<String, CustomMap> listObjects(String className) {
		Map<String, CustomMap> result = new TreeMap<String, CustomMap>();
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			String qc = null;
			
			// If specified, filter results for given class name and for all its subclasses 
			if (className!=null && !"".equals(className) && !"_".equals(className)) {
				String[] clsl = className.split(",");
				
				for(String cls : clsl) {
					if (cls!=null && !"".equals(cls)) {
						if (qc==null) {
							qc = " . { ?s rdf:type "+cls+" } "; 
						} else {
							qc += " UNION { ?s rdf:type "+cls+" } ";
						}
						
						List<String> subClassesList = listSubclasses(cls, false);
						for (String sc : subClassesList)
							qc += " UNION { ?s rdf:type "+sc+" } ";
					}
				}
			}

			if (qc==null) qc = "";

			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s ?p ?o " + qc + " } ", model);
			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			String lastId = null;
			CustomMap currentObject = null;
			
			// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				currentId = r.get("s").asResource().getLocalName();
				if (!currentId.equals(lastId)) {
					if ((lastId != null) && currentObject.size()>0) result.put(lastId, currentObject);
					currentObject = new CustomMap();
				}
				
				lastId = currentId;
				String propertyName = Cfg.fromNamespaceToPrefix(r.get("p").asResource().getNameSpace()) +  r.get("p").asResource().getLocalName();
				
				if (r.get("o").isResource()) {
					currentObject.put(propertyName, Cfg.fromNamespaceToPrefix(r.get("o").asResource().getNameSpace()) + r.get("o").asResource().getLocalName());
				} else {
					currentObject.put(propertyName, r.get("o").asLiteral().getString());
				}
			}

			result.put(lastId, currentObject);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}
	
	/*
	 * List all object ids of a given class name 
	 */
	public List<String> listObjectsId(String className) {
		List<String> result = new ArrayList<String>();
		
		QueryExecution vqe = null;
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			String qc = null;
			
			// If specified, filter results for given class name and for all its subclasses 
			if (className!=null && !"".equals(className) && !"_".equals(className)) {
				String[] clsl = className.split(",");
				
				for(String cls : clsl) {
					if (cls!=null && !"".equals(cls)) {
						if (qc==null) {
							qc = " { ?s rdf:type "+cls+" } "; 
						} else {
							qc += " UNION { ?s rdf:type "+cls+" } ";
						}
						
						List<String> subClassesList = listSubclasses(cls, false);
						for (String sc : subClassesList)
							qc += " UNION { ?s rdf:type "+sc+" } ";
					}
				}
			}

			if (qc==null) qc = "";

			// Create search query
			vqe = VirtuosoQueryExecutionFactory.create("SELECT ?s FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s rdf:type "+className+" } ", model);
			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			
			// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				currentId = r.get("s").asResource().getLocalName();
				result.add(currentId);
			}

		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}
	
	/* Full model search */
	public Map<String, CustomMap> search(String word, String className, String color, String userId) {
		if ("".equals(color)) color = null;
		Map<String, CustomMap> result = new TreeMap<String, CustomMap>();
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			String qc = null;
			
			if (qc==null) qc = "";

			// Create search query
			
			String filter = "";
			String[] words = word.split("\\s");
			if (word.startsWith("\"")) words = new String[]{ word.replaceAll("\"", "") };
			for(int  i=0; i<words.length; i++) {
				if (i>0) filter += " && ";
				filter += "(regex(?o,\""+words[i]+"\",\"i\") || regex(?s,\""+words[i]+"\",\"i\")) ";
			}
			
			filter = " FILTER (" + filter;
			
			for (String ns : Cfg.ONTOLOGY_NAMESPACES) if (ns.length()>10) filter += " && !regex(?o, \""+ns+"\",\"i\") ";
			filter += ")";
			
			if (className!=null && !"".equals(className)) {
				String[] classNameList = className.split(",");
				filter += " . ";
				for (String c : classNameList) {
					filter += " { ?s rdf:type " + c + " } UNION ";  
				}
				
				filter = filter.substring(0, filter.length()-6);
			}
			
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s ?p ?o " + qc + filter + " } LIMIT 5000 ", model);

			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			String lastId = null;
			CustomMap currentObject = null;
			
			// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				currentId = r.get("s").asResource().getLocalName();
				if (color!=null && !color.equals(getObjectLegalColorName(currentId))) continue;
				
				if (!currentId.equals(lastId)) {
					if (lastId != null && currentObject.size()>0) result.put(lastId, currentObject);
					currentObject = new CustomMap();
				}
				
				lastId = currentId;
				
				String propertyName = Cfg.fromNamespaceToPrefix(r.get("p").asResource().getNameSpace()) + r.get("p").asResource().getLocalName();
				if (r.get("o").isResource()) {
					currentObject.put(propertyName, Cfg.fromNamespaceToPrefix(r.get("o").asResource().getNameSpace()) + r.get("o").asResource().getLocalName());
				} else {
					currentObject.put(propertyName, r.get("o").asLiteral().getString());
				}
			}

			if (currentObject != null && currentObject.size()>0) result.put(currentId, currentObject);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}

	/* Get the object what has a specified field-value property and is of a given type */
	public List<String> specificObjectSearch(String field, String value, String className) {
		List<String> idList = new ArrayList<String>();
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			String classClause = "";
			if (className!=null) classClause =  " . ?s rdf:type <"+className+"> ";
			
			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s "+field+" ?o  FILTER regex(?o,\""+value+"\",\"i\") "+classClause+" } ORDER BY ?s ", model);
			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			
			// Get IDs that fit specific search
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				currentId = r.get("s").asResource().getLocalName();
				idList.add(currentId);
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return idList; 
	}
	
	
	public String getObjectClass(String id) {
		// Connect to rdf server
		InfModel model = ModelUtil.getModel();
		
		// Create search query
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { <"+Cfg.RESOURCE_URI_NS+id+"> rdf:type ?c } ", model);
		ResultSet rs = vqe.execSelect();
		
		if (rs.hasNext()) {
			QuerySolution r = rs.next();
			return Cfg.fromNamespaceToPrefix(r.get("c").asResource().getNameSpace()) + r.get("c").asResource().getLocalName();
		} else return null;
	}
	
	public String getObjectClassSimple(String id) {
		// Connect to rdf server
		InfModel model = ModelUtil.getModel();
		
		// Create search query
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { <"+Cfg.RESOURCE_URI_NS+id+"> rdf:type ?c } ", model);
		ResultSet rs = vqe.execSelect();
		
		if (rs.hasNext()) {
			QuerySolution r = rs.next();
			return r.get("c").asResource().getLocalName();
		} else return null;
	}
	
	public String getObjectClassName(String id) {
		// Connect to rdf server
		InfModel model = ModelUtil.getModel();
		
		// Create search query
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { <"+Cfg.RESOURCE_URI_NS+id+"> rdf:type ?c } ", model);
		ResultSet rs = vqe.execSelect();
		
		if (rs.hasNext()) {
			QuerySolution r = rs.next();
			return r.get("c").asResource().getLocalName();
		} else return null;
	}
	
	// List all superclasses of given class
	public List<String> listSuperClasses(String className) {
		List<String> result = new ArrayList<String>();
		List<String> tmp = new ArrayList<String>();
		
		OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelUtil.getOntology());

		String classURI = fromClassNameToURI(className);
		OntClass ontClass = ont.getOntClass(classURI);
		
		if (ontClass!=null) {
			ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
			
			while(it.hasNext())	{
				OntClass cls = it.next();
				String superclassURI = Cfg.fromNamespaceToPrefix(cls.getNameSpace())+cls.getLocalName();
				result.add(superclassURI);
				tmp.add(superclassURI);
			}
			
			for(String c : tmp) result.addAll(listSuperClasses(c));
		}
			
		return result;
	}
	
	// List all superclasses names (without prefix) of given class
	public List<String> listSuperClassesName(String className) {
		List<String> result = new ArrayList<String>();
		List<String> tmp = new ArrayList<String>();
		
		OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelUtil.getOntology());

		String classURI = fromClassNameToURI(className);
		OntClass ontClass = ont.getOntClass(classURI);
		
		if (ontClass!=null) {
			ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
			
			while(it.hasNext())	{
				OntClass cls = it.next();
				String superclassURI = Cfg.fromNamespaceToPrefix(cls.getNameSpace())+cls.getLocalName();
				result.add(cls.getLocalName());
				tmp.add(superclassURI);
			}
			
			for(String c : tmp) result.addAll(listSuperClassesName(c));
		}
			
		return result;
	}
	
	// Get all objects modified or inserted in the last specified minutes
	public List<String> listRecentChanges(String minutesago) throws Exception {
		List<String> allChangedIds = listObjectsId("ac:Case-File");
		
		long millisecondsago = new Long(minutesago)*60000;
		allChangedIds.addAll(ResourceStatistics.listRecentChanges(millisecondsago));
		return allChangedIds;
		//return ResourceStatistics.listRecentChanges(millisecondsago);
	}
	
	/*
	 * Resolves the actual property value of a given subject
	 * !!!!Removed functions, we use a single non-recursive function instead (see below)
	 */
	/*private String[] resolveModelPathPart(String className, String property, String id, boolean includeId, boolean anyLanguage, boolean showLang) {
		if ("class".equals(property)) return new String[]{ getObjectClassName(id) }; // 'class' is reserved word
		if ("superclass".equals(property)) { // 'superclass' is reserved word
			List<String> sup = listSuperClassesName(getObjectClass(id));
			String[] supList = new String[sup.size()];
			sup.toArray(supList);
			return supList;
		}
		
		// 'id' is a reserved word
		// instead of a property value return the actual id of the subject
		if ("id".equals(property)) return new String[]{ id };  
		
		List<String> result = new ArrayList<String>();
		
		// Connect to rdf server
		InfModel model = ModelUtil.getModel();
		
		// construct the proper query
		String classClause = "";
		String idClause = " ?a ";
		String propertyClause = " "+property+" ";
		// current subject
		if (id!=null) idClause = " <"+Cfg.RESOURCE_URI_NS+id+"> "; 
		// if classname is given discard results that does not match it's object class or superclasses
		if (className!=null && !"*".equals(className)) classClause = ". "+ idClause +" rdf:type " + className + " ";
		
		// Create search query
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { " + idClause + propertyClause + " ?c "+ classClause +" } order by ?c ", model);
		ResultSet rs = vqe.execSelect();
		while(rs.hasNext()) {
			QuerySolution s = rs.next();
			RDFNode node = s.get("c");
			
			if (node.isLiteral())  {
				String lang = node.asLiteral().getLanguage();
				// if we require a specific language results discard all other languages
				// get current language from class variable "currentLanguage" that has been previously set
				if (!anyLanguage && lang!=null && !"".equals(lang) && !lang.equals(getCurrentLanguage())) continue;
				// if language code is required to be shown, add LANG prefix which is later used by Solr to filter results by language
				result.add((showLang && lang!=null && !"".equals(lang)?"LANG"+lang+"__":"") + node.asLiteral().getString() + (includeId?"@"+id:""));
			} else {
				// its a resource so return the identifier
				result.add(node.asResource().getLocalName());
			}
		}
		
		String[] res = new String[result.size()];
		result.toArray(res);
		return res;
	}

	public String[] resolveModelPath2(String path, String id, boolean includeId, boolean anyLang, boolean showLang) {
		if (path==null) return new String[]{};
		
		int idx = path.indexOf("=");
		String part = path;
		if (idx!=-1) part = path.substring(0, idx);
		
		String[] atoms = part.split("\\.");
		String[] values = resolveModelPathPart(atoms[0], atoms[1], id, includeId, anyLang, showLang);
		
		if (idx!=-1) {
			List<String> valueList = new ArrayList<String>();
			for (String v : values) 
				valueList.addAll(Arrays.asList(resolveModelPath(path.substring(idx+1), v, includeId, anyLang, showLang)));
			
			values = new String[valueList.size()];
			valueList.toArray(values);
		} 
		
		return values;
	}*/
	
	public String getObjectFromMedia(String mediaFileName) {
		String result = "";
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			String qc = null;
			
			if (qc==null) qc = "";
			
			mediaFileName = Cfg.MEDIA_URL + mediaFileName;
			
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s ?p \""+mediaFileName+"\" } LIMIT 5000 ", model);

			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			
			// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				currentId = r.get("s").asResource().getLocalName();
				
				result = "," + currentId; 
			}

		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}
	
	/*
	 * Resolves the actual value of a given path
	 */
	public String[] resolveModelPath(String path, String id, boolean includeId, boolean anyLang, boolean showLang, boolean distinct) {
		//System.out.println(path);
		if (path==null) return new String[]{};
		List<String> result = new ArrayList<String>();
		
		String idClause = " ?p0 ";
		if (id!=null) idClause = " <"+Cfg.RESOURCE_URI_NS+id+"> "; 
		
		/*
		 * Translate the path to sparql query
		 */
		String query = " WHERE { ";
		String[] parts = path.split(Cfg.PATH_OBJECT_REFERENCE_PREFIX);
		int i = 0;
		for(String part : parts) {
			String[] atom = part.split(Cfg.PATH_PROPERTY_PREFIX);
			if (i==0) {
				if ("id".equals(atom[1])) {		// "id" is a reserved word
					return new String[]{ id }; // return the actual Id of the object
				} else {
					if ("class".equals(atom[1]) || "superclass".equals(atom[1])) { // 'class' and 'superclass' are reserved words
						query += idClause + " rdf:type ?p"+(++i)+" ";			// return the object types 
						break;
					} else {
						query += idClause + atom[1] + " ?p"+(++i)+" ";
					}
				}
				if (!atom[0].equals("*") && id==null) query += ". ?p0 " + " rdf:type " + atom[0];
			} else {
				if (!atom[0].equals("*")) query += ". ?p"+i + " rdf:type " + atom[0]; // related object must be of specified type ("*" means "any type")
				if ("id".equals(atom[1])) {
					break;
				} else {
					if ("class".equals(atom[1]) || "superclass".equals(atom[1])) {
						query += " . ?p"+i + " rdf:type ?p"+(++i)+" ";
						break;
					/*} else if ("superclass".equals(atom[1])) {
						query += " . ?p"+i + " rdf:type " + " ?t ";
						query += " . ?t  rdfs:subClassOf ?p"+(++i)+" ";
						break;*/
					} else {
						query += " . ?p"+i + " " + atom[1] + " ?p"+(++i)+" ";
					}
				}
			}
		}
		
		//if (!distinct || i<2) //TODO: fix distinct in query
			query = "SELECT * " + query + " } ";
		//else 
		//	query = "SELECT DISTINCT ?p"+(i-1)+" ?p"+i+" " + query + " } ";
		
		//System.out.println("PATH: " + path);
		//System.out.println("QUERY: " + query);
		
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, ModelUtil.getModel());
		ResultSet rs = vqe.execSelect();
		
		List<String> distinctList = new ArrayList<String>();
		
		while(rs.hasNext()) {
			QuerySolution s = rs.next();
			RDFNode node = s.get("p"+i);
			String thisId = id;
			
			if (i>1) {
				RDFNode IdNode = s.get("p"+(i-1));
				thisId = IdNode.asResource().getLocalName();
			}
			
			// TODO: fix distinct in query 
			//       -- this is not a good practice but we were forced to do it since "distinct" queries does not seem to work trough Jena or Virt jdbc
			if (distinct && i>=2) {
				String concat = node.toString()+thisId;
				if (distinctList.contains(concat)) continue;
				distinctList.add(concat);
			}
			
			if (node.isLiteral())  {
				String lang = node.asLiteral().getLanguage();
				// if we require a specific language results discard all other languages
				// get current language from class variable "currentLanguage" that has been previously set
				if (!anyLang && lang!=null && !"".equals(lang) && !lang.equals(getCurrentLanguage())) continue;
				// if language code is required to be shown, add LANG prefix which is later used by Solr to filter results by language
				result.add((showLang && lang!=null && !"".equals(lang)?"LANG"+lang+"__":"") + node.asLiteral().getString() + (includeId?"@"+thisId:""));
			} else {
				// its a resource so return the identifier
				result.add(node.asResource().getLocalName());
			}
		}
		
		String[] res = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	String tmpSearch = null;
	Integer tmpPag = null;
	Integer tmpCount = -1;
	
	/* Lists all files located in MEDIA_PATH (excluding folders) and filters by search text */
	public List<String> listMedia(String search, String pag) throws Exception {
		List<String> result = new ArrayList<String>();
		File f = new File(Cfg.MEDIA_PATH);
		tmpSearch = search;
		tmpPag = null;
		if (pag!=null) tmpPag = new Integer(pag);
		tmpCount = -1;
		
		File[] dirList = f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory() && !pathname.getName().equals("tmp") && !pathname.getName().equals("thumbnails")) {
					return true;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		FileFilter fileFilter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile()) {
					if (tmpSearch != null) {
						if (!pathname.getName().toLowerCase().contains(tmpSearch.toLowerCase())) return false;
					}
				} else return false;
				
				tmpCount++;
				if (tmpPag != null) {
					if (tmpCount<tmpPag*25) return false;
					if (tmpCount>=((tmpPag+1)*25)) return false;
				}

				return true;
			}
		};
		
		for (File dir : dirList) {
			File[] list = dir.listFiles(fileFilter);
			for (File current : list) {
				String mediaFileName = current.getPath().replaceAll(Cfg.MEDIA_PATH, "");
				result.add(mediaFileName+getObjectFromMedia(mediaFileName));
			}
		}
		
		File[] list = new File(Cfg.MEDIA_PATH).listFiles(fileFilter);
		for (File current : list) {
			String mediaFileName = current.getPath().replaceAll(Cfg.MEDIA_PATH, "");
			result.add(mediaFileName+getObjectFromMedia(mediaFileName));
		}
		
		return result;		
	}
	
}
