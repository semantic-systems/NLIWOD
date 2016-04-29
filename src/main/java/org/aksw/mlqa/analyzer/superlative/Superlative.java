package org.aksw.mlqa.analyzer.superlative;

import java.util.List;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;
import weka.core.FastVector;

public class Superlative implements IAnalyzer {
	static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
	private Attribute attribute = null;
	private StanfordCoreNLP pipeline;
	
	public Superlative() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.setProperty("ner.useSUTime", "false");
		pipeline = new StanfordCoreNLP(props);
		
		FastVector fvWekaSuperlative= new FastVector(2);
		fvWekaSuperlative.addElement("Superlative");
		fvWekaSuperlative.addElement("NoSuperlative");
		attribute = new Attribute("Superlative", fvWekaSuperlative);
	}
	
//FIXME: Time funktioniert mit Stanford NLP nicht einwandfrei.
	
	@Override
	public Object analyze(String q) {
		String result = "NoSuperlative";
		Annotation annotation = new Annotation(q);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences)
		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class); 
	        if(pos.equals("RBS")||pos.equals("JJS"))
	        	result = "Superlative";
	       }
		return result;
	}
	public Attribute getAttribute() {
		return attribute;
	}

}
