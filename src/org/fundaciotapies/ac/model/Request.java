package org.fundaciotapies.ac.model;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.model.bo.Media;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.model.support.ObjectFile;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class Request {
	private static Logger log = Logger.getLogger(Request.class);
	
	public String getRdf() {
		// Connect to rdf server
		OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));

		// Get object in its RDF specification
		StringWriter sw = new StringWriter();
		data.write(sw);
		return sw.toString();
	}


	public Map<String, String> getObject(String id) {
		Map<String, String> result = null;

		try {
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			int userLegalLevel = 1;
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel) throw new Exception("Access to object denied due to legal restrictions");
			
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			String uniqueId = "http://ac.org/"; // TODO: get id prefix from global configuration 
			
			// Get object by Id
			Individual ind = data.getIndividual(uniqueId+id);
			StmtIterator it = ind.listProperties();
			result = new TreeMap<String, String>();
			while(it.hasNext()) {
				Statement stmt = it.next();
				if (stmt.getObject().isLiteral())
					result.put(stmt.getPredicate().getLocalName(), stmt.getObject().asLiteral().getString());
				else
					result.put(stmt.getPredicate().getLocalName(), stmt.getObject().asResource().getLocalName());
			}
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public ObjectFile getObjectFile(String id) {
		try {
			// Checks whether user can view object or not
			Right right = new Right();
			right.load(id);
			int userLegalLevel = 1;
			if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel) throw new Exception("Access to object denied due to legal restrictions");
			
			// Only media objects have associated files
			String className = new Request().getObject(id).get("type");
			boolean isMediaObject = new Request().listAllSubclasses("MEDIA").contains(className);
			if (!isMediaObject) return null;
			
			Media media = new Media();
			media.load(id);
			String ext = media.getPath().substring(media.getPath().length()-3);
			
			// Assign media type
			String mime = "application/"+ext;
			if ("png,jpg,gif,svg".contains(ext)) {
				mime = "image/"+ext;
			} else if ("html".contains(ext)) {
				mime = "text/"+ext;
			} else if ("txt".contains(ext)) {
				mime = "text/plain";
			} else if ("avi".equals(ext)) {
				mime = "video/x-msvideo";
			} else if ("mpeg,mpg".contains(ext)) {
				mime = "video/mpeg";
			}
			
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
	            result.add(actual.getLocalName());
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
			String classURI = Constants.OWL_URI_NS + className;
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config 

			OntClass ontClass = ont.getOntClass(classURI);

			// Get all properties for given class
			Iterator<OntProperty> it = ontClass.listDeclaredProperties();
			result = new ArrayList<String>();
			while (it.hasNext()) {
				OntProperty prop = it.next();
				OntResource range = prop.getRange();
				// Creates string with property info: whether it's data or object property, functional, symmetric, etc.
				result.add(prop.getLocalName() + " " + ((range!=null)?range.getLocalName():"_") + " " +
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
	
	public List<String> listClassPropertiesSimple(String className) {
		List<String> result = null;

		try {
			if (className == null) return null;
			String classURI = Constants.OWL_URI_NS + className;
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config 

			OntClass ontClass = ont.getOntClass(classURI);

			// Get all properties for given class
			Iterator<OntProperty> it = ontClass.listDeclaredProperties();
			result = new ArrayList<String>();
			while (it.hasNext()) {
				OntProperty prop = it.next();
				result.add(prop.getLocalName());
			}
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result;
	}
	
	public List<String> listAllSubclasses(String className) {
		List<String> result = new ArrayList<String>();

		try {
			if (className == null) return null;
			String classURI = Constants.OWL_URI_NS + className;
			// Load ontology
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl"); // TODO: get ontology file path from global config 

			OntClass ontClass = ont.getOntClass(classURI);
			
			// Use recursive calls to navigate through the class tree starting form given root class
			for (ExtendedIterator<OntClass> it = ontClass.listSubClasses();it.hasNext();) {
				OntClass cls = it.next();
				result.add(cls.getLocalName());
				result.addAll(listAllSubclasses(cls.getLocalName()));
			}	
			
		} catch (Throwable e) {
			log.error("Error ", e);
		}

		return result; 
	}
	
	public Map<String, Map<String, String>> search(String word, String className, String role) {
		Map<String, Map<String, String>> result = new TreeMap<String, Map<String,String>>();
		
		try {
			// Connect to rdf server
			OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba"));
			
			String qc = "";
			// If specified, filter results for given class name and all its subclasses 
			if (className!=null && !"".equals(className)) {
				qc = " . { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+Constants.OWL_URI_NS+className+"> } ";
				List<String> subClassesList = listAllSubclasses(className);
				for (String sc : subClassesList)
					qc += " UNION { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+Constants.OWL_URI_NS+sc+"> } ";
			}

			// Create search query
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create("SELECT * FROM <http://localhost:8890/ACData> WHERE { ?s ?p ?o " + qc + "FILTER regex(?o,\""+word+"\",\"i\") } ORDER BY ?s "),(VirtGraph) data.getBaseModel().getGraph());
			ResultSet rs = vqe.execSelect();
			
			String currentId = null;
			String lastId = null;
			Map<String, String> currentObject = null;
			
			// Get results (triples) and structure them in a 3 dimension map (object name - property name - property value)
			Right right = null;
			while (rs.hasNext()) {
				QuerySolution r = rs.next();
				currentId = r.get("s").asResource().getLocalName();
				if (!currentId.equals(lastId)) {
					if (lastId != null) result.put(lastId, currentObject);
					currentObject = new TreeMap<String, String>();
					
					right = new Right();
					right.load(currentId);
				}
				
				// TODO: Implement properly legal restrictions on search (assign userLegalLevel the right value depending on logged user role)
				int userLegalLevel = 1;
				if (role!=null) {
					try {
						userLegalLevel = Integer.parseInt(role);
					} catch (NumberFormatException e) {	log.warn(e.toString()); }
				}
				if (right.getRightLevel() !=null && right.getRightLevel() > userLegalLevel) continue;
				
				if (r.get("o").isResource()) {
					currentObject.put(r.get("p").asResource().getLocalName(), r.get("o").asResource().getLocalName());
				} else {
					currentObject.put(r.get("p").asResource().getLocalName(), r.get("o").asLiteral().getString());
				}
				
				lastId = currentId;
			}
			
			// Solved bug: we need to put the last found object!
			if (lastId != null) result.put(lastId, currentObject);
		} catch (Throwable e) {
			log.error("Error ", e);
		}
		
		return result;
	}
	
}
