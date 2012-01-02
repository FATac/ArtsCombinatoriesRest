package org.fundaciotapies.ac.logic.solr;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.model.support.DataMapping;
import org.fundaciotapies.ac.model.support.Mapping;

import com.google.gson.Gson;

public class SearchConfigurations {

	public String listSearchConfigurations() throws Exception {
		try {
			Mapping mapping = new Gson().fromJson(new FileReader(Cfg.CONFIGURATIONS_PATH+"mapping/search.json"), Mapping.class);
			for(DataMapping m : mapping.getData()) {
				m.setSort(null);
				m.setFilter(null);
				m.setValue(null);
				m.setType(null);
			}
			return new Gson().toJson(mapping);
		} catch (FileNotFoundException e) {
			return new Gson().toJson("No search configurations");
		}
		
	}
}
