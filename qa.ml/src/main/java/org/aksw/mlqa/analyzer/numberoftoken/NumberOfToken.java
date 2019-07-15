package org.aksw.mlqa.analyzer.numberoftoken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;

/**
 * Analyses the number of token in the input question. Counts noun phrases as one token.
 * @author ricardousbeck
 *
 */
public class NumberOfToken implements IAnalyzer {
	private static final  StanfordCoreNLP PIPELINE;
	
	private Logger log = LoggerFactory.getLogger(NumberOfToken.class);
	private Attribute attribute = null;

	static {
		Properties props = new Properties();
	    props.setProperty("annotators","tokenize, ssplit, pos, depparse");
	    PIPELINE = new StanfordCoreNLP(props);
	}
	
	public NumberOfToken() {
		attribute = new Attribute("NumberOfToken");
	}

	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);
		String[] split = q.split("\\s+");
		ArrayList<String> nounPhrases = getNounPhrases(q);
		return (double) (split.length - nounPhrases.size());
	}
	
	/***
	 * Returns a list of all noun phrases of the question q.
	 * @param q  a question
	 * @return list of noun phrases
	 */
	private ArrayList<String> getNounPhrases(String q) {
 		ArrayList<String> nounP = new ArrayList<String>();
     
 		Annotation annotation = new Annotation(q);
        PIPELINE.annotate(annotation);
      
        List<CoreMap> question = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        
        for (CoreMap sentence : question) {
            SemanticGraph basicDeps = sentence.get(BasicDependenciesAnnotation.class);
            Collection<TypedDependency> typedDeps = basicDeps.typedDependencies();
         
            Iterator<TypedDependency> dependencyIterator = typedDeps.iterator();
            while(dependencyIterator.hasNext()) {
            	TypedDependency dependency = dependencyIterator.next();
            	String depString = dependency.reln().toString();
            	if(depString.equals("compound") || depString.equals("amod")) {
            		String dep = dependency.dep().toString();
            		String gov = dependency.gov().toString();
            		nounP.add(dep.substring(0, dep.lastIndexOf("/")) + " " + gov.substring(0, gov.lastIndexOf("/")));
            	}
            }
        }    
        return nounP;
 	}
	
	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
