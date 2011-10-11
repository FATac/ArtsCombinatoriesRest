package org.fundaciotapies.ac.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.model.bo.Media;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.model.bo.User;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.ObjectFile;
import org.fundaciotapies.ac.model.support.Template;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class Request {
	private static Logger log = Logger.getLogger(Request.class);
	
	public String getCurrentLanguage() {
		return "ca"; // TODO: get language from client user config/cookies
	}
	
	public Integer getRoleLevel(String roleName) {
		if (roleName==null) return 1;
		int roleLevel = 1;
		for (String r : Constants.userRoles) {
			if (roleName.equals(r)) return roleLevel;
			roleLevel++;
		}
		
		return 1;
	}
	
	public String getObjectLegalColor(String id) {
		
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
			default: return "#00ff00";
			}
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	private String extractUriId(String URI) {
		return URI.replace(Constants.OBJECT_BASE_URI, "").replace(Constants.RDFS_URI_NS, "").replace(Constants.AC_URI_NS, "");
	}
	
	public String getRdf() {
		// Connect to rdf server
		OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));

		// Get object in its RDF specification
		StringWriter sw = new StringWriter();
		data.write(sw);
		return sw.toString();
	}

	public CustomMap getObject(String id, String userId) {
		CustomMap result = null;

		try {
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			
			int userLegalLevel = 1;
			
			// userId = "" is internal code to allow full access, can only be used by internal calls!
			if (userId != null && !"".equals(userId)) {
				User user = new User();
				user.load(userId);
				userLegalLevel = getRoleLevel(user.getUserRole());
			}
			
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(userId)) {
				throw new Exception("Access to object denied due to legal restrictions");
			}
			
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			// Get object by Id
			Individual ind = data.getIndividual(Constants.OBJECT_BASE_URI+id);
			StmtIterator it = ind.listProperties();
			result = new CustomMap();
			while(it.hasNext()) {
				Statement stmt = it.next();
				if (stmt.getObject().isLiteral()) {
					String lang = stmt.getObject().asLiteral().getLanguage();
					if (lang!=null && !"".equals(lang) && !lang.equals(getCurrentLanguage())) continue;
					
					result.put(extractUriId(stmt.getPredicate().toString()), stmt.getObject().asLiteral().getString());
				} else {
					result.put(extractUriId(stmt.getPredicate().toString()), extractUriId(stmt.getObject().toString()));
				}
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
				qc = " . { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+cls+"> } ";
					
				List<String> subClassesList = listSubclasses(cls, false);
				for (String sc : subClassesList)
					qc += " UNION { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+sc+"> } ";
			}
			
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			String query = "SELECT ?s FROM <http://localhost:8890/ACData> WHERE { { ?s <"+Constants.AC_URI_NS+"FatacId> \""+id+"\" } " + qc + " } ";
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create(query),(VirtGraph) data.getBaseModel().getGraph());
			ResultSet rs = vqe.execSelect();
			
			if (rs.hasNext()) {
				QuerySolution qs = rs.next();
				result = extractUriId(qs.get("s").toString());
			}
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public ObjectFile getObjectFile(String id, String userId) {
		try {
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);

			int userLegalLevel = 1;
			if (userId != null && !"".equals(userId)) {
				User user = new User();
				user.load(userId);
				userLegalLevel = getRoleLevel(user.getUserRole());
			}
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(userId)) {
				throw new Exception("Access to object denied due to legal restrictions");
			}
			
			// Only media objects have associated files
			String className = (String)new Request().getObject(id, "").get("Class");
			boolean isMediaObject = new Request().listSubclasses("Media", false).contains(className);
			if (!isMediaObject) return null;
			
			Media media = new Media();
			media.load(id);
			if (media.getPath()==null) return null;
			
			String ext = media.getPath().substring(media.getPath().length()-3);
			
			// Assign media type
			ext = ext.toLowerCase();
			String mime = "application/"+ext;
			if ("png,jpg,gif,svg,jpeg".contains(ext)) {
				mime = "image/"+ext;
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
			result.setInputStream(new FileInputStream(media.getPath()));
			result.setContentType(mime);
			
			return result;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public List<String> listObjectClasses() {
		List<String> result = null;

		try {
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config 

			// Get all classes in the ontology
			ExtendedIterator<OntClass> classesIt = ont.listNamedClasses();
			result = new ArrayList<String>();
			while ( classesIt.hasNext() )
	        {
	            OntClass actual = classesIt.next();
	            String name = extractUriId(actual.toString());
	            if (name!=null) result.add(name);
	        }
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public List<String> listClassProperties(String className) {
		List<String> result = null;

		try {
			if (className == null) return null;

			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config
			
			Set<String> props = listClassPropertiesSimple(className);
			
			result = new ArrayList<String>();
			for(String p : props) {
				OntProperty prop = ont.getOntProperty(Constants.AC_URI_NS + p);
				ExtendedIterator<? extends OntResource> l = prop.listRange();
				String range = "";
				if (l!=null) while (l.hasNext()) range += extractUriId(l.next().toString())+",";
				
				// Creates string with property info: whether it's data or object property, functional, symmetric, etc.
				result.add(extractUriId(prop.toString()) + " " + ((!"".equals(range))?range:"_") + " " +
						(prop.isDatatypeProperty()?"D":"O") +
						(prop.isFunctionalProperty()?"F":"_") +
						(prop.isSymmetricProperty()?"S":"_") +
						(prop.isTransitiveProperty()?"T":"_") +
						(prop.hasInverse()?"I":"_"));
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
			String classURI = Constants.AC_URI_NS + className;
			
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config
			
			// Get all properties for given class
			Query q = QueryFactory.create("SELECT ?prop WHERE { ?prop <"+Constants.RDFS_URI_NS+"domain> <"+classURI+"> } ");
			QueryExecution qe = QueryExecutionFactory.create(q, ont);
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()) {
				QuerySolution s = rs.next();
				result.add(extractUriId(s.get("prop").asResource().getURI()));
			}

			// Also get properties for its superclasses (for they are inherited)
			OntClass ontClass = ont.getOntClass(classURI);
			if (ontClass!=null) {
				OntClass parentClass = ontClass.getSuperClass();
				if (parentClass!=null)	result.addAll(listClassPropertiesSimple(extractUriId(parentClass.toString())));
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public List<String> listSubclasses(String className, Boolean direct) {
		List<String> result = new ArrayList<String>();

		try {
			if (className == null) return null;
			String classURI = Constants.AC_URI_NS + className;
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config 

			OntClass ontClass = ont.getOntClass(classURI);
			
			// Use recursive calls to navigate through the class tree starting form given root class
			for (ExtendedIterator<OntClass> it = ontClass.listSubClasses(direct);it.hasNext();) {
				OntClass cls = it.next();
				result.add(cls.getNameSpace().substring(cls.getNameSpace().indexOf("#")+1)+extractUriId(cls.toString()));
			}	
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result; 
	}
	
	public String getLegalObjectId(String referredObjectId) {
		String result = null;
		
		try {
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			// Create search query
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT ?s FROM <http://localhost:8890/ACData> WHERE { ?s <"+Constants.AC_URI_NS+"isAssignedTo> <"+Constants.OBJECT_BASE_URI+referredObjectId+"> . { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+"Rights> } } ORDER BY ?s "), (VirtGraph) data.getBaseModel().getGraph());
			ResultSet rs = vqe.execSelect();
			
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				result = extractUriId(r.get("s").toString());
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;	
	}
	
	public Map<String, CustomMap> listObjects(String className) {
		Map<String, CustomMap> result = new TreeMap<String, CustomMap>();
		
		try {
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			String qc = null;
			
			// If specified, filter results for given class name and for all its subclasses 
			if (className!=null && !"".equals(className) && !"_".equals(className)) {
				String[] clsl = className.split(",");
				
				for(String cls : clsl) {
					if (cls!=null && !"".equals(cls)) {
						if (qc==null) {
							qc = " . { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+cls+"> } "; 
						} else {
							qc += " UNION { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+cls+"> } ";
						}
						
						List<String> subClassesList = listSubclasses(cls, false);
						for (String sc : subClassesList)
							qc += " UNION { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+sc+"> } ";
					}
				}
			}

			if (qc==null) qc = "";

			// Create search query
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?s ?p ?o " + qc + " } "),(VirtGraph) data.getBaseModel().getGraph());
			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			String lastId = null;
			CustomMap currentObject = null;
			
			// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				currentId = extractUriId(r.get("s").toString());
				if (!currentId.equals(lastId)) {
					if ((lastId != null) && currentObject.size()>0) result.put(lastId, currentObject);
					currentObject = new CustomMap();
				}
				
				lastId = currentId;
				
				if (r.get("o").isResource()) {
					currentObject.put(extractUriId(r.get("p").asResource().toString()), extractUriId(r.get("o").toString()));
				} else {
					currentObject.put(extractUriId(r.get("p").asResource().toString()), r.get("o").asLiteral().getString());
				}
			}

			result.put(lastId, currentObject);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}

	
	public Map<String, CustomMap> search(String word, String className, String userId) {
		Map<String, CustomMap> result = new TreeMap<String, CustomMap>();
		
		try {
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			String qc = null;
			
			// If specified, filter results for given class name and for all its subclasses 
			if (className!=null && !"".equals(className) && !"_".equals(className)) {
				String[] clsl = className.split(",");
				
				for(String cls : clsl) {
					if (cls!=null && !"".equals(cls)) {
						if (qc==null) {
							qc = " . { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+cls+"> } "; 
						} else {
							qc += " UNION { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+cls+"> } ";
						}
						
						List<String> subClassesList = listSubclasses(cls, false);
						for (String sc : subClassesList)
							qc += " UNION { ?s <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+sc+"> } ";
					}
				}
			}

			if (qc==null) qc = "";

			// Create search query
			String filter = "FILTER (regex(?o,\""+word+"\",\"i\") && !regex(?o, \""+Constants.OBJECT_BASE_URI+"\",\"i\") && !regex(?o, \""+Constants.AC_URI_NS+"\",\"i\")) ";
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?s ?p ?o " + qc + filter + " } "),(VirtGraph) data.getBaseModel().getGraph());
			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			String lastId = null;
			CustomMap currentObject = null;
			
			// Get user role level
			int userLegalLevel = 1;
			if (userId != null && !"".equals(userId)) {
				User user = new User();
				user.load(userId);
				userLegalLevel = getRoleLevel(user.getUserRole());
			}
			
			// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
			Right right = null;
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				currentId = extractUriId(r.get("s").toString());
				if (!currentId.equals(lastId)) {
					if (lastId != null && (right.getRightLevel()==null || right.getRightLevel() <= userLegalLevel)) {
						if (currentObject.size()>0) result.put(lastId, currentObject);
					}
					
					currentObject = new CustomMap();
					
					right = new Right();
					right.load(currentId);
				}
				
				lastId = currentId;
				
				if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel && !"".equals(userId)) continue;
				
				if (r.get("o").isResource()) {
					currentObject.put(extractUriId(r.get("p").asResource().toString()), extractUriId(r.get("o").toString()));
				} else {
					String lang = r.get("o").asLiteral().getLanguage();
					if (lang!=null && !"".equals(lang) && !lang.equals(getCurrentLanguage())) continue;
					
					currentObject.put(extractUriId(r.get("p").asResource().toString()), r.get("o").asLiteral().getString());
				}
			}

			if (lastId != null && (right.getRightLevel()==null || right.getRightLevel() <= userLegalLevel)) result.put(lastId, currentObject);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}

	public List<String> specificObjectSearch(String field, String value, String className) {
		List<String> idList = new ArrayList<String>();
		
		try {
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			// Create search query
			field = Constants.AC_URI_NS + field;
			className = Constants.AC_URI_NS + className;
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?s <"+field+"> ?o  FILTER regex(?o,\""+value+"\",\"i\") . ?s <"+Constants.RDFS_URI_NS+"Class> <"+className+"> } ORDER BY ?s "),(VirtGraph) data.getBaseModel().getGraph());
			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			
			// Get IDs that fit specific search
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				currentId = extractUriId(r.get("s").toString());
				idList.add(currentId);
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return idList; 
	}
	
	
	public List<String[]> query(String sparql) {
		List<String[]> result = new ArrayList<String[]>();
		
		// Connect to rdf server
		OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
		
		// Create search query
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create(sparql),(VirtGraph) data.getBaseModel().getGraph());
		ResultSet rs = vqe.execSelect();
		
		// Get IDs that fit specific search
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			
			RDFNode na = r.get("a");
			RDFNode nb = r.get("b");
			RDFNode nc = r.get("c");
			String a = "&lt;"+na.toString()+"&gt;";
			String b = "&lt;"+nb.toString()+"&gt;";
			String c = "";

			if (nc.isResource()) c = "&lt;"+nc.toString()+"&gt;"; else c = "\""+nc.toString()+"\"";
			
			String[] insert = {a,b,c};
			
			result.add(insert);
		}
		
		return result;
	}

	public List<String> saveBackup(String backupId) {
		List<String> backupScript = new ArrayList<String>();
		
		try {
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			// Create search query
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?a ?b ?c } "),(VirtGraph) data.getBaseModel().getGraph());
			ResultSet rs = vqe.execSelect();
			
			// Get IDs that fit specific search
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				
				RDFNode na = r.get("a");
				RDFNode nb = r.get("b");
				RDFNode nc = r.get("c");
				String a = "";
				String b = "";
				String c = "";
				if (na.isResource())	a = "<"+na.toString()+">"; else a = "\""+na.toString()+"\"";
				if (nb.isResource())	b = "<"+nb.toString()+">"; else b = "\""+nb.toString()+"\"";
				if (nc.isResource())	c = "<"+nc.toString()+">"; else c = "\""+nc.toString()+"\"";
				
				String insert = "INSERT INTO GRAPH <http://localhost:8890/ACData> { "+a+" "+b+" "+c+" }";
				
				backupScript.add(insert);
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return backupScript; 
	}
	
	public String getObjectClass(String id) {
		// Connect to rdf server
		OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
		
		// Create search query
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { <"+Constants.OBJECT_BASE_URI+id+"> <"+Constants.RDFS_URI_NS+"Class> ?c } "),(VirtGraph) data.getBaseModel().getGraph());
		ResultSet rs = vqe.execSelect();
		
		if (rs.hasNext()) {
			QuerySolution r = rs.next();
			return extractUriId(r.get("c").toString());
		} else return null;
	}
	
	public List<String> listSuperClasses(String className) {
		List<String> result = new ArrayList<String>();
		List<String> tmp = new ArrayList<String>();
		
		OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config
		OntClass ontClass = ont.getOntClass(Constants.AC_URI_NS + className);
		ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
		
		while(it.hasNext())	{
			OntClass cls = it.next();
			result.add(extractUriId(cls.toString()));
			tmp.add(extractUriId(cls.toString()));
		}
		
		for(String c : tmp) result.addAll(listSuperClasses(c));
			
		return result;
	}
	
	private String[] resolveModelPathPart(String className, String property, String id) {
		if ("class".equals(property)) return new String[]{ getObjectClass(id) }; // 'class' is reserved word
		List<String> result = new ArrayList<String>();
		
		// Connect to rdf server
		OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
		
		String classClause = "";
		String idClause = " ?a ";
		String propertyClause = " <"+Constants.AC_URI_NS+property+"> ";
		
		if (id!=null) {
			idClause = " <"+Constants.OBJECT_BASE_URI+id+"> ";
		} else if (className!=null && !"*".equals(className)) {
			classClause = ". ?a <"+Constants.RDFS_URI_NS+"Class> <"+Constants.AC_URI_NS+className+"> "; // TODO: We need reasoning to include subclasses
		}
		
		// Create search query
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { " + idClause + propertyClause + " ?c "+ classClause +" } "),(VirtGraph) data.getBaseModel().getGraph());
		ResultSet rs = vqe.execSelect();
		while(rs.hasNext()) {
			QuerySolution s = rs.next();
			if (s.get("c").isLiteral())
				result.add(s.get("c").toString());
			else
				result.add(extractUriId(s.get("c").toString()));
		}
		
		String[] res = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	public String[] resolveModelPath(String path, String id) {
		int idx = path.indexOf(":");
		String part = path;
		if (idx!=-1) part = path.substring(0, idx);
		
		String[] atoms = part.split("\\.");
		String[] values = resolveModelPathPart(atoms[0], atoms[1], id);
		
		if (idx!=-1) {
			List<String> valueList = new ArrayList<String>();
			for (String v : values) 
				valueList.addAll(Arrays.asList(resolveModelPath(path.substring(idx+1), v)));
			
			values = new String[valueList.size()];
			valueList.toArray(values);
		} 
		
		return values;
	}
	
	public Template getObjectView(String id) {
		Template template = null;
		
		try {
			String className = getObjectClass(id);
			File f = new File(Constants.JSON_PATH+"mapping/"+className+".json");
			
			if (!f.exists()) {
				List<String> superClasses = listSuperClasses(className);
				for (String superClassName : superClasses) {
					f = new File(Constants.JSON_PATH+"mapping/"+superClassName+".json");
					if (f.exists()) break;
				}
			}
			
			if (!f.exists()) { 
				log.warn("Trying to obtain view from no-template object class");
				return null;
			}
			
			template = new Gson().fromJson(new FileReader(f), Template.class);
			for (DataMapping dm : template.getHeader()) {
				String type = dm.getType();
				
				if ("text".equals(type)) {
					for (String path : dm.getPath()) {
						if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
						dm.getValue().addAll(Arrays.asList(resolveModelPath(path, id)));
					}
				}
				
				dm.setPath(null);
			}
			
			for (DataMapping dm : template.getBody()) {
				String type = dm.getType();
				
				if ("text".equals(type)) {
					for (String path : dm.getPath()) {
						if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
						dm.getValue().addAll(Arrays.asList(resolveModelPath(path, id)));
					}
				}
				
				dm.setPath(null);
			}
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return template;
	}
	
}
