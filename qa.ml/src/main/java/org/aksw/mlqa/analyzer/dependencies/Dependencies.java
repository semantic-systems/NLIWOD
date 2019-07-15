package org.aksw.mlqa.analyzer.dependencies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;

/***
 * The purpose of this class is to count the occurrences of the dependencies from a question.
 * @author Lukas
 *
 */
public class Dependencies implements IAnalyzer {
	private static final  StanfordCoreNLP PIPELINE;
	private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	
	/***
	 * All dependencies we consider for the feature.
	 */
	private static final ArrayList<String> DEPENDENCIES = new ArrayList<String>( Arrays.asList("nsubj","iobj", "dobj", "csubj", "xcomp",
			"advcl", "nmod", "nummod","appos",  "acl", "amod", "det", "neg", "compound", "case", "advmod", "root", "punct")); 
	
	static {
		Properties props = new Properties();
	    props.setProperty("annotators","tokenize, ssplit, pos, depparse");
	    PIPELINE = new StanfordCoreNLP(props);
	}
	
	public Dependencies() {
		for(String tag: DEPENDENCIES) {
			Attribute attribute = new Attribute(tag);	
			attributes.add(attribute);
		}
	}
	
	/***
	 * Returns a HashMap with the dependencies as keys and their occurrences as values.
	 */
	@Override
	public Object analyze(String q) {
		Map<String,Integer> dependencyMap = new LinkedHashMap<String,Integer>();
		for(String tag: DEPENDENCIES) {
			dependencyMap.put(tag,0);
		}

		Annotation annotation = new Annotation(q);
        PIPELINE.annotate(annotation);
      
        List<CoreMap> question = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap sentence : question) {
			SemanticGraph basicDependencies = sentence.get(BasicDependenciesAnnotation.class);
			Collection<TypedDependency> typedDependencies = basicDependencies.typedDependencies();
	      
			for(TypedDependency t: typedDependencies) {
				String dep = "";
				for(String tag: DEPENDENCIES) {
					//startswith check for subtypes
					if(t.reln().toString().startsWith(tag)) {
						dep = tag;
					}
				}
				// not contained in the DEPENDENCIES list so not relevant
				if(dep.equals("")) continue;
				
				dependencyMap.put(dep, dependencyMap.get(dep) + 1);
			}
		}
		return dependencyMap;
	}

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}
	
	@Override
	public Attribute getAttribute() {
		return null;
	}
	
	public ArrayList<String> getTags() {
		return DEPENDENCIES;
	}
}
