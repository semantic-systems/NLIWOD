package org.aksw.mlqa.analyzer.entityType;

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
import weka.core.FastVector;

public class EntityLocation implements IAnalyzer {
	//static Logger log = LoggerFactory.getLogger(EntityLocation.class);
	private Attribute attribute = null;
	private StanfordCoreNLP pipeline;
	
	public EntityLocation() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.setProperty("ner.useSUTime", "false");
		pipeline = new StanfordCoreNLP(props);
		FastVector fvWekaLocation = new FastVector(2);
		fvWekaLocation.addElement("Location");
		fvWekaLocation.addElement("NoLocation");
		attribute = new Attribute("Location", fvWekaLocation);
	}

	@Override
	public Object analyze(String q) {
		String result = "NoLocation";
		Annotation annotation = new Annotation(q);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences)
		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        String ne = token.get(NamedEntityTagAnnotation.class); 
	        if(ne.equals("LOCATION"))
	        	result = "Location";
	       }
		return result;
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
