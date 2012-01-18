package org.fundaciotapies.ac.model;

import org.fundaciotapies.ac.Cfg;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtInfGraph;
import virtuoso.jena.driver.VirtModel;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.InfGraph;

public class ModelUtil {

	private static InfModel model = null;
	private static OntModel ontology = null;
	
	/*
	 * Gets Ontology graph from Virtuoso and reuses it everytime it's requested 
	 */
	public static OntModel getOntology() {
		if (ontology!=null && !ontology.isClosed()) return ontology;
		VirtModel vm = VirtModel.openDatabaseModel(Cfg.ONTOLOGY_NAMESPACES[0], Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF, vm);
		return ontology;
	}
	
	/*
	 * Gets data graph from Virtuoso and reuses it everytime it's requested
	 */
	public static InfModel getModel() {
		if (model!=null && !model.isClosed()) return model;
		VirtModel vm = VirtModel.openDatabaseModel(Cfg.ONTOLOGY_NAMESPACES[0], Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		vm.createRuleSet("ac_rules", Cfg.ONTOLOGY_NAMESPACES[0]);
		InfGraph igr = new VirtInfGraph("ac_rules", true, Cfg.RESOURCE_URI_NS, Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		model = ModelFactory.createInfModel(igr);
		return model;
	}
	
	public static void resetAll() {
		resetModel();
		resetOntology();
	}
	
	/*
	 * Reset all ontologies specified in Configuration
	 * by loading them, clearing and reloading
	 */
	public static void resetOntology() {
		for (int i=0;i<Cfg.ONTOLOGY_NAMESPACES.length;i+=2){
			VirtGraph ont = new VirtGraph(Cfg.ONTOLOGY_NAMESPACES[i], Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
			ont.clear();
			ont.read(Cfg.ONTOLOGY_NAMESPACES[i], null);
			ont.close();
		}
		// close current Virtuoso ontology to ensure it will be reloaded on next request 
		if (ontology!=null) ontology.close();
	}
	
	/*
	 * Reset data by loading its Virtuoso graph and clearing
	 */
	public static void resetModel() {
		VirtGraph res = new VirtGraph(Cfg.RESOURCE_URI_NS, Cfg.RDFDB_URL, Cfg.RDFDB_USER, Cfg.RDFDB_PASS);
		res.clear();
		res.close();
		// close current data graph to ensure reload on next request
		if (model!=null) model.close();
	}	
	
}
