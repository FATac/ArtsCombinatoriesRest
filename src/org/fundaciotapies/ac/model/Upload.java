package org.fundaciotapies.ac.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.bo.Media;
import org.fundaciotapies.ac.model.bo.IdentifierCounter;
import org.fundaciotapies.ac.model.bo.ResourceStatistics;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.rest.client.Profile;
import org.fundaciotapies.ac.rest.client.Transco;
import org.fundaciotapies.ac.rest.client.TranscoEntity;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtTransactionHandler;
import virtuoso.jena.driver.VirtuosoUpdateFactory;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Command;

public class Upload {
	private static Logger log = Logger.getLogger(Upload.class);
	
	public List<String> script = null;
	
	public InfModel model = null;
	
	private String normalizeId(String about) throws Exception {
		String temp = Normalizer.normalize(about.trim(), Normalizer.Form.NFD);
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    String result = pattern.matcher(temp).replaceAll("").replaceAll("\\<.*?>","").replaceAll("[^A-Za-z0-9_\\s]", "").replaceAll("[\\s+\\n+\\t+]", "_");
	    if (result!=null && !"".equals(result)) {
	    	if ("0123456789".contains(result.charAt(0)+"")) result = "_" + result;
	    } else {
	    	result = "Unidentified";
	    }
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
	
	public String encodeMediaFile(String filePath, int profile) {
		
		try {
			String ext = filePath.split("\\.")[1];
			String name = filePath.split("\\.")[0];
			
			String filePathMaster = name + "_master." + ext;
			
			File f1 = new File(Cfg.MEDIA_PATH + filePath);
			File f2 = new File(Cfg.MEDIA_PATH + filePathMaster);
			f1.renameTo(f2);
			
			if ("mp3,aif,wma,wav,ogg,oga".contains(ext)) ext = "ogg"; else ext = "ogv";
		
			TranscoEntity transco = new TranscoEntity();
			transco.setSrc_path(Cfg.MEDIA_PATH + filePathMaster);
			transco.setProfiles(new ArrayList<Profile>());
			Profile prof = new Profile();
			prof.setType(profile+"");
			prof.setDst_path(Cfg.MEDIA_PATH + name + "." + ext);
			transco.getProfiles().add(prof);
			
			String res = new Transco().addTransco(transco);
			log.info(res);
			
			return name + "." + ext;
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
	
	public String auxId;
	public String removeMediaFile(String id) {
		
		try {
			Media media = new Media();
			media.load(id);
			
			File f = new File(Cfg.MEDIA_PATH);	
			auxId = id;
			
			File[] fileList = f.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(auxId+"_") || name.startsWith(auxId+".");
				}
			});
			
			for (File fileToDelete : fileList) {
				if (fileToDelete.exists()) fileToDelete.delete();
			}
			
			media.delete();
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
		
		return "success";
	}
	
	public String addMediaFile(InputStream in, String filePath) {
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
			File f = new File(Cfg.MEDIA_PATH+filePath);
			
			OutputStream fout = new FileOutputStream(f);
			   
			byte[] buffer = new byte[1024*1024]; 
			int len = 0;
			while((len=in.read(buffer))!=-1) {
				fout.write(buffer, 0, len);
				fout.flush();
			}
			
			fout.close();
			
			Media media = new Media();
			media.setPath(filePath);
			media.setMediaId(id);
			media.saveUpdate();
			
			convertMediaFile(id);
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
		
		return Cfg.REST_URL+"media/"+id;
	}
	
	
	public void convertMediaFile(String id) throws Exception {
		Media media = new Media();
		media.load(id);
		String mediaPath = media.getPath();
		String ext = mediaPath.split("\\.")[1];
		
		String newMediaPath = null;
		
		int profile=0;
		for (String s : Cfg.MEDIA_CONVERSION_PROFILES) {
			if (s.equals(ext)) {
				Media mediaMaster = new Media();
				mediaMaster.setPath(id + "_master." + ext);
				mediaMaster.setMediaId(id+"_master");
				mediaMaster.saveUpdate();
				
				newMediaPath = encodeMediaFile(mediaPath, profile);
				break;
			}
			profile++;
		}
		
		if (newMediaPath !=null) {
			media.setPath(newMediaPath);
			media.saveUpdate();
		}
	}
	
	public String uploadObject(String className, String about, String[] properties, String[] propertyValues) {
		String result = "error";
		VirtTransactionHandler vth = null;
		if (className==null) return "error";
		if (about==null) about = className.substring(className.indexOf(":")+1);
		
		try {
			
			model = ModelUtil.getModel();
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelUtil.getOntology());
			
			String[] cls = className.split(",");
			String id = generateObjectId(about);
			
			String fullId = Cfg.RESOURCE_URI_NS  + id;
			
			int i = 0;
			
			script = new ArrayList<String>();
			for (String classNameElement : cls) 
				script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+fullId+"> rdf:type "+classNameElement+" } ");
			
			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			
			List<String> lcp = new ArrayList<String>(new Request().listClassPropertiesSimple(className));
			while(i<properties.length) {
				boolean isObjectProperty = false;
				
				if (!lcp.contains(properties[i]) && !"ac:FatacId".equals(properties[i]) || "type".equals(properties[i])) { i++; continue; }
				
				String qualifiedProperty = Cfg.fromPrefixToNamespace(properties[i].split(":")[0])+properties[i].split(":")[1];
				for(ObjectProperty op : lop) {
					if (op.toString().equals(qualifiedProperty)) {
						isObjectProperty = true;
						break;
					}
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) 
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+fullId+"> "+properties[i].trim()+" <"+Cfg.RESOURCE_URI_NS+propertyValues[i]+"> }");
					else {
						String lang = null;
						
						for (String l : Cfg.LANGUAGE_LIST) {
							if (propertyValues[i].endsWith("@"+l)) {
								lang = "@"+l;
								break;
							}
						}
						
						if (lang!=null) propertyValues[i] = propertyValues[i].substring(0, propertyValues[i].length()-3);
						
						propertyValues[i] = propertyValues[i].replace('"', '\'').replace('\n', ' ').replace('\t', ' ');
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+fullId+"> "+properties[i].trim()+" \"" + propertyValues[i] + "\""+(lang!=null?lang:"")+" }");
					}
				}
				
				i++;
			}
			
			Command c = new Command() {
				@Override
				public Object execute() {
					for (String s : script) {
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(model.getGraph()))).exec();
					}
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)model.getGraph());
			vth.begin();
			vth.executeInTransaction(c);
			vth.commit();
			
			result = id;
			
			try {
				if (id!=null) ResourceStatistics.creation(id);
			} catch (Exception e) {
				log.warn("Could not create statistics for object " + id, e);
			}
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
			
			model = ModelUtil.getModel();
			
			script = new ArrayList<String>();
			script.add("DELETE FROM <" + Cfg.RESOURCE_URI_NS + "> { ?a ?b ?c } WHERE { ?a ?b ?c FILTER (?a = <"+Cfg.RESOURCE_URI_NS+objectId+"> or ?c = <"+Cfg.RESOURCE_URI_NS+objectId+">) . ?a ?b ?c }");
			
			Command c = new Command() {
				@Override
				public Object execute() {
					for (String s : script)
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(model.getGraph()))).exec();
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)model.getGraph());
			vth.begin();
			vth.executeInTransaction(c);
			vth.commit();
			
			Right right = new Right();
			right.setObjectId(objectId);
			right.delete();
			
			result = "success";
			
			ResourceStatistics.deletion(objectId);
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
			model = ModelUtil.getModel();
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelUtil.getOntology());
			int i = 0;
			
			script = new ArrayList<String>();

			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			while(i<properties.length) {
				if ("rdf:type".equalsIgnoreCase(properties[i])) {
					i++;
					continue;
				}
				boolean isObjectProperty = false;
				
				String qualifiedProperty = Cfg.fromPrefixToNamespace(properties[i].split(":")[0])+properties[i].split(":")[1];
				for(ObjectProperty op : lop) {
					if (op.toString().equals(qualifiedProperty)) {
						isObjectProperty = true;
						break;
					}
				}
				
				if (!"".equals(propertyValues[i]) && !alreadyDeleted.contains(properties[i])) {
					script.add("DELETE FROM <" + Cfg.RESOURCE_URI_NS + "> { ?a ?b ?c } WHERE { ?a "+properties[i]+" ?c FILTER (?a = <" + Cfg.RESOURCE_URI_NS+uniqueId + "> ) . ?a ?b ?c }");
					alreadyDeleted.add(properties[i]);
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) {
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+Cfg.RESOURCE_URI_NS+uniqueId+"> "+properties[i]+" <"+Cfg.RESOURCE_URI_NS+propertyValues[i]+"> }");
					} else {
						String lang = null;
						
						for (String l : Cfg.LANGUAGE_LIST) {
							if (propertyValues[i].endsWith("@"+l)) {
								lang = "@"+l;
								break;
							}
						}
						
						if (lang!=null) propertyValues[i] = propertyValues[i].substring(0, propertyValues[i].length()-3);
						
						propertyValues[i] = propertyValues[i].replace('"', '\'').replace('\n', ' ').replace('\t', ' ');
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+Cfg.RESOURCE_URI_NS+uniqueId+"> "+properties[i]+" \"" + propertyValues[i] + "\""+(lang!=null?lang:"")+" }");
					}
				}
				i++;
			}
			
			Command c = new Command() {
				@Override
				public Object execute() {
					for (String s : script)
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(model.getGraph()))).exec();
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)model.getGraph());
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
	
	public String mixAndExportOntologyAndData() {
		VirtTransactionHandler vth = null;
		
		try {
			model = ModelUtil.getModel();
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF, ModelUtil.getOntology());
			
			script = new ArrayList<String>();
			
			StmtIterator it = ont.listStatements();
			while(it.hasNext()) {
				Statement stmt = it.next();
				Resource  subject   = stmt.getSubject();     // get the subject
			    Property  predicate = stmt.getPredicate();   // get the predicate
			    RDFNode   object    = stmt.getObject();      // get the object
			    
			    if (!object.isLiteral())
			    	script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+subject.toString()+"> <"+predicate.toString()+"> <"+object.toString()+"> } ");
			    else {
			    	String value = object.toString().replaceAll("\\n", "\\s");
			    	script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+subject.toString()+"> <"+predicate.toString()+"> \""+value+"\" } ");
			    }
			}
			
			Command c = new Command() {
				@Override
				public Object execute() {
					for (String s : script) {
						System.out.println(s);
						VirtuosoUpdateFactory.create(s, ((VirtGraph)(model.getGraph()))).exec();
					}
					return null;
				}
			};
			
			vth = new VirtTransactionHandler((VirtGraph)model.getGraph());
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
	
	public void reset(boolean all) throws Exception {
		
		try {
			// reloads ontology
			ModelUtil.resetOntology();

			if (all) {
				// removes model stored data
				ModelUtil.resetModel();
				Media.clear();
				IdentifierCounter.clear();
				ResourceStatistics.clear();
				
				// removes all media files 
				File f = new File(Cfg.MEDIA_PATH);
				for(String x : f.list()) {
					File fx = new File(Cfg.MEDIA_PATH+x);
					if (fx.isFile()) fx.delete();
				}
			}
		} catch (Exception e) {
			log.error("Error ", e);
			throw e;
		}
		
	}
}
