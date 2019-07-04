package org.aksw.mlqa.analyzer.partofspeechtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;

/***
 * The purpose of this class is to count the occurrences of the part-of-speech tags from a question.
 * @author Lukas
 *
 */
public class PartOfSpeechTags implements IAnalyzer {
	private static final  StanfordCoreNLP PIPELINE;
	private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	
	/***
	 * List of all part-of-speech tags. Without punctuations.
	 */
	private static final ArrayList<String> TAGS = new ArrayList<String>( Arrays.asList("CC", "CD", "DT", "EX", "FW", "IN", 
			"JJ", "JJR", "JJS", "LS", "MD", "NN", "NNS", "NNP", "NNPS", "PDT", "POS", "PRP","PRP$", 
			"RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP",
			"VBZ", "WDT", "WP", "WP$", "WRB")); 

	static {
		Properties props = new Properties();
	    props.setProperty("annotators","tokenize, ssplit, pos");
	    PIPELINE = new StanfordCoreNLP(props);
	}
	
	public PartOfSpeechTags() {
		for(String tag: TAGS) {
			Attribute attribute = new Attribute(tag);	
			attributes.add(attribute);
		}
	}
	
	/***
	 * Returns a HashMap with the part-of-speech tags as keys and their occurrences as values.
	 */
	@Override
	public Object analyze(String q) {
		Map<String,Integer> tagCount = new LinkedHashMap<String,Integer>();
		for(String tag: TAGS) {
			tagCount.put(tag,0);
		}

		Annotation annotation = new Annotation(q);
        PIPELINE.annotate(annotation);
      
        List<CoreMap> question = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : question) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for(CoreLabel token: tokens) {   
            	String tag = token.tag();
            	if(TAGS.contains(tag)) {
            		tagCount.put(tag, tagCount.get(tag) + 1);
            	}
            }
        }       
		return tagCount;
	}

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}
	
	@Override
	public Attribute getAttribute() {
		return null;
	}
	
	public ArrayList<String> getTags() {
		return TAGS;
	}
}
