package org.aksw.hawk.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nlp.Annotater;
import org.aksw.hawk.nlp.Cardinality;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.nouncombination.NounCombinationChain;
import org.aksw.hawk.nouncombination.NounCombiners;
import org.aksw.hawk.number.UnitController;
import org.aksw.hawk.querybuilding.PatternSparqlGenerator;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.sparql.SPARQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tranforms a question into a sequence of POS-tags, entities, properties, classes and combined nouns
 * @author rricha
 *
 */

public class SequenceGenerator {
	static Logger log = LoggerFactory.getLogger(SequenceGenerator.class);
	private ASpotter nerdModule;
	private Annotater annotater;
	private Cardinality cardinality;
	private QueryTypeClassifier queryTypeClassifier;
	private StanfordNLPConnector stanfordConnector;

	private UnitController numberToDigit;
	private NounCombinationChain nounCombination;

	public SequenceGenerator() {
		queryTypeClassifier = new QueryTypeClassifier();

		nerdModule = new Spotlight();
		// controller.nerdModule = new Spotlight();
		// controller.nerdModule =new TagMe();
		// controller.nerdModule = new MultiSpotter(fox, tagMe, wiki, spot);

		this.stanfordConnector = new StanfordNLPConnector();
		this.numberToDigit = new UnitController();

		numberToDigit.instantiateEnglish(stanfordConnector);
		nounCombination = new NounCombinationChain(NounCombiners.HawkRules, NounCombiners.StanfordDependecy);

		cardinality = new Cardinality();

		SPARQL sparql = new SPARQL("http://131.234.28.52:3030/ds/sparql");
		annotater = new Annotater(sparql);

	}
	
	private void getLabelsToQuestion(HAWKQuestion q) {
		String transformedQuestion = new String();
		
		q.setTransformedQuestion(q.getLanguageToQuestion().get("en"));
		
		//POS-tagging
		q.setTree(stanfordConnector.parseTree(q, this.numberToDigit));
		log.info("POS-tagged tree: " + q.getTree());
		
		// Annotate tree
		log.info("Semantically annotating the tree.");
		annotater.annotateTree(q);
		
		Map<String, String> annotationToWords  = annotater.annotationToWords;
		
		//Place properties in their respective places
		for (Map.Entry<String, String> entry : annotationToWords.entrySet())
		{
		    //case of auxilary verb
		    if (entry.getValue().equals("#property"))
		    	continue;
		    if (entry.getValue().contains("#property")) {
		    	transformedQuestion = HAWKUtils.replaceLabelByAnnotation(q.getTransformedQuestion(), entry.getKey(), entry.getValue());
		    	q.setTransformedQuestion(transformedQuestion);
		    }
		}
		q.setTree(stanfordConnector.parseTree(q, this.numberToDigit));
		log.info("tree after placing props: " + q.getTree());
		
		// Disambiguate parts of the query
		log.info("Combined nouns.");
		nounCombination.runChain(q);
		
		log.info("Named entity recognition.");
		q.setLanguageToNamedEntites(nerdModule.getEntities(q.getTransformedQuestion()));
		
		List<Entity> namedEntity =  q.getLanguageToNamedEntites().get("en");
		List<Entity> nounPhrases = q.getLanguageToNounPhrases().get("en");
		List<Entity> finalNE = new ArrayList<>();
		List<Entity> finalCNN = new ArrayList<>();
		
		log.info("NE: " + namedEntity + "CNN: " + nounPhrases);
		
		if(namedEntity != null && nounPhrases != null) {
			Iterator<Entity> np = nounPhrases.iterator();
			Iterator<Entity> ne = namedEntity.iterator();
			//Loop over nounPhrases to get finalCNN
			while(np.hasNext()) {
				Entity enp = np.next();
				String nounPh = enp.getLabel();
				Boolean found = false;
				while(ne.hasNext()) {
					Entity ene = ne.next();
					String namedEnt = ene.getLabel();
					// if nounPh == namedEnt, it's a NE
					if (nounPh.equals(namedEnt) && !finalNE.contains(ene)) {
						finalNE.add(ene);
						found = true;
					}
					// if namedEnt is a substring of nounPh, it's a CNN
					else if(nounPh.contains(namedEnt) && !finalCNN.contains(enp)) {
						finalCNN.add(enp);
						found = true;
					}
					if (found == true)
						break;
					
				}
				if (found == false)
					finalCNN.add(enp);
			}
			
			ne = namedEntity.iterator();
			//Loop over namedEntities to add the distinct ones
			while(ne.hasNext()) {
				Entity ene = ne.next();
				String namedEnt = ene.getLabel();
				np = nounPhrases.iterator();
				Boolean found = false;
				while(np.hasNext()) {
					Entity enp = np.next();
					String nounPh = enp.getLabel();
					if(nounPh.contains(namedEnt))
						found = true;
				}
				if(found == false && !finalNE.contains(ene))
					finalNE.add(ene);
			}
			
		}
		
		else if(namedEntity == null && nounPhrases != null) 
			finalCNN = nounPhrases;
		
		else if(namedEntity != null && nounPhrases == null)
			finalNE = namedEntity;
		
		
		q.getLanguageToNamedEntites().put("en", finalNE);
		q.getLanguageToNounPhrases().put("en", finalCNN);
		log.info("finalNE: " + finalNE + "FinalCNN: " + finalCNN);
		
		//Replace NE and CNN with urls 
		transformedQuestion = HAWKUtils.replaceNamedEntitysWithURL(q);
		q.setTransformedQuestion(transformedQuestion);
		
		//Place classes in their respective places
		for (Map.Entry<String, String> entry : annotationToWords.entrySet())
		{
		    if (entry.getValue().contains("#class")) {
		    	transformedQuestion = HAWKUtils.replaceLabelByAnnotation(q.getTransformedQuestion(), entry.getKey(), entry.getValue());
		    	q.setTransformedQuestion(transformedQuestion);
		    }
		}

		q.setTree(stanfordConnector.parseTree(q, this.numberToDigit));
		
		//Place POS-tags in place of the remaining words
		StringBuilder result = new StringBuilder();
		List<MutableTreeNode> treeNodes = q.getTree().getAllNodesInSentenceOrder(); 
		Iterator<MutableTreeNode> mt = treeNodes.iterator();
		while (mt.hasNext()) {
			MutableTreeNode tmp = mt.next();
			String label = tmp.label;
			String posTag = tmp.posTag;
				
			//Place POS-tag
	        if(!label.startsWith("http")) {
	        	result.append(posTag + " ");
	        }
	        else {
	        	result.append(label + " ");
	        }
		}
		
		log.info("result: " + result.toString());
		q.setTransformedQuestion(result.toString());
		
	}
	/*
	 * This method returns a sentence without POS-tags and with just class/prop/CNN labels.
	 * Example output: #class resource CNN #property
	 */
	public String getQuestionWithoutPOSTags(HAWKQuestion q) {
		StringBuilder result = new StringBuilder();
		List<MutableTreeNode> treeNodes = q.getTree().getAllNodesInSentenceOrder(); 
		Iterator<MutableTreeNode> mt = treeNodes.iterator();
		while (mt.hasNext()) {
			MutableTreeNode tmp = mt.next();
			String label = tmp.label;
			String posTag = tmp.posTag;
			//remove POS-tags
			if(label.startsWith("http")) {
				if (label.contains("#class"))
					result.append("#class ");
				else if(label.contains("#property"))
					result.append("#property ");
				else if(label.contains("resource"))
					result.append("resource ");
				else if(label.contains("combinedNN"))
					result.append("combinedNN ");
			}
				
		}
		return result.toString();
	}
	
	public static void main(final String[] args) throws IOException{
		
//		List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD7_Train_Multilingual));
//		Iterator<HAWKQuestion> hq = questions.iterator();
//		FileOutputStream fout=new FileOutputStream("output.txt");
//		 
//		while(hq.hasNext()) {
//			SequenceGenerator sg = new SequenceGenerator();
//			HAWKQuestion q = hq.next();
//			sg.getLabelsToQuestion(q);
//			String line = "ANALYTICS \t question: " + q.getLanguageToQuestion().get("en") + ": \t" + sg.getQuestionWithoutPOSTags(q) + "\n";
//			fout.write(line.getBytes());
//			log.info(line);
//		}
//		fout.close();
		
		//Person death/deathplace/.. IN DET http://aksw.org/combinedNN/Battle_of_Arnhem PUNCT
		
		SequenceGenerator sg = new SequenceGenerator();
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "In which year was Rachel Stevens born?");
		sg.getLabelsToQuestion(q);
		log.info("ANALYTICS \t question: " + q.getLanguageToQuestion().get("en") + ": \t" + sg.getQuestionWithoutPOSTags(q) + "\n");
	}

	

}
