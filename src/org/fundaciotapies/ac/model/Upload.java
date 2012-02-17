package org.fundaciotapies.ac.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
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
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.shared.Command;

public class Upload {
	private static Logger log = Logger.getLogger(Upload.class);
	
	public List<String> script = null;
	
	public InfModel model = null;
	
	/*
	 * Transform any string to an identifier
	 */
	private String normalizeId(String about) throws Exception {
		String temp = Normalizer.normalize(about.trim(), Normalizer.Form.NFD);
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+"); // remove diacritical marks (accents, etc.)
	    
	    // remove anything that is not a letter, nubmer or space form, and transform all space forms to '_' 
	    String result = pattern.matcher(temp).replaceAll("").replaceAll("\\<.*?>","").replaceAll("[^A-Za-z0-9_\\s]", "").replaceAll("[\\s+\\n+\\t+]", "_");
	    if (result!=null && !"".equals(result)) {
	    	// number-starting strings cannot be identifiers so add a starting character
	    	if ("0123456789".contains(result.charAt(0)+"")) result = "_" + result;
	    } else {
	    	// if no identifier is left after normalization, use a predefined string
	    	result = "Unidentified";
	    }
	    // cut too long ids
	    if (result.length()>140) result = result.substring(0, 140);
	    return result;
	}
	
	/*
	 * Generate identifier ensuring that its unique
	 */
	private String generateObjectId(String about) throws Exception {
		if (about == null) throw new NullPointerException();
		IdentifierCounter oc = new IdentifierCounter();
		
		about = normalizeId(about);
		int idx = about.lastIndexOf('_');
		if (idx>-1) {
			String dumb = about.substring(idx+1);
			try {
				Integer.parseInt(dumb);
				about = about.substring(0, idx) + "_n" + dumb;
			} catch (NumberFormatException e) {	}
		}
		
		// get identifier counter
		oc.load(about);
		oc.setCounter(oc.getCounter()+1);
		if (oc.getCounter() == 1l) {
			oc.save();
		} else {
			oc.update();
		}
		
		Long n = oc.getCounter();
		if (n>1) 
			return about + "_" + n; // add counter mark if this id is already used
		else
			return about;
	}
	
	// use video services to encode media file in given profile
	public void encodeMediaFile(String sourceFilePath, String destinationFilePath, int profile) throws Exception {
		TranscoEntity transco = new TranscoEntity();
		transco.setSrc_path(Cfg.MEDIA_PATH + sourceFilePath);
		transco.setProfiles(new ArrayList<Profile>());
		Profile prof = new Profile();
		prof.setType(profile+"");
		prof.setDst_path(Cfg.MEDIA_PATH + destinationFilePath);
		transco.getProfiles().add(prof);
			
		String res = new Transco().addTransco(transco);
		log.info(res);
	}
	
	public String auxId;
	public String removeMediaFile(String id) {
		try {
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
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
		
		return "success";
	}
	
	public String addMediaFile(InputStream in, String filePath) {
		String fileName = filePath.split("\\.")[0];
		if ("".equals(fileName.trim())) fileName = "m";

		try {
			String id = normalizeId(fileName) + "_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0,15);
			
			String[] tmp = filePath.split("\\.");
			String ext = "";
			if (tmp!=null && tmp.length>0) ext = tmp[tmp.length-1];
			
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
			
		 	if (Cfg.MEDIA_AUTOCONVERT) convertMediaFile(filePath);
		 	
		 	return Cfg.MEDIA_URL+filePath;
		} catch (Exception e) {
			log.error("Error ", e);
			return "error";
		}
	}
	
	
	public void convertMediaFile(String id) throws Exception {
		id = id.replaceFirst("_master\\.","").replaceFirst("_[\\d]+\\.", "");
		
		String extension = id.substring(id.lastIndexOf('.')+1);
		String fileName = id.substring(0,id.lastIndexOf('.'));
		
		File master = null; 
		List<String> mlist = new Request().listMedia();
		for (String m : mlist) {
			if (m.contains(fileName + "_master.")) {
				master = new File(Cfg.MEDIA_PATH + m);
				break;
			}
		}
		
		// converts to ogg audio or ogg video
		String outputExtension = "ogg,oga,mp3,aif,wma,wav".contains(extension)?"oga":"ogv";
		
		int profileType=1;
		int profileNumber=0;
		
		// Iterates over media conversion formats, each position is a different profile
		for (String s : Cfg.MEDIA_CONVERSION_PROFILES) {
			// if current extension is eligible for conversion using current profile position
			if (s.contains(extension)) {
				if (master == null) {
					master = new File(Cfg.MEDIA_PATH + fileName + "_master." + extension);
					new File(Cfg.MEDIA_PATH + id).renameTo(master);
				}
				
				// output file must have the same id as the original file with profile number as prefix, with output extension
				String newMediaPath = id + (profileNumber==0?"":"_"+profileNumber)+"."+outputExtension;
				
				// call encoding service
				try {
					encodeMediaFile(master.getPath(), newMediaPath, profileType);
				} catch (Exception e) {
					log.warn("Error calling video conversion services. " + e.toString());
				}
				
				profileNumber++; // we've just used this profile number, so increment it
			}
			profileType++;
		}
	}
	
	public String uploadObject(String className, String about, String[] properties, String[] propertyValues) {
		String result = "error";
		VirtTransactionHandler vth = null;
		if (className==null) return "error";
		if (about==null || "".equals(about)) about = className.substring(className.indexOf(":")+1);
		
		try {
			
			model = ModelUtil.getModel();
			OntModel ont = ModelUtil.getOntology();
			
			String[] cls = className.split(",");
			String id = generateObjectId(about);
			
			String fullId = Cfg.RESOURCE_URI_NS  + id;
			
			int i = 0;
			
			script = new ArrayList<String>();
			for (String classNameElement : cls) 
				script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+fullId+"> rdf:type "+classNameElement+" } ");
			
			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			
			//List<String> lcp = new ArrayList<String>(new Request().listClassPropertiesSimple(className));
			while(i<properties.length) {
				boolean isObjectProperty = false;
				
				//if (!lcp.contains(properties[i]) && !"ac:FatacId".equals(properties[i]) || "type".equals(properties[i])) { i++; continue; }
				if ("rdf:type".equals(properties[i]) || !properties[i].contains(":")) { i++; continue; }
				
				String qualifiedProperty = Cfg.fromPrefixToNamespace(properties[i].split(":")[0])+properties[i].split(":")[1];
				for(ObjectProperty op : lop) {
					if (op.toString().equals(qualifiedProperty)) {
						isObjectProperty = true;
						break;
					}
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) 
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+fullId+"> "+properties[i].trim()+" <"+Cfg.RESOURCE_URI_NS+propertyValues[i].trim()+"> }");
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
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+fullId+"> "+properties[i].trim()+" \"" + propertyValues[i].trim() + "\""+(lang!=null?lang:"")+" }");
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

	/*
	 * Updates object data
	 */
	public String updateObject(String uniqueId, String[] properties, String[] propertyValues) {
		String result = "error";
		VirtTransactionHandler vth = null;
		
		Set<String> alreadyDeleted = new TreeSet<String>();
		
		try {
			model = ModelUtil.getModel();
			
			// load ontology and jena reasoner
			OntModel ont = ModelUtil.getOntology();
			int i = 0;
			
			script = new ArrayList<String>();

			// list all ontology object properties (relations)
			List<ObjectProperty> lop = ont.listObjectProperties().toList();
			while(i<properties.length) {
				if ("rdf:type".equals(properties[i]) || !properties[i].contains(":")) { i++; continue; }
				boolean isObjectProperty = false;
				
				// all property names come with prefix, they need to be converted to namespace to be compared with Jena responses
				String qualifiedProperty = Cfg.fromPrefixToNamespace(properties[i].split(":")[0])+properties[i].split(":")[1];
				for(ObjectProperty op : lop) {
					if (op.toString().equals(qualifiedProperty)) { // checks whether current property is relation (ObjectProperty)
						isObjectProperty = true;
						break;
					}
				}
				
				if (!"".equals(propertyValues[i]) && !alreadyDeleted.contains(properties[i])) {
					// first delete properties that are being modified 
					script.add("DELETE FROM <" + Cfg.RESOURCE_URI_NS + "> { ?a ?b ?c } WHERE { ?a "+properties[i]+" ?c FILTER (?a = <" + Cfg.RESOURCE_URI_NS+uniqueId + "> ) . ?a ?b ?c }");
					alreadyDeleted.add(properties[i]); // ensure that properties are only deleted once per update
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) {
						// if current property is a relation, predicate-object is a URI
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+Cfg.RESOURCE_URI_NS+uniqueId+"> "+properties[i]+" <"+Cfg.RESOURCE_URI_NS+propertyValues[i].trim()+"> }");
					} else { // otherwise, predicate-object is a literal/number/date value
						// extract language from value, if exists
						String lang = null;
						for (String l : Cfg.LANGUAGE_LIST) {
							if (propertyValues[i].endsWith("@"+l)) {
								lang = "@"+l;
								break;
							}
						}
						
						if (lang!=null) propertyValues[i] = propertyValues[i].substring(0, propertyValues[i].length()-3);
						// remove any conflictive character
						propertyValues[i] = propertyValues[i].replace('"', '\'').replace('\n', ' ').replace('\t', ' ');
						script.add("INSERT INTO GRAPH <" + Cfg.RESOURCE_URI_NS + "> { <"+Cfg.RESOURCE_URI_NS+uniqueId+"> "+properties[i]+" \"" + propertyValues[i].trim() + "\""+(lang!=null?lang:"")+" }");
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
	
	/*
	 * Resets ontologies (reloads all of them), erases model data and media files
	 * IF all is set to false, only reloads ontologies
	 */
	public void reset(int what) throws Exception {
		
		try {
			// reloads ontology
			if (what==1) {
				ModelUtil.resetOntology();
			} else if (what==2) {
				Cfg.userLevelTmp = new HashMap<String, Integer>();
			} else {
				log.info(">>>> RESETING ALL DATA (MEDIA, MODEL, TABLES) AND UPDATING ONTOLOGIES <<<<");
				// removes model stored data
				ModelUtil.resetModel();
				IdentifierCounter.clear();
				ResourceStatistics.clear();
				
				// removes all media files 
				File f = new File(Cfg.MEDIA_PATH);
				for(String x : f.list()) {
					File fx = new File(Cfg.MEDIA_PATH+x);
					if (fx.isFile()) fx.delete();
				}
				
				Cfg.userLevelTmp = new HashMap<String, Integer>();
			}
		} catch (Exception e) {
			log.error("Error ", e);
			throw e;
		}
		
	}
}
