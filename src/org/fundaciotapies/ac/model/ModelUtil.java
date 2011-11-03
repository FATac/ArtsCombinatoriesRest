package org.fundaciotapies.ac.model;

import org.fundaciotapies.ac.Cfg;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtInfGraph;
import virtuoso.jena.driver.VirtModel;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.InfGraph;
public class ModelUtil {

	private static InfModel model = null;
	private static VirtModel ontology = null;
	
	public static VirtModel getOntology() {
		if (ontology!=null && !ontology.isClosed()) return ontology;
		ontology = VirtModel.openDatabaseModel(Cfg.ONTOLOGY_URI_NS, Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		return ontology;
	}
	
	public static InfModel getModel() {
		if (model!=null && !model.isClosed()) return model;
		getOntology().createRuleSet("ac_rules", Cfg.ONTOLOGY_URI_NS);
		InfGraph igr = new VirtInfGraph("ac_rules", true, Cfg.RESOURCE_URI_NS, Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		model = ModelFactory.createInfModel(igr);
		return model;
	}
	
	public static void resetAll() {
		resetModel();
		resetOntology();
	}
	
	public static void resetOntology() {
		VirtGraph ont = new VirtGraph(Cfg.ONTOLOGY_URI_NS, Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		ont.clear();
		ont.read(Cfg.ONTOLOGY_URI_NS, null);
		ont.close();
		if (ontology!=null) ontology.close();
	}
	
	public static void resetModel() {
		VirtGraph res = new VirtGraph(Cfg.RESOURCE_URI_NS, Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		res.clear();
		res.close();
		if (model!=null) model.close();
	}
}
