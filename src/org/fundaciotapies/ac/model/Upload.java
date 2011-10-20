package org.fundaciotapies.ac.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.model.bo.Media;
import org.fundaciotapies.ac.model.bo.IdentifierCounter;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.rest.client.Profile;
import org.fundaciotapies.ac.rest.client.Transco;
import org.fundaciotapies.ac.rest.client.TranscoEntity;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtTransactionHandler;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.vocabulary.RDF;

public class Upload {
	private static Logger log = Logger.getLogger(Upload.class);
	
	public List<String> script = null;
	public OntModel data = null;
	
	private static VirtModel vm = null;
	
	private VirtModel getVirtModel() {
		if (vm==null || vm.isClosed()) vm = VirtModel.openDatabaseModel("http://localhost:8890/ACData",Constants.RDFDB_URL, "dba", "dba");
		return vm;
	}
	
	private String normalizeId(String about) throws Exception {
		String temp = Normalizer.normalize(about.trim(), Normalizer.Form.NFD);
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    String result = pattern.matcher(temp).replaceAll("").replaceAll("\\<.*?>","").replaceAll("[^A-Za-z0-9()_\\s\\-\\']", "").replaceAll("[\\s+\\n+\\t+]", "_");
	    if (result.length()>140) result = result.substring(0, 140);
	    return result;
	}
	
	private String generateObjectId(String about) throws Exception {
		if (about == null) throw new NullPointerException();
		IdentifierCounter oc = new IdentifierCounter();
		
		about = normalizeId(about);
		
		oc.load(about);
		oc.setCounter(oc.getCounter()+1);
		if (oc.getCounter() == 1l) {
			oc.save();
		} else {
			oc.update();
		}
		
		Long n = oc.getCounter();
		if (n>1) 
			return about + "_" + oc.getCounter();
		else
			return about;
	}
	
	public void addVideoFile(String id) {
		
		try {
			TranscoEntity transco = new TranscoEntity();
			//transco.setSrc_path("http://stress:8080/ArtsCombinatoriesRest/getObjectFile?id="+id);
			transco.setSrc_path("/tmp/test.dv"); // TODO: delete line and uncomment above
			transco.setProfiles(new ArrayList<Profile>());
			Profile prof = new Profile();
			prof.setType("0");
			prof.setDst_path("/tmp/"+id+"___file.mp4");
			//prof.setDst_path("/"+id+"___file.mp4"); TODO: uncomment
			transco.getProfiles().add(prof);
			
			String res = new Transco().addTransco(transco);
			System.out.println(res);
		} catch (Exception e) {
			log.error("Error ", e);
		}
	}
	
	public String addMediaFile(InputStream in, String filePath) {
		
		//String id = Long.toHexString(Double.doubleToLongBits(Math.random())) + Long.toHexString(Double.doubleToLongBits(Math.random()));
		String id = "_media_file_";
		
		try {
			String[] tmp = filePath.split("\\.");
			String ext = "";
			if (tmp!=null && tmp.length>0) ext = tmp[tmp.length-1];
			
			IdentifierCounter oc = new IdentifierCounter();
			oc.load(id);
			oc.setCounter(oc.getCounter()+1);
			if (oc.getCounter() == 1l) oc.save(); else oc.update();
			
			id = id + oc.getCounter();
			
			filePath = id + "." + ext;
			File f = new File(Constants.FILE_DIR+filePath);
			
			OutputStream fout = new FileOutputStream(f);
			   
			byte[] buffer = new byte[1024*1024]; 
			int len = 0;
			while((len=in.read(buffer))!=-1) {
				fout.write(buffer, 0, len);
				fout.flush();
			}
			
			fout.close();
			
			Media media = new Media();
			media.setPath(Constants.FILE_DIR+filePath);
			media.setMediaId(id);
			media.saveUpdate();
			
			for (String s : Constants.VIDEO_FILE_EXTENSIONS) {
				if (s.equals(ext)) {
					addVideoFile(id);
					break;
				}
			}
		} catch (Exception e) {
			log.error("Error ", e);
			e.printStackTrace();
			return "error";
		}
		
		return Constants.REST_URL+"media/"+id;
	}
	
	private String extractUriId(String URI) {
		return URI.replace(Constants.RESOURCE_BASE_URI, "").replace(Constants.RDF_URI_NS, "").replace(Constants.AC_URI_NS, "");
	}
	
	public String uploadResource(String className, String about, String[] properties, String[] propertyValues) {
		String result = "error";
		if (className==null) return "error";
		if (about==null) about = className;
		
		try {
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			if (new File("data.owl").exists())	model.read("file:data.owl");
			
			String[] cls = className.split(",");
			String id = generateObjectId(about);
			String fullId = Constants.RESOURCE_BASE_URI + id;
			
			Resource res = model.getResource(fullId);
			if (res==null) res = model.createResource(fullId);
			
			List<ObjectProperty> lop = model.listObjectProperties().toList();
			
			int i = 0;
			
			for (String classNameElement : cls) 
				res.addProperty(RDF.type, model.getResource(Constants.AC_URI_NS+classNameElement));
			
			while(i<properties.length) {
				boolean isObjectProperty = false;
				
				for(ObjectProperty op : lop) {
					if (extractUriId(op.toString()) .equals(properties[i])) {
						isObjectProperty = true;
						break;
					}
				}
				
				Property prop = model.getProperty(Constants.AC_URI_NS+properties[i].trim());
				if (isObjectProperty) {
					Resource res2 = model.getResource(Constants.RESOURCE_BASE_URI+propertyValues[i]);
					res.addProperty(prop, res2);
				} else {
					propertyValues[i] = propertyValues[i].replace('"', '\'').replace('\n', ' ').replace('\t', ' ');
					res.addProperty(prop, propertyValues[i]);
				}
				
				i++;
			}
			
			model.write(new FileWriter("data.owl"));
			result = "sucess";
		} catch (Exception e) {
			log.error("Error ", e);
		}
		
		return result;
	}
	
	public String uploadObject(String className, String about, String[] properties, String[] propertyValues) {
		String result = "error";
		VirtTransactionHandler vth = null;
		if (className==null) return "error";
		if (about==null) about = className;
		
		try {
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, getVirtModel());
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl");
			
			String[] cls = className.split(",");
			String id = generateObjectId(about);
			
			String fullId = Constants.RESOURCE_BASE_URI + id;
			
			int i = 0;
			
			script = new ArrayList<String>();
			for (String classNameElement : cls) 
				script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> rdf:type <"+Constants.AC_URI_NS+classNameElement+"> } ");
			
			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			
			List<String> lcp = new ArrayList<String>(new Request().listClassPropertiesSimple(className));
			while(i<properties.length) {
				boolean isObjectProperty = false;
				
				if (!lcp.contains(properties[i]) && !"FatacId".equals(properties[i])) { i++; continue; }
				for(ObjectProperty op : lop) {
					if (extractUriId(op.toString()) .equals(properties[i])) {
						isObjectProperty = true;
						break;
					}
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) 
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> <"+Constants.AC_URI_NS+properties[i].trim()+"> <"+Constants.RESOURCE_BASE_URI+propertyValues[i]+"> }");
					else {
						String lang = null;
						
						for (String l : Constants.LANG_LIST) {
							if (propertyValues[i].endsWith("@"+l)) {
								lang = "@"+l;
								break;
							}
						}
						
						if (lang!=null) propertyValues[i] = propertyValues[i].substring(0, propertyValues[i].length()-3);
						
						propertyValues[i] = propertyValues[i].replace('"', '\'').replace('\n', ' ').replace('\t', ' ');
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> <"+Constants.AC_URI_NS+properties[i].trim()+"> \"" + propertyValues[i] + "\""+(lang!=null?lang:"")+" }");
					}
				}
				
				i++;
			}
			
			Command c = new Command() {
				@Override
				public Object execute() {
					//System.out.println(script);
					for (String s : script)
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(data.getBaseModel().getGraph()))).exec();
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)data.getBaseModel().getGraph());
			vth.begin();
			vth.executeInTransaction(c);
			vth.commit();
			
			result = id;
		} catch (Exception e) {
			log.error("Error ", e);
			if (vth!=null) vth.abort();
		} 
		
		return result;
	}
	
	
	public String deleteObject(String objectId) {
		String result = "error";
		VirtTransactionHandler vth = null;
		
		try {
			Media media = new Media();
			media.load(objectId);
			
			if (media.getSid()!=null) {
				File f = new File(media.getPath());
				if (f.exists()) f.delete();
			}
			
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, getVirtModel());
			
			script = new ArrayList<String>();
			script.add("DELETE FROM <http://localhost:8890/ACData> { ?a ?b ?c } WHERE { ?a ?b ?c FILTER (?a = <"+Constants.RESOURCE_BASE_URI+objectId+"> or ?c = <"+Constants.RESOURCE_BASE_URI+objectId+">) . ?a ?b ?c }");
			
			Command c = new Command() {
				@Override
				public Object execute() {
					for (String s : script)
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(data.getBaseModel().getGraph()))).exec();
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)data.getBaseModel().getGraph());
			vth.begin();
			vth.executeInTransaction(c);
			vth.commit();
			
			Right right = new Right();
			right.setObjectId(objectId);
			right.delete();
			
			result = "success";
		} catch (Exception e) {
			log.error("Error ", e);
		}
		
		return result;
	}

	
	public String updateObject(String uniqueId, String[] properties, String[] propertyValues) {
		String result = "error";
		VirtTransactionHandler vth = null;
		
		Set<String> alreadyDeleted = new TreeSet<String>();
		
		try {
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, getVirtModel());
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl");
			
			int i = 0;
			
			script = new ArrayList<String>();

			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			while(i<properties.length) {
				if ("class".equalsIgnoreCase(properties[i])) {
					i++;
					continue;
				}
				boolean isObjectProperty = false;
				
				for(ObjectProperty op : lop) {
					if (extractUriId(op.toString()).equals(properties[i])) {
						isObjectProperty = true;
						break;
					}
				}
				
				if ((!"filePath".equals(properties[i]) || "filePath".equals(properties[i]) && !"".equals(propertyValues[i])) && (!alreadyDeleted.contains(properties[i]))) {
					script.add("DELETE FROM <http://localhost:8890/ACData> { ?a ?b ?c } WHERE { ?a <"+Constants.AC_URI_NS+properties[i]+"> ?c FILTER (?a = <"+Constants.RESOURCE_BASE_URI+uniqueId+">) . ?a ?b ?c }");
					alreadyDeleted.add(properties[i]);
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) {
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+Constants.RESOURCE_BASE_URI+uniqueId+"> <"+Constants.AC_URI_NS+properties[i]+"> <"+Constants.RESOURCE_BASE_URI+propertyValues[i]+"> }");
					} else {
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+Constants.RESOURCE_BASE_URI+uniqueId+"> <"+Constants.AC_URI_NS+properties[i]+"> \"" + propertyValues[i] + "\" }");
					}
				}
				i++;
			}
			
			Command c = new Command() {
				@Override
				public Object execute() {
					for (String s : script)
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(data.getBaseModel().getGraph()))).exec();
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)data.getBaseModel().getGraph());
			vth.begin();
			vth.executeInTransaction(c);
			vth.commit();
			
			result = "success";
		} catch (Exception e) {
			log.error("Error ", e);
			if (vth!=null) vth.abort();
		} 
		
		return result;
	}
	
	public String proves() {
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> SELECT ?Pizza ?Eaten where {?Pizza a ?y. ?y rdfs:subClassOf pizza:Pizza. Optional {?Pizza pizza:Eaten ?Eaten}}";
        
		data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://www.co-ode.org/ontologies/pizza/pizza.owl#", Constants.RDFDB_URL, "dba", "dba"));
		//OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		//ont.read("file:OntologiaArtsCombinatories.owl");
		//ont.add(data);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(QueryFactory.create(queryString),(VirtGraph) data.getBaseModel().getGraph());
		ResultSet rs = vqe.execSelect();
		
		String currentId = null;
		
		// Get IDs that fit specific search
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			currentId = r.get("Pizza").toString() + "    " + r.get("Eaten").toString();
			System.out.println(currentId);
			
		}
		
		return null;
	}
	
	public String mixAndExportOntologyAndData() {
		VirtTransactionHandler vth = null;
		
		try {
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, getVirtModel());
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl");
			
			script = new ArrayList<String>();
			
			StmtIterator it = ont.listStatements();
			while(it.hasNext()) {
				Statement stmt = it.next();
				Resource  subject   = stmt.getSubject();     // get the subject
			    Property  predicate = stmt.getPredicate();   // get the predicate
			    RDFNode   object    = stmt.getObject();      // get the object
			    
			    if (!object.isLiteral())
			    	script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+subject.toString()+"> <"+predicate.toString()+"> <"+object.toString()+"> } ");
			    else {
			    	String value = object.toString().replaceAll("\\n", "\\s");
			    	script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+subject.toString()+"> <"+predicate.toString()+"> \""+value+"\" } ");
			    }
			}
			
			Command c = new Command() {
				@Override
				public Object execute() {
					for (String s : script) {
						System.out.println(s);
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(data.getBaseModel().getGraph()))).exec();
					}
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)data.getBaseModel().getGraph());
			vth.begin();
			vth.executeInTransaction(c);
			vth.commit();
		} catch (Exception e) {
			log.error("Error ", e);
			if (vth!=null) vth.abort();
			return "error";
		}
		
		return "success";
	}
}
