package org.aksw.mlqa.analyzer.entitytype;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Entity {
	
	private static StanfordCoreNLP pipeline;

	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.setProperty("ner.useSUTime", "false");
		pipeline = new StanfordCoreNLP(props);
	}
	
	/***
	 * Checks if there is an entity of the specified type in the question.
	 * @param entityType an entity type: Date, Location, Organization, Person, Percent, or Money
	 * @param question
	 * @return if a entity of that type is present returns the name of the type otherwise "No" + the name of the type 
	 */
	protected String recognizeEntity(String entityType, String question){
		String result = "No" + entityType;
		Annotation annotation = new Annotation(question);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences)
		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        String ne = token.get(NamedEntityTagAnnotation.class); 
	        if(entityType.toUpperCase().equals(ne))
	        	result = entityType;
	       }
		return result;
	}
}
