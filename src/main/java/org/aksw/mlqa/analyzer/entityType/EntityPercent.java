package org.aksw.mlqa.analyzer.entityType;

import java.util.List;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;
import weka.core.FastVector;

public class EntityPercent implements IAnalyzer {
		static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
		private Attribute attribute = null;
		private StanfordCoreNLP pipeline;
		
		public EntityPercent() {
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
			props.setProperty("ner.useSUTime", "false");
			pipeline = new StanfordCoreNLP(props);
			FastVector fvWekaPercent = new FastVector(2);
			fvWekaPercent.addElement("Percent");
			fvWekaPercent.addElement("NoPercent");
			attribute = new Attribute("Percent", fvWekaPercent);
		}

		@Override
		public Object analyze(String q) {
			String result = "NoPercent";
			Annotation annotation = new Annotation(q);
			pipeline.annotate(annotation);
			List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
			for (CoreMap sentence : sentences)
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		        String ne = token.get(NamedEntityTagAnnotation.class); 
		        if(ne.equals("PERCENT"))
		        	result = "Percent";
		       }
			return result;
		}

		@Override
		public Attribute getAttribute() {
			return attribute;
		}
	}

