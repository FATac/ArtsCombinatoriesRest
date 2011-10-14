package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.logic.solr.SolrManager;

@Path("/solr/{option}")
public class Solarize {
	@GET
	@Produces("text/plain")
	public String solarize(@PathParam("option") String option, @QueryParam("s") String searchText) {
		try {
			if ("commit".equals(option)) {
				new SolrManager().commit();
			} else if ("clear".equals(option)) {
				new SolrManager().deleteAll();
			} else if ("schema".equals(option)) {
				new SolrManager().generateSchema();
			} else if ("indexate".equals(option)) {
				new SolrManager().indexate();
			} else if ("search".equals(option)) {
				return new SolrManager().search(searchText);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return "success";
	}
}
