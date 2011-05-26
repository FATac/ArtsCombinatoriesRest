package org.fundaciotapies.ac.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fundaciotapies.ac.logic.support.Question;
import org.fundaciotapies.ac.model.bo.Right;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Legal {
	private static Logger log = Logger.getLogger(Legal.class);
	
	private static String legal_path = "file:/home/jordi.roig.prieto/Documents/FAT/legal/";
	private static String legal_file = "legal.owl";
	private static String legal_ns = "http://fundaciotapies.org/legal.owl#";
	
	
	public void setObjectsRight(List<String> objectIdList, String color) {
		try {
			Right r = new Right();
			for (String id : objectIdList) {
				r.setObjectId(id);
				r.delete();
				
				if ("red".equals(color)) {
					r.setRightLevel(4);
				} else if ("orange".equals(color)) {
					r.setRightLevel(3);
				} else if ("yellow".equals(color)) {
					r.setRightLevel(2);
				} else {
					r.setRightLevel(1);
				}
				
				r.saveUpdate();
			}
		} catch (Exception e) {
			log.error("Exception ", e);
		}
	}
	
	public String startLegal() {
		String result = null;
		
		try {
			result = "user_" + Math.round(Math.random()*100000000);
			
			Properties prop = new Properties();
			prop.setProperty("lastQuestion", "");
			prop.setProperty("color", "orange");
			prop.setProperty("requiredData", "");
			
			prop.store(new FileOutputStream(result + ".properties"), null);
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		
		return result;
	}
	
	public Question nextQuestion(String responseId, String user) {
		Question result = new Question();
		String[] answerList = null; 
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(user + ".properties"));
			
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
			model.read(legal_path + legal_file);
			
			String lastQuestion = prop.getProperty("lastQuestion");
			Individual ind = null;
			
			if (!"".equals(lastQuestion)) {
				StmtIterator it = model.getIndividual(legal_ns + lastQuestion).listProperties(model.getProperty(legal_ns + "hasResponse"));
				Boolean ok = false;
				
				while(it.hasNext()) {
					Statement stmt = it.next();
					if (stmt.getObject().asResource().getLocalName().equals(responseId)) {
						ok = true;
						break;
					}
				}				
				if (!ok) throw new Exception("Given response does not correspond to previous question. " + responseId);
				
				ind = model.getIndividual(legal_ns + responseId);
				RDFNode colorValue = ind.getPropertyValue(model.getProperty(legal_ns + "color"));
				if (colorValue!=null) {
					prop.setProperty("color", colorValue.asLiteral().getString());	
				}
				// TODO: Falta recollir les dades requerides
			} else {
				ind = model.getIndividual(legal_ns + "StartPoint");
			}
			
			RDFNode nextQuestion = ind.getPropertyValue(model.getProperty(legal_ns + "nextQuestion"));
			if (nextQuestion!=null) {
				StmtIterator it = nextQuestion.asResource().listProperties(model.getProperty(legal_ns + "hasResponse"));
				List<Statement> list = it.toList();
				answerList = new String[list.size()];
				
				result.setQuestionId(nextQuestion.asResource().getLocalName());
				
				int i = 0;
				for (Statement stmt : list) answerList[i++] = stmt.getObject().asResource().getLocalName();
				result.setAnswerList(answerList);
				
				prop.setProperty("lastQuestion", result.getQuestionId() );
				prop.store(new FileOutputStream(user + ".properties"), null);
			} else {
				result.setColor(prop.getProperty("color"));
				File f = new File(user + ".properties");
				f.delete();
			}
			
		} catch (Exception e) {
			log.error("Error ", e);
		}
		
		return result;
	}
	
}
