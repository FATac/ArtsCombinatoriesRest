package org.fundaciotapies.ac.rest;

import java.io.FileReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Cfg;

@Path("/ontology/{prefix}")
public class GetOntology {
	private static Logger log = Logger.getLogger(GetOntology.class);

	@GET
	@Produces("application/xml")
	public String getOntology(@PathParam("prefix") String p) {
		try {
			if (p.equals(Cfg.ONTOLOGY_PREFIX+"#") 
					|| p.equals(Cfg.ONTOLOGY_PREFIX)) {
				StringBuffer sb = new StringBuffer();
				FileReader in = new FileReader(Cfg.ONTOLOGY_PATH);
				char[] buf = new char[1024];
				int len = 0;
				while((len=in.read(buf))>0) {
					sb.append(buf, 0, len);
				}
				
				return sb.toString();
			} else throw new Exception("Wrong prefix");
		} catch (Exception e) {
			log.error("Error ", e);
			return null;
		}
	}
}
