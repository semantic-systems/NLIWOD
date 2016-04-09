package org.aksw.mlqa.analyzer.nqs;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;

public class ner_resolver {

	Properties props;
	StanfordCoreNLP pipeline;
	static ArrayList<String> nertag = new ArrayList<String>();

	public ner_resolver(Properties props,StanfordCoreNLP pipeline){
		this.props = props;
		this.pipeline = pipeline;
	}
	
	public ner_resolver() {
		this.props = new Properties();
		this.props.put("annotators",
				"tokenize, ssplit, pos, lemma, ner");
		this.pipeline = new StanfordCoreNLP(props);
	}

	public List<Triple<String, Integer, Integer>> getNERTags(String inputText,boolean getAll) {	
		pipeline.clearAnnotatorPool();
		List<Triple<String, Integer, Integer>> entities = new ArrayList<Triple<String,Integer,Integer>>();		
	    Annotation annotation = new Annotation(inputText);
	    pipeline.annotate(annotation);
	    entities = getNERTags(annotation,getAll);
	    
	    if(entities.size()>0)
	    	displayEntities(entities,inputText);
	    else
	    	System.out.println("NO NER FOUND.");
	   
	    return entities;
	}
	

	public List<Triple<String, Integer, Integer>> getNERTags(Annotation annotation, boolean getAll) {
		List<Triple<String, Integer, Integer>> entities = new ArrayList<Triple<String,Integer,Integer>>();		
		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
	        for (int i = 0; i < tokens.size(); i++) {
	            CoreLabel token = tokens.get(i);
	            if(!getAll){
		            if(!token.ner().equals("O")){
		            	Triple<String, Integer, Integer> entity = new Triple<String, Integer, Integer>(token.ner(), token.beginPosition(), token.endPosition());
		               	entities.add(entity);
		            }
	            } else{
	            	Triple<String, Integer, Integer> entity = new Triple<String, Integer, Integer>(token.ner(), token.beginPosition(), token.endPosition());
	               	entities.add(entity);
	            }
	        }
	    }		
		return entities;
	}
	
	private void displayEntities(List<Triple<String, Integer, Integer>> entities, String inputText) {
		ArrayList<String> tag = new ArrayList<String>();
		for(Triple<String, Integer, Integer>entry:entities){
	    	//System.out.println(entry.first+":"+inputText.substring(entry.second,entry.third));
	    	tag.add(entry.first);
	    }
		nertag =tag;
	}

	public void close(){
		props.clear();
		pipeline.clearAnnotatorPool();
	}
	
}