package org.aksw.mlqa.analyzer.entityType;

import java.util.List;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.FastVector;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class EntityMoney implements IAnalyzer {
		static Logger log = LoggerFactory.getLogger(EntityMoney.class);
		private Attribute attribute = null;
		private StanfordCoreNLP pipeline;
		
		public EntityMoney() {
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
			props.setProperty("ner.useSUTime", "false");
			pipeline = new StanfordCoreNLP(props);
			FastVector fvWekaMoney = new FastVector(2);
			fvWekaMoney.addElement("Money");
			fvWekaMoney.addElement("NoMoney");
			attribute = new Attribute("Money", fvWekaMoney);
		}

		@Override
		public Object analyze(String q) {
			String result = "NoMoney";
			Annotation annotation = new Annotation(q);
			pipeline.annotate(annotation);
			List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
			for (CoreMap sentence : sentences)
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		        String ne = token.get(NamedEntityTagAnnotation.class); 
		        if(ne.equals("MONEY"))
		        	result = "Money";
		       }
			return result;
		}

		@Override
		public Attribute getAttribute() {
			return attribute;
		}
	}
