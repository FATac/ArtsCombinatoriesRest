package org.fundaciotapies.ac.oai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.CustomMap;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Mapping;

import com.google.gson.Gson;

public class OAIFilesGenerator {

	private Map<String, CustomMap> createDocumentEntry(String id,
			String className, Mapping mapping, Map<String, CustomMap> documents) {
		CustomMap document = documents.get(id);
		if (document == null) {
			document = new CustomMap();
		}
		
		int temporalCounter = 0;

		for (DataMapping dataMapping : mapping.getData()) {
			if (document.get(dataMapping.getName()) == null) {
				if (descriptionExists(dataMapping)) {
					if (pathExists(dataMapping)){
						// build description formatted
						addFormattedDescriptionWithPath(id, className,
								document, dataMapping);
					}
					else{
						// Just description => literal
						addDescriptionToDocument(document, dataMapping);	
					}
				}
				else if (pathExists(dataMapping)) {
					addAllPathsToDocument(id, className, document, dataMapping);
				} 
			}
			if (temporalCounter++ >= 50){
				break;
			}
		}

		documents.put(id, document);
		return documents;
	}

	private void addFormattedDescriptionWithPath(String id, String className,
			CustomMap document, DataMapping dataMapping) {
		List<String> allResults = new ArrayList<String>();
		for (String path : dataMapping.getPath()) {
			String currentClassName = path
					.split(Cfg.PATH_PROPERTY_PREFIX)[0].trim();
			if (isCurrentClassNameOrWildcard(className,
					currentClassName)) {
				String[] result = new Request().resolveModelPath(
						path, id, false, true, false, true);
				allResults.addAll(Arrays.asList(result));
			}
		}
		String descriptionFormatted = MessageFormat.format(dataMapping.getDescription(), allResults.toArray());
		System.out.println("DEPA=> " + dataMapping.getName() + ": " + descriptionFormatted);
		document.put(dataMapping.getName(), descriptionFormatted);
	}

	private void addDescriptionToDocument(CustomMap document,
			DataMapping dataMapping) {
		System.out.println("DESC=> " + dataMapping.getName() + ": " + dataMapping.getDescription());
		document.put(dataMapping.getName(),
				dataMapping.getDescription());
	}

	private void addAllPathsToDocument(String id, String className,
			CustomMap document, DataMapping dataMapping) {
		for (String path : dataMapping.getPath()) {
			String currentClassName = path
					.split(Cfg.PATH_PROPERTY_PREFIX)[0].trim();
			if (isCurrentClassNameOrWildcard(className,
					currentClassName)) {
				String[] result = new Request().resolveModelPath(
						path, id, false, true, false, true);
				addResultsToDocument(document, dataMapping,
						result);
			}
		}
	}

	private void addResultsToDocument(CustomMap document,
			DataMapping dataMapping, String[] result) {
		for (String resultString : result) {
			if (resultString != null) {
				System.out.println("PATH=> " + dataMapping.getName() + ": " + resultString);
				document.put(dataMapping.getName(),
						resultString);
			}
		}
	}

	private boolean isCurrentClassNameOrWildcard(String className,
			String currentClassName) {
		return className.equals(currentClassName)
				|| "*".equals(currentClassName);
	}

	private boolean pathExists(DataMapping dataMapping) {
		return pathExistsOnMapping(dataMapping);
	}

	private boolean descriptionExists(DataMapping dataMapping) {
		return dataMapping.getDescription() != null;
	}

	public void generate() throws Exception {
		Map<String, CustomMap> documents = new HashMap<String, CustomMap>();

		Request request = new Request();
		BufferedReader fin = new BufferedReader(new FileReader(
		// Cfg.CONFIGURATIONS_PATH
				"/opt/tapies/config/" + "mapping/oai-mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		fin.close();

		Set<String> objectTypesIndexed = new TreeSet<String>();

		for (DataMapping m : mapping.getData()) {
			if (pathExistsOnMapping(m)) {
				for (String path : m.getPath()) {
					System.out.println(path);
					String className = path.split(Cfg.PATH_PROPERTY_PREFIX)[0]
							.trim();
					if (classNameIsNotIndexed(objectTypesIndexed, className)) {
						objectTypesIndexed.add(className);
					}
				}
			}
		}

		System.out.println("bucle indexed");
		for (String className : objectTypesIndexed) {
			System.out.println(className);
			List<String> list = request.listObjectsId(className);
			for (String id : list) {
				documents = createDocumentEntry(id, className, mapping,
						documents);
			}
		}

		writeXmlFile(documents, mapping);
	}

	private boolean pathExistsOnMapping(DataMapping m) {
		return m != null && m.getPath() != null;
	}

	private boolean classNameIsNotIndexed(Set<String> objectTypesIndexed,
			String className) {
		return !"*".equals(className)
				&& !objectTypesIndexed.contains(className);
	}

	private void writeXmlFile(Map<String, CustomMap> documents, Mapping mapping)
			throws IOException {
		for (Map.Entry<String, CustomMap> entry : documents.entrySet()) {
			String id = entry.getKey();
			CustomMap document = entry.getValue();

			File f = new File(
			// Cfg.OAI_PATH
					"/opt/tapies/oai" + id + ".xml");
			FileWriter fw = new FileWriter(f);
			writeXmlHeader(fw, mapping);

			for (Map.Entry<String, Object> otherEntry : document.entrySet()) {
				String name = otherEntry.getKey();
				Object value = otherEntry.getValue();
				writeXmlValueTag(mapping, fw, name, value);
			}

			writeXmlFooter(fw, mapping);
			fw.close();
		}
	}

	private void writeXmlValueTag(Mapping mapping, FileWriter fw, String name,
			Object value) throws IOException {
		String xmlTag;
		if (StringUtils.isNotBlank(mapping.getXmlPrefix())) {
			xmlTag = mapping.getXmlPrefix() + ":" + name;
		} else {
			xmlTag = name;
		}
		if (value instanceof String) {
			writeXmlLine(fw, (String) value, xmlTag);
		} else if (value instanceof String[]) {
			for (String v : (String[]) value) {
				writeXmlLine(fw, v, xmlTag);
			}
		}
	}

	private void writeXmlFooter(FileWriter fw, Mapping mapping)
			throws IOException {
		fw.write(mapping.getXmlFooter());
	}

	private void writeXmlHeader(FileWriter fw, Mapping mapping)
			throws IOException {
		fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		fw.write(mapping.getXmlHeader() + "\n");
	}

	private void writeXmlLine(FileWriter fw, String value, String xmlTag)
			throws IOException {
		fw.write("<" + xmlTag + ">");
		fw.write("<![CDATA[" + value + "]]>");
		fw.write("</" + xmlTag + ">\n");
	}

}
