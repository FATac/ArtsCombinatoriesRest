package org.fundaciotapies.ac.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.fundaciotapies.ac.logic.solr.SolrManager;
import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

@Path("/solr/{option}")
public class Solar {
	
	@GET
	@Produces("application/json")
	public String solarize(@Context HttpServletRequest request, @PathParam("option") String option, @QueryParam("s") String searchText, @QueryParam("f") String filter, @QueryParam("start") String start, @QueryParam("rows") String rows) {
		try {
			SolrManager solr = new SolrManager();
			
			String lang = new Request().getCurrentLanguage(request);
			
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
				return solr.search(searchText, filter, start, rows, lang);
			} else if ("autocomplete".equals(option)) {
				return solr.autocomplete(searchText, start, rows, lang);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Gson().toJson("error");
		}
		
		return new Gson().toJson("success");
	}
}
