package org.fundaciotapies.ac.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.model.bo.Media;
import org.fundaciotapies.ac.model.bo.ObjectCounter;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.rest.client.Profile;
import org.fundaciotapies.ac.rest.client.Transco;
import org.fundaciotapies.ac.rest.client.TranscoEntity;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtTransactionHandler;
import virtuoso.jena.driver.VirtuosoUpdateFactory;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Command;

public class Upload {
	private static Logger log = Logger.getLogger(Upload.class);
	
	public List<String> script = null;
	public OntModel data = null;
	
	private String generateObjectId(String className) throws Exception {
		if (className == null) throw new NullPointerException();
		ObjectCounter oc = new ObjectCounter();
		
		oc.load(className);
		oc.setCounter(oc.getCounter()+1);
		if (oc.getCounter() == 1l) {
			oc.save();
		} else {
			oc.update();
		}
		
		if (oc.getCounter()<1000000000) {
			NumberFormat nf = new DecimalFormat("000000000");
			return className.toLowerCase() + "/" + nf.format(oc.getCounter());
		} else {
			throw new Exception("Id counter limit reached!");
		}
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
	
	public String addMediaFile(InputStream in, String id, String filePath) {
		
		try {
			String[] tmp = filePath.split("\\.");
			String ext = "";
			if (tmp!=null && tmp.length>0) ext = tmp[tmp.length-1];
			
			filePath = id+"___file."+ext;
			File f = new File(Constants.FILE_DIR+filePath);
			OutputStream fout = new FileOutputStream(f);
			   
			byte[] buffer = new byte[256]; 
			int len = 0;
			while((len=in.read(buffer))!=-1) fout.write(buffer, 0, len);
			
			fout.flush();
			fout.close();
			
			Media media = new Media();
			media.setPath(Constants.FILE_DIR+filePath);
			media.setObjectId(id);
			media.saveUpdate();
			
			for (String s : Constants.VIDEO_FILE_EXTENSIONS) {
				if (s.equals(ext)) {
					addVideoFile(id);
					break;
				}
			}
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
		
		return "success";
	}
	
	public String uploadObject(String className, String[] properties, String[] propertyValues) {
		String result = "error";
		VirtTransactionHandler vth = null;
		
		try {
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData", Constants.RDFDB_URL, "dba", "dba"));
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl");
			
			String id = generateObjectId(className);
			String fullId = Constants.baseURI + id;
			
			int i = 0;
			
			script = new ArrayList<String>();
			script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> rdf:type <"+Constants.OWL_URI_NS+className+"> } ");
			
			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			List<String> lcp = new Request().listClassPropertiesSimple(className);
			while(i<properties.length) {
				boolean isObjectProperty = false;
				
				if (!lcp.contains(properties[i])) { i++; continue; }
				for(ObjectProperty op : lop) {
					if (op.getLocalName().equals(properties[i])) {
						isObjectProperty = true;
						break;
					}
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) 
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> <"+Constants.OWL_URI_NS+properties[i].trim()+"> <"+Constants.baseURI+propertyValues[i]+"> }");
					else {
						String lang = null;
						
						if (propertyValues[i].endsWith("@ca")) {
							lang = "@ca";
						} else if (propertyValues[i].endsWith("@es")) {
							lang = "@es";
						} else if (propertyValues[i].endsWith("@en")) {
							lang = "@en";
						} 
						
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> <"+Constants.OWL_URI_NS+properties[i].trim()+"> \"" + propertyValues[i] + "\""+(lang!=null?"@"+lang:"")+" }");
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
			
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData", Constants.RDFDB_URL, "dba", "dba"));
			
			script = new ArrayList<String>();
			script.add("DELETE FROM <http://localhost:8890/ACData> { ?a ?b ?c } WHERE { ?a ?b ?c FILTER (?a = <"+Constants.baseURI+objectId+"> or ?c = <"+Constants.baseURI+objectId+">) . ?a ?b ?c }");
			
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
		
		try {
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData", Constants.RDFDB_URL, "dba", "dba"));
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl");
			
			int i = 0;
			
			script = new ArrayList<String>();

			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			while(i<properties.length) {
				boolean isObjectProperty = false;
				
				for(ObjectProperty op : lop) {
					if (op.getLocalName().equals(properties[i])) {
						isObjectProperty = true;
						break;
					}
				}
				
				if (!"filePath".equals(properties[i]) || "filePath".equals(properties[i]) && !"".equals(propertyValues[i]))
					script.add("DELETE FROM <http://localhost:8890/ACData> { ?a ?b ?c } WHERE { ?a <"+Constants.OWL_URI_NS+properties[i]+"> ?c FILTER (?a = <"+Constants.baseURI+uniqueId+">) . ?a ?b ?c }");
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) {
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+Constants.baseURI+uniqueId+"> <"+Constants.OWL_URI_NS+properties[i]+"> <"+Constants.baseURI+propertyValues[i]+"> }");
					} else {
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+Constants.baseURI+uniqueId+"> <"+Constants.OWL_URI_NS+properties[i]+"> \"" + propertyValues[i] + "\" }");
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
	

}
