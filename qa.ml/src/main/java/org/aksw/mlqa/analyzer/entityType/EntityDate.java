package org.aksw.mlqa.analyzer.entityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;

public class EntityDate implements IAnalyzer {
	// private static Logger log = LoggerFactory.getLogger(EntityDate.class);
	private Attribute attribute = null;
	private StanfordCoreNLP pipeline;
	
	public EntityDate() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.setProperty("ner.useSUTime", "false");
		pipeline = new StanfordCoreNLP(props);
		
		ArrayList<String> fvWekaDate = new ArrayList<String>();
		fvWekaDate.add("Date");
		fvWekaDate.add("NoDate");
		attribute = new Attribute("Date", fvWekaDate);
	}
	
//FIXME: Time funktioniert mit Stanford NLP nicht einwandfrei.
	
	@Override
	public Object analyze(String q) {
		String result = "NoDate";
		Annotation annotation = new Annotation(q);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences)
		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        String ne = token.get(NamedEntityTagAnnotation.class); 
	        if("DATE".equals(ne))
	        	result = "Date";
	       }
		return result;
	}
	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
