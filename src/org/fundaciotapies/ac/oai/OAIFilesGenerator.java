package org.fundaciotapies.ac.oai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Mapping;
import org.fundaciotapies.ac.model.support.OrderedName;
import org.fundaciotapies.ac.model.support.OrderedNameComparator;
import org.fundaciotapies.ac.model.support.StringListHashMap;

import static org.fundaciotapies.ac.model.support.StringListHashMap.convertToArrayList;

import com.google.gson.Gson;

public class OAIFilesGenerator {

	/**
	 * Creates a new document with all the descriptions and path values
	 * 
	 * @param id
	 * @param className
	 * @param mapping
	 * @param documents
	 * @return
	 */
	private Map<String, StringListHashMap<OrderedName>> createDocumentEntry(String id,
			String className, Mapping mapping, Map<String, StringListHashMap<OrderedName>> documents) {
		StringListHashMap<OrderedName> document = documents.get(id);
		if (document == null) {
			document = new StringListHashMap<OrderedName>();
		}

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
		}

		documents.put(id, document);
		return documents;
	}

	/**
	 * Formats a description with the path info
	 * 
	 * @param id
	 * @param className
	 * @param document
	 * @param dataMapping
	 */
	private void addFormattedDescriptionWithPath(String id, String className,
			StringListHashMap<OrderedName> document, DataMapping dataMapping) {
		List<String> allResults = obtainPathValues(id, className, dataMapping);
		String descriptionFormatted = MessageFormat.format(dataMapping.getDescription(), allResults.toArray());
		putInDocument(document, dataMapping, descriptionFormatted);
	}

	/**
	 * Obtains the path values from Virtuoso
	 * 
	 * @param id
	 * @param className
	 * @param dataMapping
	 * @return
	 */
	private List<String> obtainPathValues(String id, String className,
			DataMapping dataMapping) {
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
		return allResults;
	}

	/**
	 * Adds a new String to the document
	 * 
	 * @param document
	 * @param dataMapping
	 * @param mapValue
	 */
	private void putInDocument(StringListHashMap<OrderedName> document,
			DataMapping dataMapping, String mapValue) {
		document.put(new OrderedName(dataMapping.getName(), dataMapping.getOrder()), convertToArrayList(mapValue));
	}

	/**
	 * Used when the mode is description
	 * 
	 * @param document
	 * @param dataMapping
	 */
	private void addDescriptionToDocument(StringListHashMap<OrderedName> document,
			DataMapping dataMapping) {
		putInDocument(document, dataMapping, dataMapping.getDescription());
	}

	/**
	 * Adds all non-null paths to the document.
	 * 
	 * If all are null, do not add them.
	 * 
	 * @param id
	 * @param className
	 * @param document
	 * @param dataMapping
	 */
	private void addAllPathsToDocument(String id, String className,
			StringListHashMap<OrderedName> document, DataMapping dataMapping) {
		List<String> allResults = obtainPathValues(id, className, dataMapping);
		ArrayList<String> notNullResults = new ArrayList<String>();
		CollectionUtils.select(allResults, notNullPredicate(), notNullResults);
		if (!notNullResults.isEmpty()){
			document.put(new OrderedName(dataMapping.getName(), dataMapping.getOrder()), notNullResults);
		}
	}

	/**
	 * Selects elements which aren't null
	 * @return
	 */
	private Predicate notNullPredicate() {
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object elementInList) {
				return elementInList != null;
			}
		};
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

	/**
	 * Generate the XML files for the given mapping
	 * 
	 * @throws Exception
	 */
	public void generate() throws Exception {
		Map<String, StringListHashMap<OrderedName>> documents = new HashMap<String, StringListHashMap<OrderedName>>();

		Request request = new Request();
		BufferedReader fin = new BufferedReader(new FileReader(
		 Cfg.CONFIGURATIONS_PATH  + "mapping/oai-mapping.json"));
		Mapping mapping = new Gson().fromJson(fin, Mapping.class);
		fin.close();

		Set<String> objectTypesIndexed = new TreeSet<String>();

		for (DataMapping m : mapping.getData()) {
			if (pathExistsOnMapping(m)) {
				for (String path : m.getPath()) {
					String className = path.split(Cfg.PATH_PROPERTY_PREFIX)[0]
							.trim();
					if (classNameIsNotIndexed(objectTypesIndexed, className)) {
						objectTypesIndexed.add(className);
					}
				}
			}
		}

		for (String className : objectTypesIndexed) {
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

	/**
	 * Writes the XML file
	 * 
	 * @param documents
	 * @param mapping
	 * @throws IOException
	 */
	private void writeXmlFile(Map<String, StringListHashMap<OrderedName>> documents, Mapping mapping)
			throws IOException {
		for (Map.Entry<String, StringListHashMap<OrderedName>> entry : documents.entrySet()) {
			String id = entry.getKey();
			StringListHashMap<OrderedName> document = entry.getValue();

			File f = new File(
			 Cfg.OAI_PATH + id + ".xml");
			FileWriter fw = new FileWriter(f);
			writeXmlHeader(fw, mapping);
			
			List<OrderedName> entries = new ArrayList<OrderedName>(document.keySet());
			Collections.sort(entries, new OrderedNameComparator());

			for (OrderedName documentKey: entries) {
				ArrayList<String> values = document.get(documentKey);
				writeXmlValueTag(mapping, fw, documentKey.getName(), values);
			}

			writeXmlFooter(fw, mapping);
			fw.close();
		}
	}

	private void writeXmlValueTag(Mapping mapping, FileWriter fw, String name,
			List<String> values) throws IOException {
		String xmlTag;
		if (StringUtils.isNotBlank(mapping.getXmlPrefix())) {
			xmlTag = mapping.getXmlPrefix() + ":" + name;
		} else {
			xmlTag = name;
		}
		for (String value : values) {
			writeXmlLine(fw, value, xmlTag);
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
