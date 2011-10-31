package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.logic.solr.SolrManager;

import com.google.gson.Gson;

@Path("/solr/{option}")
public class Solar {
	@GET
	@Produces("application/json")
	public String solarize(@PathParam("option") String option, @QueryParam("s") String searchText, @QueryParam("start") String start, @QueryParam("rows") String rows) {
		try {
			SolrManager solr = new SolrManager();
			
			if ("commit".equals(option)) {
				solr.commit();
			} else if ("clear".equals(option)) {
				solr.deleteAll();
			} else if ("schema".equals(option)) {
				solr.generateSchema();
			} else if ("update".equals(option)) {
				solr.indexate();
			} else if ("reload".equals(option)) {
				solr.deleteAll();
				solr.indexate();
			} else if ("search".equals(option)) {
				return solr.search(searchText, start, rows);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Gson().toJson("error");
		}
		
		return new Gson().toJson("success");
	}
}
