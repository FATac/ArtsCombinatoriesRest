package org.fundaciotapies.ac.logic.indexation;

import java.io.File;
import java.io.FileReader;

import com.google.gson.Gson;
import com.hp.hpl.jena.graph.query.Mapping;

public class SolrIndexation {
		
	public void start() throws Exception {
		FileReader f = new FileReader(new File("/home/jordi.roig.prieto/workspace/ArtsCombinatoriesRest/json/mapping/mapping.json")); // TODO: get uri from configuration
		Mapping mapping = new Gson().fromJson(f, Mapping.class);
		

		 
	}
}
