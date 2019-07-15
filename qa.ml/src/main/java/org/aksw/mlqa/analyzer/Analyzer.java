package org.aksw.mlqa.analyzer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.aksw.mlqa.analyzer.comparative.Comparative;
import org.aksw.mlqa.analyzer.dependencies.Dependencies;
import org.aksw.mlqa.analyzer.entitytype.EntityDate;
import org.aksw.mlqa.analyzer.entitytype.EntityLocation;
import org.aksw.mlqa.analyzer.entitytype.EntityMoney;
import org.aksw.mlqa.analyzer.entitytype.EntityOrganization;
import org.aksw.mlqa.analyzer.entitytype.EntityPercent;
import org.aksw.mlqa.analyzer.entitytype.EntityPerson;
import org.aksw.mlqa.analyzer.numberoftoken.NumberOfToken;
import org.aksw.mlqa.analyzer.partofspeechtags.PartOfSpeechTags;
import org.aksw.mlqa.analyzer.queryanswertype.QueryAnswerTypeAnalyzer;
import org.aksw.mlqa.analyzer.questiontype.QuestionTypeAnalyzer;
import org.aksw.mlqa.analyzer.questionword.QuestionWord;
import org.aksw.mlqa.analyzer.superlative.Superlative;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

/**
 * This class extracts all specified features from a question and returns the feature vector. 
 * @author Lukas
 *
 */
public class Analyzer {
	
	/**
	 * The IAnalyzers that are applied for the feature extraction.
	 */
	private List<IAnalyzer> analyzers;
	
	/**
	 * List of the attributes of the IAnalyzers.
	 */
	public ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();

	/**
	 * Uses all IAnalyzers/features as default.
	 */
	public Analyzer() {
		analyzers = new ArrayList<IAnalyzer>();
		analyzers.add(new PartOfSpeechTags());
		analyzers.add(new Dependencies());
		analyzers.add(new QuestionTypeAnalyzer());
		analyzers.add(new QueryAnswerTypeAnalyzer());
		analyzers.add(new QuestionWord());
		analyzers.add(new NumberOfToken());
		analyzers.add(new Superlative());
		analyzers.add(new Comparative());
		analyzers.add(new EntityPerson());
		analyzers.add(new EntityMoney());
		analyzers.add(new EntityLocation());
		analyzers.add(new EntityPercent());
		analyzers.add(new EntityOrganization());
		analyzers.add(new EntityDate());	
		setAttributes();
	}
	
	/**
	 * Uses all specified IAnalyzers/features.
	 * @param analyzers
	 */
	public Analyzer(List<IAnalyzer> analyzers) {
		this.analyzers= analyzers;
		setAttributes();
	}

	/**
	 * Analyzes the question and extracts all features that were set for this Analyzer.
	 * @param q question string
	 * @return feature vector for the input question
	 */
	public Instance analyze(String q) {
		Instance tmpInstance = new DenseInstance(fvWekaAttributes.size());
		
		for (IAnalyzer analyzer : analyzers) {
			//special case for PartOfSpeechTags, need to set 36 attributes
			if(analyzer instanceof PartOfSpeechTags) {
				analyzePOS(tmpInstance, (PartOfSpeechTags) analyzer, q);
				continue;
			}		
			
			//special case for Dependencies, need to set 18 attributes
			if(analyzer instanceof Dependencies) {
				analyzeDeps(tmpInstance, (Dependencies) analyzer, q);
				continue;
			}
			
			Attribute attribute = analyzer.getAttribute();
			if (attribute.isNumeric()) {
				tmpInstance.setValue(attribute, (double) analyzer.analyze(q));
			} else if (attribute.isNominal() || attribute.isString()) {
				String value = (String) analyzer.analyze(q);
				tmpInstance.setValue(attribute,value);
				tmpInstance.setDataset(null);
			}
		}
		return tmpInstance;
	}
	
	/**
	 * Retrieves the occurrences of all dependencies from the question. Then iterates over the HashMap and sets all 18 attributes for the instance.
	 * @param tmpInstance 
	 * @param analyzer
	 * @param q 
	 */
	private void analyzeDeps(Instance tmpInstance, Dependencies analyzer, String q) {
		@SuppressWarnings("unchecked")
		LinkedHashMap<String,Integer> map = (LinkedHashMap<String, Integer>) analyzer.analyze(q);
		
		ArrayList<Attribute> attributes = analyzer.getAttributes();
		for(String ind: map.keySet()) {
			Attribute a = null;
			//searches for the attribute object of the HashMap entry
			for(Attribute att: attributes) {
				if(att.name().equals(ind)) {
					a = att;
					break;
				}
			}
			tmpInstance.setValue(a, map.get(ind));
		}
	}
			
	/**
	 * Retrieves the occurrences of all part-of-speech tags from the question. Then iterates over the HashMap and sets all 36 attributes for the instance.
	 * @param tmpInstance
	 * @param analyzer
	 * @param q
	 */
	private void analyzePOS(Instance tmpInstance, PartOfSpeechTags analyzer, String q) {
		@SuppressWarnings("unchecked")
		LinkedHashMap<String,Integer> map = (LinkedHashMap<String, Integer>) analyzer.analyze(q);
		
		ArrayList<Attribute> attributes = analyzer.getAttributes();
		for(String ind: map.keySet()) {
			Attribute a = null;
			//searches for the attribute object of the HashMap entry
			for(Attribute att: attributes) {
				if(att.name().equals(ind)) {
					a = att;
					break;
				}
			}
			tmpInstance.setValue(a, map.get(ind));
		}
	}
	
	/**
	 * Registers the attributes of all IAnalyzers that are used for this Analyzer.
	 * @param analyzers list of analyzers
	 */
	private void setAttributes() {
		for (IAnalyzer analyzer : analyzers) {
			//special case for PartOfSpeechTags, need to add 36 attributes
			if(analyzer instanceof PartOfSpeechTags) {
				fvWekaAttributes.addAll((((PartOfSpeechTags) analyzer).getAttributes()));
				continue;
			}			
			//special case for Dependencies, need to add 18 attributes
			if(analyzer instanceof Dependencies) {
				fvWekaAttributes.addAll((((Dependencies) analyzer).getAttributes()));
				continue;
			}

			fvWekaAttributes.add(analyzer.getAttribute());
		}
	}
}