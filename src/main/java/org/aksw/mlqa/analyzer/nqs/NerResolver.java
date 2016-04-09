package org.aksw.mlqa.analyzer.nqs;

import java.util.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;
import lombok.extern.slf4j.Slf4j;


@Slf4j public class NerResolver {

	Properties props;
	StanfordCoreNLP pipeline;

	public NerResolver(Properties props,StanfordCoreNLP pipeline){
		this.props = props;
		this.pipeline = pipeline;
	}
	
	public NerResolver() {
		this.props = new Properties();
		this.props.put("annotators",
				"tokenize, ssplit, pos, lemma, ner");
		this.pipeline = new StanfordCoreNLP(props);
	}

	public List<Triple<String, Integer, Integer>> getNERTags(String inputText,boolean getAll) {	
		StanfordCoreNLP.clearAnnotatorPool();
		List<Triple<String, Integer, Integer>> entities = new ArrayList<>();		
	    Annotation annotation = new Annotation(inputText);
	    pipeline.annotate(annotation);
	    entities = getNERTags(annotation,getAll);
	   /* if(entities.size()>0)
	    	displayEntities(entities,inputText);
	    else
	    	log.warn("NO NER FOUND.");*/
	    return entities;
	}
	

	public List<Triple<String, Integer, Integer>> getNERTags(Annotation annotation, boolean getAll) {
		List<Triple<String, Integer, Integer>> entities = new ArrayList<>();		
		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
	        for (int i = 0; i < tokens.size(); i++) {
	            CoreLabel token = tokens.get(i);
	            if(!getAll){
		            if(!token.ner().equals("O")){
		            	Triple<String, Integer, Integer> entity = new Triple<>(token.ner(), token.beginPosition(), token.endPosition());
		               	entities.add(entity);
		            }
	            } else{
	            	Triple<String, Integer, Integer> entity = new Triple<>(token.ner(), token.beginPosition(), token.endPosition());
	               	entities.add(entity);
	            }
	        }
	    }		
		return entities;
	}
	
	private void displayEntities(List<Triple<String, Integer, Integer>> entities, String inputText) {
		for(Triple<String, Integer, Integer>entry:entities){
	    	log.info(entry.first+","+inputText.substring(entry.second,entry.third));
	    }		
	}

	public void close(){
		props.clear();
		StanfordCoreNLP.clearAnnotatorPool();
	}
	
}