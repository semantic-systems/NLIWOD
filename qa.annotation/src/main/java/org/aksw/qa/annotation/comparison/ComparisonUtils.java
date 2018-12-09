package org.aksw.qa.annotation.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Offers functionality to extract superlatives and comparatives from strings and functions to retrieve properties and the order for a 
 * comparative or superlative.
 */
public class ComparisonUtils {
	
	private static final StanfordCoreNLP PIPELINE;
	
	private static final String COMPARATIVETAG = "JJR";
	
	private static final String SUPERLATIVETAG = "JJS";
	
	static {
		Properties props = new Properties();
	    props.setProperty("annotators","tokenize, ssplit, pos, depparse");
	    PIPELINE = new StanfordCoreNLP(props);
	}
	
	/**
	 * Returns a list of properties for the given superlative or comparative (also works for the basic form of the
	 * supported adjectives). Supported adjectives can be found in {@link ComparisonEnum}.
	 * @param comparison Adjective.
	 * @return List of properties for the given adjective, null if the adjective is not supported. 
	 */
	public ArrayList<String> getProperties(String comparison) {
		if(comparison == null) return null;
		String comp = comparison.toUpperCase();
		if(Arrays.stream(ComparisonEnum.values()).anyMatch( enumValue -> enumValue.toString().equals(comp))) {
			return ComparisonEnum.valueOf(comp).getURIS();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the order for the given superlative or comparative which can then be used in SPARQL queries. 
	 * Supported adjectives can be found in {@link ComparisonEnum}.
	 * @param comparison A superlative or comparative.
	 * @return Order for the given superlative or comparative, null if it is not supported. 
	 */
	public String getOrder(String comparison) {
		if(comparison == null) return null;
		String comp = comparison.toUpperCase();
		if(Arrays.stream(ComparisonEnum.values()).anyMatch( enumValue -> enumValue.toString().equals(comp))) {
			return ComparisonEnum.valueOf(comp).getOrder();
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieves comparatives from the string. Calls {@link #getWords(String, String)}.
	 * @param question String to retrieve comparatives from. 
	 * @return List of the retrieved comparatives. 
	 */
	public ArrayList<String> getComparatives(String question) {
        return getWords(question, COMPARATIVETAG);
	}
		
	/**
	 * Retrieves superlatives from the string. Calls {@link #getWords(String, String)}.
	 * @param question String to retrieve superlatives from. 
	 * @return List of the retrieved superlatives. 
	 */
	public ArrayList<String> getSuperlatives(String question) {
        return getWords(question, SUPERLATIVETAG);
	}
	
	/**
	 * Retrieves a part of speech from the given string, depending on the parameter tag.
	 * JJR for comparatives and JJS for superlatives.
	 * @param question String to retrieve words from. 
	 * @param tag JJR for comparatives and JJS for superlatives.
	 * @return List of the retrieved words. 
	 */
	private ArrayList<String> getWords(String question, String tag) {
		if(question == null || tag == null) return null;
		Annotation annotation = new Annotation(question);
        PIPELINE.annotate(annotation);  
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        
 		ArrayList<String> words = new ArrayList<String>();		
 		for (CoreMap sentence : sentences) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for(CoreLabel token: tokens) {
               	if(token.tag().startsWith(tag)){
            		String word = token.toString();
            		words.add(word.substring(0, word.lastIndexOf("-")));
            	}
            }
        }       	
 		return words;
 	}
}
