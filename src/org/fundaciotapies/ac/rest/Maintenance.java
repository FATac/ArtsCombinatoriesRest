package org.fundaciotapies.ac.rest;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;
import org.fundaciotapies.ac.logic.solr.SolrManager;
import org.fundaciotapies.ac.oai.OAIFilesGenerator;

@Path("/maintenance")
public class Maintenance {
	private final static Logger log = Logger.getLogger(Maintenance.class);

	@GET
	@Produces("application/json")
	public String maintenance() {
		
		try {
			File f = new File(Cfg.CONFIGURATIONS_PATH + "legal");
			File[] fileList = f.listFiles();
			for (File fx : fileList) {
				String name = fx.getName();
				if (name.matches("user_[0-9]+.properties")) fx.delete();
			}
			
			f = new File(Cfg.MEDIA_PATH + "tmp");
			fileList = f.listFiles();
			for (File fx : fileList) {
				fx.delete();
			}
			
			if (Cfg.SOLR_PATH!=null) {
				SolrManager solr = new SolrManager();
				solr.deleteAll();
				solr.index();
			}
			
			if (Cfg.OAI_PATH!=null) {
				OAIFilesGenerator oai = new OAIFilesGenerator();
				oai.generate();
			}
		} catch (Exception e) {
			log.error("Error running maintenance ", e);
			return "error";
		}
		
		return "success";
	}
}
