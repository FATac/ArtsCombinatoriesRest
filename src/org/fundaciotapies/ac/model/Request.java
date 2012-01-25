package org.fundaciotapies.ac.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.bo.Media;
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
import com.hp.hpl.jena.rdf.model.Property;
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
		
		if (!Cfg.USER_ROLE_SERVICE_AVAILABLE) return 1;
		
		try {
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
		    int i = 1;
		    for (String l : Cfg.USER_LEVEL) {
		    	if (l.contains(userRole)) return i;
		    	i++;
		    }
		    
		    responseStream.close();
		    
		} catch (Exception e) {
			Cfg.USER_ROLE_SERVICE_AVAILABLE = false;
			//log.warn("Error obtaining user role. Please make sure that USER_ROLE_SERVICE_URL is correct.");
		}
	    return 1;
	}
	
	public void setCurrentLanguage(String currentLanguage) {
		this.currentLanguage = currentLanguage;
	}
	
	public String getCurrentLanguage() {
		return currentLanguage;
	}
	
	/*
	 * Read cookies of request to get the current language of the client
	 */
	public String getCurrentLanguage(HttpServletRequest request) {
		String lang = Cfg.LANGUAGE_LIST[0];
		Cookie[] ckl = request.getCookies();
		if (ckl!=null) {
			for (Cookie k : ckl) {
				if (k.getName()=="Language") {
					lang = k.getValue();
					boolean valid = false;
					for (String l :Cfg.LANGUAGE_LIST) {
						if (l.equals(lang)) {
							valid = true;
							break;
						}
					}
					if (!valid) lang = Cfg.LANGUAGE_LIST[0];
				}
			}
		}
		return lang;
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
			int userLegalLevel = getUserLegalLevel(userId);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(userId)) {
				hideUrl = true;
			}
			
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
			Media media = new Media();
			media.load(id);
			String path = media.getPath();
			if (path==null) return null;
			String[] parts = path.split("\\.");
			return parts[parts.length-1].toLowerCase();
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public ObjectFile getMediaFile(String id, String profile, String uid) {
		try {
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			
			int userLegalLevel = getUserLegalLevel(uid);
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(uid)) {
				throw new Exception("Access to object ("+id+") denied due to legal restrictions");
			} else if ("master".equals(profile) && userLegalLevel < 4) {
				throw new Exception("Access to MASTER ("+id+") denied due to legal restrictions");
			}
			
			Media media = new Media();
			if (profile!=null && !"".equals(profile) && !"0".equals(profile)) media.load(id+"_"+profile); else media.load(id);
			if (media.getPath()==null) return null;
			String mediaPath = media.getPath();
			
			String ext = mediaPath.substring(mediaPath.length()-3);
			
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
			result.setInputStream(new FileInputStream(Cfg.MEDIA_PATH + mediaPath));
			result.setContentType(mime);
			
			return result;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}

	
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
				if (qs.get("type")!=null) propType += ","+qs.get("type").asResource().getLocalName();
				if (qs.get("range")!=null) {
					range += ","+Cfg.fromNamespaceToPrefix(qs.get("range").asResource().getNameSpace())+qs.get("range").asResource().getLocalName();
				}
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
	
	public Set<String> listClassPropertiesSimple2(String className) {
		Set<String> result = new TreeSet<String>();

		try {
			if (className == null) return null;
			
			String prefix = "";
			for(int i=0;i<Cfg.ONTOLOGY_NAMESPACES.length;i+=2) {
				prefix += " prefix " + Cfg.ONTOLOGY_NAMESPACES[i+1] + ": <" + Cfg.ONTOLOGY_NAMESPACES[i] + "> "; 
			}
			
			/*
			 * get all properties of a given class and its super classes
			 * also get properties that have no domain, that is- properties that can be used in any class
			 */
			String query = prefix +
				" select ?prop " +
				" where {" +
				"   { { ?prop rdf:type owl:ObjectProperty } " +
				"   union" +
				"   { ?prop rdf:type owl:DatatypeProperty } } " +
				"   . " +
				"   { ?prop rdfs:domain "+ className +" } " +
				"   union" +
				"   { ?prop rdf:type ?z . filter not exists { ?prop rdfs:domain ?x } }" +
				"   union " +
				"   { "+ className +" rdfs:subClassOf ?super" +
				"     . ?prop rdfs:domain ?super } " +
				" } " +
				" order by ?prop ";
			
			// Virtuoso reasoning does not work here!! We use Jena instead 
			//QueryExecution qe = VirtuosoQueryExecutionFactory.create(query, ModelUtil.getOntology());
			OntModel ont = ModelUtil.getOntology();
			QueryExecution qe = QueryExecutionFactory.create(query, ont);
			ResultSet rs = qe.execSelect();
			
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				String propName = Cfg.fromNamespaceToPrefix(qs.get("prop").asResource().getNameSpace()) +  qs.get("prop").asResource().getLocalName();
				result.add(propName);
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
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
			String filter = " FILTER (regex(?o,\""+word+"\",\"i\")";
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
			
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s ?p ?o " + qc + filter + " } ", model);
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

	/* not used */
	public void saveBackup2() {
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <"+Cfg.RESOURCE_URI_NS+"> WHERE { ?a ?b ?c } ", model.getRawModel());
			ResultSet rs = vqe.execSelect();
			
			// Get IDs that fit specific search
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				RDFNode na = r.get("a");
				RDFNode nb = r.get("b");
				RDFNode nc = r.get("c");
				
				Resource res = model.getResource(na.toString());
				if (res==null) res = model.createResource(na.toString());
				
				Property prop = model.getProperty(nb.toString());
				if (prop==null) prop = model.createProperty(nb.toString());
				
				RDFNode node = null;
				if (nc.isResource()) {
					node = model.getResource(nc.toString());
					if (node==null) node = model.createResource(nc.toString());
					res.addProperty(prop, node);
				} else if (nc.isLiteral()) {
					res.addProperty(prop, nc.asLiteral().getString(), nc.asLiteral().getLanguage());
				}
			}
			
			BufferedWriter buf = new BufferedWriter(new FileWriter("out.owl"));
			model.write(buf);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
	}
	
	/* not used */
	public void saveBackup() {
		
		InfModel model = ModelUtil.getModel();
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT DISTINCT ?s FROM <" + Cfg.RESOURCE_URI_NS + "> WHERE { ?s ?v ?p } ", model);
		ResultSet rs = vqe.execSelect();
		
		// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			String id = r.get("s").asResource().getLocalName();
			CustomMap map = getObject(id, "");
			
			List<String> propertiesList = new ArrayList<String>();
			List<String> propertiesValuesList = new ArrayList<String>();
			Set<Map.Entry<String, Object>> set = map.entrySet();
			for (Map.Entry<String, Object> ent : set) {
				String[] values = null;
				if (ent.getValue() instanceof String) {
					values = new String[]{ (String)ent.getValue() };
				} else {
					values = (String[])ent.getValue();
				}
				
				for (String v : values) {
					propertiesList.add(ent.getKey());
					propertiesValuesList.add(v);
				}
				
				String[] properties = new String[propertiesList.size()];
				String[] propertyValues = new String[propertiesValuesList.size()];
				
				propertiesList.toArray(properties);
				propertiesValuesList.toArray(propertyValues);
				
				new Upload().updateObject(id, properties, propertyValues);
			}
		}
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
	
	public List<String> listRecentChanges(String minutesago) throws Exception {
		long millisecondsago = new Long(minutesago)*60000;
		return ResourceStatistics.listRecentChanges(millisecondsago);
	}
	
	/*
	 * Resolves the actual property value of a given subject
	 * !!!!Removed functions, we use a single non-recursive function instead
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
	
	/*
	 * Resolves the actual value of a given path
	 */
	public String[] resolveModelPath(String path, String id, boolean includeId, boolean anyLang, boolean showLang) {
		if (path==null) return new String[]{};
		List<String> result = new ArrayList<String>();
		
		String idClause = " ?p0 ";
		if (id!=null) idClause = " <"+Cfg.RESOURCE_URI_NS+id+"> "; 
		
		/*
		 * Translate the path to a comprehensible sparql query
		 */
		String query = " where { ";
		String[] parts = path.split("=");
		int i = 0;
		for(String part : parts) {
			String[] atom = part.split("\\.");
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
		
		query = "select * " + query + " } ";
		
		//log.info("PATH: " + path);
		//log.info("QUERY: " + query);
		
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, ModelUtil.getModel());
		ResultSet rs = vqe.execSelect();
		while(rs.hasNext()) {
			QuerySolution s = rs.next();
			RDFNode node = s.get("p"+i);
			String thisId = id;
			
			if (i>1) {
				RDFNode IdNode = s.get("p"+(i-1));
				thisId = IdNode.asResource().getLocalName();
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
	
	public List<String> listMedia() throws Exception {
		return new Media().list();
	}
	
}
