package org.fundaciotapies.ac.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.Constants;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtInfGraph;
import virtuoso.jena.driver.VirtModel;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.InfGraph;

public class ModelUtil {
	private static Logger log = Logger.getLogger(ModelUtil.class);

	private static InfModel model = null;
	private static VirtModel ontology = null;
	
	public static VirtModel getOntology() {
		if (ontology!=null && !ontology.isClosed()) return ontology;
		ontology = VirtModel.openDatabaseModel(Constants.ONTOLOGY_URI_NS, Constants.RDFDB_URL, Constants.RDFDB_USER, Constants.RDFDB_PASS);
		return ontology;
	}
	
	public static InfModel getModel() {
		if (model!=null && !model.isClosed()) return model;
		
		getOntology().createRuleSet("ac_rules", Constants.ONTOLOGY_URI_NS);
		InfGraph igr = new VirtInfGraph("ac_rules", true, Constants.RESOURCE_URI_NS, Constants.RDFDB_URL, Constants.RDFDB_USER, Constants.RDFDB_PASS);
		model = ModelFactory.createInfModel(igr);
		
		return model;
	}
	
	public static void reset() {
		try {
			new VirtGraph(Constants.RESOURCE_URI_NS, Constants.RDFDB_URL, Constants.RDFDB_USER, Constants.RDFDB_PASS).clear();
			Connection conn = DriverManager.getConnection(Constants.RDFDB_URL, Constants.RDFDB_USER, Constants.RDFDB_PASS);
			Statement stmt = conn.createStatement();
			stmt.execute("DB.DBA.RDF_LOAD_RDFXML (http_get('"+Constants.ONTOLOGY_URI_NS+"'), '', '"+Constants.ONTOLOGY_URI_NS+"'); ");
			stmt.close();
			conn.close();
		} catch (Exception e) {
			log.error("Error ", e);
		}
	}
}
