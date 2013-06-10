package org.fundaciotapies.ac.oai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Mapping;

import com.google.gson.Gson;

public class OAIFilesGenerator {
	
	public Map<String, CustomMap> documents = null;
	
	private void createDocumentEntry(String id, String className, Mapping mapping) {
		CustomMap document = documents.get(id);
		if (document==null){
			document = new CustomMap();
		}
		
		for(DataMapping dataMapping : mapping.getData()) {
			if (document.get(dataMapping.getName())==null) {
				if (dataMapping.getPath()!=null) {
					for (String path : dataMapping.getPath()) {
						String currentClassName = path.split(Cfg.PATH_PROPERTY_PREFIX)[0].trim();
						if (className.equals(currentClassName) || "*".equals(currentClassName)) {
							String[] result = new Request().resolveModelPath(path, id, false, true, false, true);
							for (String resultString : result) {
								if (resultString!=null){
									document.put(dataMapping.getName(), resultString);
								}
							}
						}
					}
				}
			}
		}
		
		documents.put(id, document);
	}
	
	
	public void generate() throws Exception {
		documents = new HashMap<String, CustomMap>();
		
		Request request = new Request();
		BufferedReader fin = new BufferedReader(new FileReader(Cfg.CONFIGURATIONS_PATH + "mapping/oai-mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		fin.close();
		
		
		Set<String> objectTypesIndexed = new TreeSet<String>();
		
		for(DataMapping m : mapping.getData()) {
			if (m.getPath()!=null) {
				for (String path : m.getPath()) {
					String className = path.split(Cfg.PATH_PROPERTY_PREFIX)[0].trim();
					if (!"*".equals(className) && !objectTypesIndexed.contains(className)){
						objectTypesIndexed.add(className);
					}
				}
			}
		}
		
		for(String className : objectTypesIndexed) {
			List<String> list = request.listObjectsId(className);
			for(String id : list){
				createDocumentEntry(id, className, mapping);
			}
		}
		
		
		for(Map.Entry<String, CustomMap> entry : documents.entrySet()) {
			String id = entry.getKey();
			CustomMap document = entry.getValue();
			
			File f = new File(Cfg.OAI_PATH + id + ".xml");
			FileWriter fw = new FileWriter(f);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			fw.write(mapping.getXmlHeader()+"\n");
			
			for(Map.Entry<String, Object> otherEntry : document.entrySet()) {
				String name = otherEntry.getKey();
				Object value = otherEntry.getValue();
				if (value instanceof String) {
					fw.write("<"+mapping.getXmlPrefix()+":"+name+">");
					fw.write("<![CDATA["+value+"]]>");
					fw.write("</"+mapping.getXmlPrefix()+":"+name+">\n");
				} else if (value instanceof String[]) {
					for(String v : (String[])value) {
						fw.write("<"+mapping.getXmlPrefix()+":"+name+">");
						fw.write("<![CDATA["+v+"]]>");
						fw.write("</"+mapping.getXmlPrefix()+":"+name+">\n");
					}
				}
			}
			
			fw.write(mapping.getXmlFooter());
			fw.close();
		}
	}

}
