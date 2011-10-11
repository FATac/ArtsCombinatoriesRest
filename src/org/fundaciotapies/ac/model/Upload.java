package org.fundaciotapies.ac.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;
import org.fundaciotapies.ac.model.bo.Media;
import org.fundaciotapies.ac.model.bo.ObjectCounter;
import org.fundaciotapies.ac.model.bo.Right;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.rest.client.Profile;
import org.fundaciotapies.ac.rest.client.Transco;
import org.fundaciotapies.ac.rest.client.TranscoEntity;
import org.jsoup.Jsoup;

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
import com.hp.hpl.jena.shared.Command;

public class Upload {
	private static Logger log = Logger.getLogger(Upload.class);
	
	public List<String> script = null;
	public OntModel data = null;
	
	private String generateObjectId(String className) throws Exception {
		if (className == null) 
			throw new NullPointerException();
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
			
			filePath = id.replace("/", "")+"___file."+ext;
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
			e.printStackTrace();
			return "error";
		}
		
		return "success";
	}
	
	private String extractUriId(String URI) {
		return URI.replace(Constants.OBJECT_BASE_URI, "").replace(Constants.RDFS_URI_NS, "").replace(Constants.AC_URI_NS, "");
	}
	
	public String uploadObject(String className, String[] properties, String[] propertyValues) {
		String result = "error";
		VirtTransactionHandler vth = null;
		if (className==null) return "error";
		
		try {
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData", Constants.RDFDB_URL, "dba", "dba"));
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
			ont.read("file:OntologiaArtsCombinatories.owl");
			
			String[] cls = className.split(",");
			String id = generateObjectId(cls[0]);
			String fullId = Constants.OBJECT_BASE_URI + id;
			
			int i = 0;
			
			script = new ArrayList<String>();
			for (String classNameElement : cls) 
				script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> rdfs:Class <"+Constants.AC_URI_NS+classNameElement+"> } ");
			
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
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+fullId+"> <"+Constants.AC_URI_NS+properties[i].trim()+"> <"+Constants.OBJECT_BASE_URI+propertyValues[i]+"> }");
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
			
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData", Constants.RDFDB_URL, "dba", "dba"));
			
			script = new ArrayList<String>();
			script.add("DELETE FROM <http://localhost:8890/ACData> { ?a ?b ?c } WHERE { ?a ?b ?c FILTER (?a = <"+Constants.OBJECT_BASE_URI+objectId+"> or ?c = <"+Constants.OBJECT_BASE_URI+objectId+">) . ?a ?b ?c }");
			
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
			data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, VirtModel.openDatabaseModel("http://localhost:8890/ACData", Constants.RDFDB_URL, "dba", "dba"));
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
					script.add("DELETE FROM <http://localhost:8890/ACData> { ?a ?b ?c } WHERE { ?a <"+Constants.AC_URI_NS+properties[i]+"> ?c FILTER (?a = <"+Constants.OBJECT_BASE_URI+uniqueId+">) . ?a ?b ?c }");
					alreadyDeleted.add(properties[i]);
				}
				
				if (!"".equals(propertyValues[i]) && propertyValues[i]!=null) {
					if (isObjectProperty) {
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+Constants.OBJECT_BASE_URI+uniqueId+"> <"+Constants.AC_URI_NS+properties[i]+"> <"+Constants.OBJECT_BASE_URI+propertyValues[i]+"> }");
					} else {
						script.add("INSERT INTO GRAPH <http://localhost:8890/ACData> { <"+Constants.OBJECT_BASE_URI+uniqueId+"> <"+Constants.AC_URI_NS+properties[i]+"> \"" + propertyValues[i] + "\" }");
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
	
	private List<String> listUtil(Object o) {
		if (o==null) return null;
		List<String> l = new ArrayList<String>();
		if (o instanceof String) {
			l.add((String)o);
		} else {
			for(String s : (String[])o) l.add(s);
		}
		
		return l;
	}
	
	public String solarize() throws Exception {
		
		Request request = new Request();
		Map<String, CustomMap> result = request.listObjects("Case-Files");
		Set<Map.Entry<String, CustomMap>> set = result.entrySet();
		
		String xml = "<add>\n";
		
		for (Map.Entry<String, CustomMap> e : set) {
			String id = e.getKey();
			CustomMap data = e.getValue();
			Object v = data.get("references");
			
			String referenceId = "";
			if (v instanceof String[]) { referenceId = ((String[])v)[0]; } else { referenceId = (String)v; }
			if (referenceId!=null) {
				referenceId = extractUriId(referenceId);
				
				CustomMap event = request.getObject(referenceId, "");
				List<String> titles = listUtil(event.get("Title"));
				List<String> desc = listUtil(event.get("Description"));
				
				xml += "<doc>\n";
				xml += "  <field name='id'>"+id+"</field>\n";
				if (titles!=null) 
					for (String t : titles)	xml += "  <field name='title'><![CDATA["+Jsoup.parse(t).text()+"]]></field>\n";
				else xml += "  <field name='title'> </field>\n";
					
				if (desc!=null) for (String d : desc)	xml += "  <field name='description'><![CDATA["+Jsoup.parse(d).text()+"]]></field>\n";
				xml += "</doc>\n";
			}
		}
		
		xml += "</add>\n";
		
		// Connect
		URL url = new URL("http://localhost:8080/solr/update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Send data
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(xml);
	    wr.flush();
	    wr.close();

	    // Get the response
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String str;
	    StringBuffer sb = new StringBuffer();
	    while ((str = rd.readLine()) != null) {
	    	sb.append(str);
	    	sb.append("\n");
	    }

	    log.info(sb.toString());
	    rd.close();
	    
	    return xml;
	}
	
	public void solrCommit() throws Exception {
		// Connect
		URL url = new URL("http://localhost:8080/solr/update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Send data
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write("<commit/>");
	    wr.flush();
	    wr.close();

	    // Get the response
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String str;
	    StringBuffer sb = new StringBuffer();
	    while ((str = rd.readLine()) != null) {
	    	sb.append(str);
	    	sb.append("\n");
	    }

	    log.info(sb.toString());
	    rd.close();
	}
	
	public void solrDeleteAll() throws Exception {
		// Connect
		URL url = new URL("http://localhost:8080/solr/update");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestProperty("Content-Type", "application/xml");
	    conn.setRequestMethod("POST");

	    // Send data
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write("<delete><query>*:*</query></delete>");
	    wr.flush();
	    wr.close();

	    // Get the response
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String str;
	    StringBuffer sb = new StringBuffer();
	    while ((str = rd.readLine()) != null) {
	    	sb.append(str);
	    	sb.append("\n");
	    }

	    log.info(sb.toString());
	    rd.close();
	}

}
