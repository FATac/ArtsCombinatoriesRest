package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.logic.solr.SearchConfigurations;
import org.fundaciotapies.ac.logic.solr.SolrManager;
import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("/solr/{option}")
public class Solar {
	private static Logger log = Logger.getLogger(Solar.class);
	
	@GET
	@Produces("application/json")
	public String solarize(@PathParam("option") String option, @QueryParam("s") String searchText, @QueryParam("f") String filter, @QueryParam("start") String start, @QueryParam("rows") String rows, @QueryParam("conf") String searchConfig, @QueryParam("sort") String sort, @QueryParam("time") String time, @QueryParam("lang") String lang, @QueryParam("fields") String fields) {
		try {
			SolrManager solr = new SolrManager();
			
			if ("commit".equals(option)) {
				solr.commit();
			} else if ("clear".equals(option)) {
				solr.deleteAll();
			} else if ("schema".equals(option)) {
				solr.generateSchema();
				return new Gson().toJson("success: restart web application server.");
			} else if ("update".equals(option)) {
				if (time==null || "".equals(time)) time = "60";
				solr.index(new Request().listRecentChanges(time));
			} else if ("reload".equals(option)) {
				solr.deleteAll();
				solr.index();
			} else if ("reloadLast".equals(option)) {
				solr.deleteAll();
				solr.indexLast();
			} else if ("search".equals(option)) {
				return solr.search(searchText, filter, start, rows, lang, searchConfig, sort, fields);
			} else if ("autocomplete".equals(option)) {
				return solr.autocomplete(searchText, start, rows, lang, searchConfig);
			} else if ("configurations".equals(option)) {
				return new SearchConfigurations().listSearchConfigurations();
			}
		} catch (Exception e) {
			log.error("Error ", e);
			return new Gson().toJson("error");
		}
		
		return new Gson().toJson("success");
	}
}
