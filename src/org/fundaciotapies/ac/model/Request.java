package org.fundaciotapies.ac.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.model.bo.Media;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.model.bo.User;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.ObjectFile;
import org.fundaciotapies.ac.model.support.Template;
import org.fundaciotapies.ac.model.support.TemplateSection;

import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class Request {
	private static Logger log = Logger.getLogger(Request.class);
	
	public String getCurrentLanguage() {
		return "ca"; // TODO: get language from client user config/cookies/session
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
	
	public String getRdf() {
		
		try {
			saveBackup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
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
			InfModel model = ModelUtil.getModel();
			
			// Get object by Id
			Resource res = model.getResource(Constants.RESOURCE_URI_NS+id);
			StmtIterator it = res.listProperties();
			result = new CustomMap();
			while(it.hasNext()) {
				Statement stmt = it.next();
				if (stmt.getObject().isLiteral()) {
					String lang = stmt.getObject().asLiteral().getLanguage();
					if (lang!=null && !"".equals(lang) && !lang.equals(getCurrentLanguage())) continue;
					
					result.put(stmt.getPredicate().getLocalName(), stmt.getObject().asLiteral().getString());
				} else {
					result.put(stmt.getPredicate().getLocalName(), stmt.getObject().asResource().getLocalName());
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
				qc = " . { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+cls+"> } ";
					
				List<String> subClassesList = listSubclasses(cls, false);
				for (String sc : subClassesList)
					qc += " UNION { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+sc+"> } ";
			}
			
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			String query = "SELECT ?s FROM <http://localhost:8890/ACData> WHERE { { ?s <"+Constants.ONTOLOGY_URI_NS+"FatacId> \""+id+"\" } " + qc + " } ";
			Query sparql = QueryFactory.create(query); 
		    QueryExecution qexec = VirtuosoQueryExecutionFactory.create(sparql, model) ;
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
	
	public List<String> listOntologyClasses() {
		List<String> result = null;

		try {
			// Load ontology
			VirtModel ont = ModelUtil.getOntology();
			
			Query sparql = QueryFactory.create(" select ?a where { ?a rdf:type owl:Class } "); 
		    QueryExecution qexec = VirtuosoQueryExecutionFactory.create(sparql, ont);
		    ResultSet rs = qexec.execSelect();
		    
		    result = new ArrayList<String>();
		    
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				RDFNode node = qs.get("a");
				result.add(node.asResource().getNameSpace());
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
			String classURI = Constants.ONTOLOGY_URI_NS + className;
			
			QueryExecution qe = VirtuosoQueryExecutionFactory.create("select * where { ?prop <http://www.w3.org/2000/01/rdf-schema#domain> <"+classURI+"> . ?prop <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type . OPTIONAL { ?prop <http://www.w3.org/2000/01/rdf-schema#range> ?range } } ", ModelUtil.getOntology());
			ResultSet rs = qe.execSelect();
			String lastPropName = null;
			String range = "";
			String propType = null;
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				String propName = qs.get("prop").asResource().getLocalName();
				if (!propName.equals(lastPropName) && lastPropName != null) {
					result.add(new String[] { lastPropName , ((!"".equals(range))?range.substring(2):"_"),  propType } );
					range = "";
				}
				
				propType = qs.get("type").asResource().getLocalName();
				if (qs.get("range")!=null) range += ", "+qs.get("range").asResource().getLocalName();
				lastPropName = propName;
			}
			
			if (lastPropName != null) {
				result.add(new String[] { lastPropName , ((!"".equals(range))?range.substring(2):"_") , propType } );
				range = "";
			}
			
			List<String> scl = listSuperClasses(className);
			for (String sc : scl) result.addAll(0, listClassProperties(sc));
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public Set<String> listClassPropertiesSimple(String className) {
		Set<String> result = new TreeSet<String>();

		try {
			if (className == null) return null;
			String classURI = Constants.ONTOLOGY_URI_NS + className;
			
			QueryExecution qe = VirtuosoQueryExecutionFactory.create("select * where { ?prop <http://www.w3.org/2000/01/rdf-schema#domain> <"+classURI+"> } ", ModelUtil.getOntology());
			ResultSet rs = qe.execSelect();
			
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				String propName = qs.get("prop").asResource().getLocalName();
				result.add(propName);
			}
			
			List<String> scl = listSuperClasses(className);
			for (String sc : scl) result.addAll(listClassPropertiesSimple(sc));
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public List<String> listSubclasses(String className, Boolean direct) {
		List<String> result = new ArrayList<String>();

		try {
			if (className == null) return null;
			String classURI = Constants.ONTOLOGY_URI_NS + className;
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF, ModelUtil.getOntology());
			OntClass ontClass = ont.getOntClass(classURI);
			
			// Use recursive calls to navigate through the class tree starting form given root class
			for (ExtendedIterator<OntClass> it = ontClass.listSubClasses(direct);it.hasNext();) {
				OntClass cls = it.next();
				result.add(cls.getLocalName());
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
			InfModel model = ModelUtil.getModel();
			
			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT ?s FROM <http://localhost:8890/ACData> WHERE { ?s <"+Constants.ONTOLOGY_URI_NS+"isAssignedTo> <"+Constants.RESOURCE_URI_NS+referredObjectId+"> . { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+"Rights> } } ORDER BY ?s ", model);
			ResultSet rs = vqe.execSelect();
			
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				result = r.get("s").asResource().getLocalName();
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
			InfModel model = ModelUtil.getModel();
			
			String qc = null;
			
			// If specified, filter results for given class name and for all its subclasses 
			if (className!=null && !"".equals(className) && !"_".equals(className)) {
				String[] clsl = className.split(",");
				
				for(String cls : clsl) {
					if (cls!=null && !"".equals(cls)) {
						if (qc==null) {
							qc = " . { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+cls+"> } "; 
						} else {
							qc += " UNION { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+cls+"> } ";
						}
						
						List<String> subClassesList = listSubclasses(cls, false);
						for (String sc : subClassesList)
							qc += " UNION { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+sc+"> } ";
					}
				}
			}

			if (qc==null) qc = "";

			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?s ?p ?o " + qc + " } ", model);
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
				
				if (r.get("o").isResource()) {
					currentObject.put(r.get("p").asResource().getLocalName(), r.get("o").asResource().getLocalName());
				} else {
					currentObject.put(r.get("p").asResource().getLocalName(), r.get("o").asLiteral().getString());
				}
			}

			result.put(lastId, currentObject);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}
	
	public List<String> listObjectsId(String className) {
		List<String> result = new ArrayList<String>();
		
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
							qc = " { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+cls+"> } "; 
						} else {
							qc += " UNION { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+cls+"> } ";
						}
						
						List<String> subClassesList = listSubclasses(cls, false);
						for (String sc : subClassesList)
							qc += " UNION { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+sc+"> } ";
					}
				}
			}

			if (qc==null) qc = "";

			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT ?s FROM <http://localhost:8890/ACData> WHERE { " + qc + " } ", model);
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

	
	public Map<String, CustomMap> search(String word, String className, String userId) {
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
							qc = " . { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+cls+"> } "; 
						} else {
							qc += " UNION { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+cls+"> } ";
						}
						
						List<String> subClassesList = listSubclasses(cls, false);
						for (String sc : subClassesList)
							qc += " UNION { ?s <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+sc+"> } ";
					}
				}
			}

			if (qc==null) qc = "";

			// Create search query
			String filter = "FILTER (regex(?o,\""+word+"\",\"i\") && !regex(?o, \""+Constants.RESOURCE_URI_NS+"\",\"i\") && !regex(?o, \""+Constants.ONTOLOGY_URI_NS+"\",\"i\")) ";
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?s ?p ?o " + qc + filter + " } ", model);
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
				
				currentId = r.get("s").asResource().getLocalName();
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
					currentObject.put(r.get("p").asResource().getLocalName(), r.get("o").asResource().getLocalName());
				} else {
					String lang = r.get("o").asLiteral().getLanguage();
					if (lang!=null && !"".equals(lang) && !lang.equals(getCurrentLanguage())) continue;
					
					currentObject.put(r.get("p").asResource().getLocalName(), r.get("o").asLiteral().getString());
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
			InfModel model = ModelUtil.getModel();
			
			// Create search query
			field = Constants.ONTOLOGY_URI_NS + field;
			className = Constants.ONTOLOGY_URI_NS + className;
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?s <"+field+"> ?o  FILTER regex(?o,\""+value+"\",\"i\") . ?s <"+Constants.RDF_URI_NS+"type> <"+className+"> } ORDER BY ?s ", model);
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
	
	
	public List<String[]> query(String sparql) {
		List<String[]> result = new ArrayList<String[]>();
		
		// Connect to rdf server
		InfModel model = ModelUtil.getModel();
		
		// Create search query
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, model);
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

	public void saveBackup() {
		
		try {
			// Connect to rdf server
			InfModel model = ModelUtil.getModel();
			
			// Create search query
			QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { ?a ?b ?c } ", model.getRawModel());
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
	
	public String getObjectClass(String id) {
		// Connect to rdf server
		InfModel model = ModelUtil.getModel();
		
		// Create search query
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { <"+Constants.RESOURCE_URI_NS+id+"> <"+Constants.RDF_URI_NS+"type> ?c } ", model);
		ResultSet rs = vqe.execSelect();
		
		if (rs.hasNext()) {
			QuerySolution r = rs.next();
			return r.get("c").asResource().getLocalName();
		} else return null;
	}
	
	public List<String> listSuperClasses(String className) {
		List<String> result = new ArrayList<String>();
		List<String> tmp = new ArrayList<String>();
		
		OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF, ModelUtil.getOntology());
		OntClass ontClass = ont.getOntClass(Constants.ONTOLOGY_URI_NS + className);
		
		if (ontClass!=null) {
			ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
			
			while(it.hasNext())	{
				OntClass cls = it.next();
				result.add(cls.getLocalName());
				tmp.add(cls.getLocalName());
			}
			
			for(String c : tmp) result.addAll(listSuperClasses(c));
		}
			
		return result;
	}
	
	private String[] resolveModelPathPart(String className, String property, String id, boolean includeId, boolean anyLanguage) {
		if ("class".equals(property)) return new String[]{ getObjectClass(id) }; // 'class' is reserved word
		
		List<String> result = new ArrayList<String>();
		
		// Connect to rdf server
		InfModel model = ModelUtil.getModel();
		
		String classClause = "";
		String idClause = " ?a ";
		String propertyClause = " <"+Constants.ONTOLOGY_URI_NS+property+"> ";
		if (id!=null) idClause = " <"+Constants.RESOURCE_URI_NS+id+"> "; 
		if (className!=null && !"*".equals(className)) classClause = ". "+ idClause +" <"+Constants.RDF_URI_NS+"type> <"+Constants.ONTOLOGY_URI_NS+className+"> ";
		
		// Create search query
		QueryExecution vqe = VirtuosoQueryExecutionFactory.create("SELECT * WHERE { " + idClause + propertyClause + " ?c "+ classClause +" } ", model);
		ResultSet rs = vqe.execSelect();
		while(rs.hasNext()) {
			QuerySolution s = rs.next();
			RDFNode node = s.get("c");
			
			if (node.isLiteral())  {
				if (!anyLanguage) {
					String lang = node.asLiteral().getLanguage();
					if (lang!=null && !"".equals(lang) && !lang.equals(getCurrentLanguage())) continue;
				}
				result.add(node.asLiteral().getString() + (includeId?"@"+id:""));
			} else {
				result.add(node.asResource().getLocalName());
			}
		}
		
		String[] res = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	public String[] resolveModelPath(String path, String id, boolean anyLanguage) {
		return resolveModelPath(path, id, false, anyLanguage);
	}
	
	public String[] resolveModelPath(String path, String id, boolean includeId, boolean anyLanguage) {
		if (path==null) return new String[]{};
		
		int idx = path.indexOf(":");
		String part = path;
		if (idx!=-1) part = path.substring(0, idx);
		
		String[] atoms = part.split("\\.");
		String[] values = resolveModelPathPart(atoms[0], atoms[1], id, includeId, anyLanguage);
		
		if (idx!=-1) {
			List<String> valueList = new ArrayList<String>();
			for (String v : values) 
				valueList.addAll(Arrays.asList(resolveModelPath(path.substring(idx+1), v, includeId, anyLanguage)));
			
			values = new String[valueList.size()];
			valueList.toArray(values);
		} 
		
		return values;
	}
	
	private Template getObjectTemplate(String id) throws Exception {
		
		String className = getObjectClass(id);
		File f = new File(Constants.CONFIGURATIONS_PATH+"mapping/"+className+".json");
		
		if (!f.exists()) {
			List<String> superClasses = listSuperClasses(className);
			for (String superClassName : superClasses) {
				f = new File(Constants.CONFIGURATIONS_PATH+"mapping/"+superClassName+".json");
				if (f.exists()) break;
			}
		}
		
		if (!f.exists()) { 
			log.warn("Trying to obtain template from no-template object class");
			return null;
		}
		
		return new Gson().fromJson(new FileReader(f), Template.class);
	}
	
	public void getObjectSectionView(TemplateSection section, String id) {
		for (DataMapping dm : section.getData()) {
			String type = dm.getType();
			
			if ("text".equals(type) || "objects".equals(type) || "media".equals(type)) {
				for (String path : dm.getPath()) {
					if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
					dm.getValue().addAll(Arrays.asList(resolveModelPath(path, id, false)));
				}
			} else if ("linkedObjects".equals(type)) {
				for (String path : dm.getPath()) {
					if (dm.getValue()==null) dm.setValue(new ArrayList<String>());
					dm.getValue().addAll(Arrays.asList(resolveModelPath(path, id, true, false)));
				}
			}
			
			dm.setPath(null);
		}
	}
	
	public Template getObjectView(String id) {
		Template template = null;
		
		try {
			template = getObjectTemplate(id);
			if (template == null) return null;
			
			for (TemplateSection section : template.getSections()) getObjectSectionView(section, id);
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return template;
	}
	
	private boolean downloadImage(String path) throws Exception {
		boolean downloaded = false;
		
		File f = new File(Constants.MEDIA_PATH+"tmp.jpg");
		if (f.exists()) f.delete();
		
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("Content-Type", "image/jpg");
		conn.setDoInput(true);
	    conn.setRequestMethod("GET");
	    
	    OutputStream os = new FileOutputStream(Constants.MEDIA_PATH+"tmp.jpg");
	    
	    InputStream is = conn.getInputStream();
	    byte[] buffer = new byte[1024];
	    int readLength = is.read(buffer);
	    while(readLength != -1)
	    {
	    	downloaded = true;
	        os.write(buffer,0,readLength);
	        readLength = is.read(buffer);
	    }
	    
	    os.close();
	    
	    return downloaded;
	}
	
	private void resizeImage(BufferedImage img0, BufferedImage img1, BufferedImage img2, BufferedImage img3, String id) throws Exception {
		if (img0==null) return;
		
		int w = Constants.THUMBNAIL_WIDTH;
		int h = Constants.THUMBNAIL_HEIGHT;
		
		BufferedImage resizedImage = new BufferedImage(Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT, img0.getType());
		Graphics2D gResult = resizedImage.createGraphics();
		gResult.setColor(Color.WHITE);
		gResult.fillRect(0, 0, Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT);
		
		float margin = w*0.04f;
		
		if (img1!=null && img2==null) w = Math.round((w/2) - margin/2);
		if (img2!=null && img3!=null) {
			w = Math.round((w/2) - margin/2);
			h = Math.round((h/2) - margin/2);
		}
		
		float widthScale = img0.getWidth() / w;
		float heightScale = img0.getHeight() / h;
		float scale = widthScale>heightScale?heightScale:widthScale;
		
		BufferedImage cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img0.getType());
		Graphics2D gCut = cutImage.createGraphics();
		gCut.setColor(Color.WHITE);
		gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
		gCut.drawImage(img0, cutImage.getWidth()/2 - img0.getWidth()/2, cutImage.getHeight()/2 - img0.getHeight()/2, null);
		gResult.drawImage(cutImage, 0, 0, w, h, null);
		cutImage.flush();
		
		if (img1!=null) {
			widthScale = img1.getWidth() / w;
			heightScale = img1.getHeight() / h;
			scale = widthScale>heightScale?heightScale:widthScale;
			
			cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img1.getType());
			gCut = cutImage.createGraphics();
			gCut.setColor(Color.WHITE);
			gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
			gCut.drawImage(img1, cutImage.getWidth()/2 - img1.getWidth()/2, cutImage.getHeight()/2 - img1.getHeight()/2, null);
			gResult.drawImage(cutImage, w+Math.round(margin), 0, w, h, null);
			cutImage.flush();
		} 
		
		if (img2!=null && img3!=null) {
			widthScale = img2.getWidth() / w;
			heightScale = img2.getHeight() / h;
			scale = widthScale>heightScale?heightScale:widthScale;
			
			cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img2.getType());
			gCut = cutImage.createGraphics();
			gCut.setColor(Color.WHITE);
			gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
			gCut.drawImage(img2, cutImage.getWidth()/2 - img2.getWidth()/2, cutImage.getHeight()/2 - img2.getHeight()/2, null);
			gResult.drawImage(cutImage, 0, h+Math.round(margin), w, h, null);
			cutImage.flush();
			
			widthScale = img3.getWidth() / w;
			heightScale = img3.getHeight() / h;
			scale = widthScale>heightScale?heightScale:widthScale;
			
			cutImage = new BufferedImage(scale>1?Math.round(w*scale):w, scale>1?Math.round(h*scale):h, img3.getType());
			gCut = cutImage.createGraphics();
			gCut.setColor(Color.WHITE);
			gCut.fillRect(0, 0, cutImage.getWidth(), cutImage.getHeight());
			gCut.drawImage(img3, cutImage.getWidth()/2 - img3.getWidth()/2, cutImage.getHeight()/2 - img3.getHeight()/2, null);
			gResult.drawImage(cutImage, w+Math.round(margin), h+Math.round(margin), w, h, null);
			cutImage.flush();
		}
		
		gResult.dispose();
		gCut.dispose();
		
		File f = new File(Constants.MEDIA_PATH + "thumbnails/" + id + ".jpg");
		ImageIO.write(resizedImage, "jpg", f);
	}

	
	public File getClassThumbnail(String className) {
		try {
			File f = new File(Constants.MEDIA_PATH + "thumbnails/classes/" + className + ".jpg");
			
			if (!f.exists()) {
				List<String> superClasses = listSuperClasses(className);
				for (String superClassName : superClasses) {
					f = new File(Constants.MEDIA_PATH + "thumbnails/classes/"+superClassName+".jpg");
					if (f.exists()) break;
				}
			}
			
			if (!f.exists()) f = new File(Constants.MEDIA_PATH + "thumbnails/classes/default.jpg");
			
			return f;
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return null;
	}
	
	public InputStream getObjectThumbnail(String id) {
		try {
			File f = new File(Constants.MEDIA_PATH + "thumbnails/" + id + ".jpg");
			
			List<String> medias = new ArrayList<String>();
			List<String> subobjects = new ArrayList<String>();
			
			if (!f.exists()) {
			
				Template template = getObjectTemplate(id);
				if (template == null) return null;
				
				for (TemplateSection section : template.getSections()) {
					for (DataMapping d : section.getData()) {
						for (String path : d.getPath()) {
							if (d.getType().equals("media")) {
								medias.addAll(Arrays.asList(resolveModelPath(path, id, false, true)));
							} else if (d.getType().equals("objects")) {
								subobjects.addAll(Arrays.asList(resolveModelPath(path, id, false, true)));
							}
						}
					}
				}
				
				int count = 0;
				List<BufferedImage> il = new ArrayList<BufferedImage>();
				
				if (medias.size()>0) {
					for (String m : medias) {
						if (downloadImage(m)) {
							il.add(ImageIO.read(new File(Constants.MEDIA_PATH+"tmp.jpg")));
							count++;
						}
						
						if (count>=4) break;
					}
				} else if (subobjects.size()>0) {					
					for (String o : subobjects) {
						InputStream in = getObjectThumbnail(o);
						if (in!=null) {
							il.add(ImageIO.read(in));
							count++;
						}
						
						if (count>=4) break;
					}
				}
				
				resizeImage(il.size()>0?il.get(0):null, il.size()>1?il.get(1):null, il.size()>2?il.get(2):null, il.size()>3?il.get(3):null, id);
				f = new File(Constants.MEDIA_PATH + "thumbnails/" + id + ".jpg");
			}

			if (!f.exists()) {
				String className = getObjectClass(id);
				f = getClassThumbnail(className);
			}
			
			if (f.exists())	return new FileInputStream(f);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return null;
	}
	
}
