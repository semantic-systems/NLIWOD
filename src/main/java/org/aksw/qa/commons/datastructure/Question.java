package org.aksw.qa.commons.datastructure;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class Question implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9130793012431486456L;

	public Integer id;
	public String answerType;
	public String pseudoSparqlQuery;
	public String sparqlQuery;
	public Boolean aggregation;
	public Boolean onlydbo;
	public Boolean outOfScope;
	public Boolean hybrid;
	public Map<String, String> languageToQuestion = new LinkedHashMap<String, String>();
	public Map<String, List<String>> languageToKeywords = new LinkedHashMap<String, List<String>>();

	public Map<String, List<Entity>> languageToNamedEntites = new LinkedHashMap<String, List<Entity>>();
	public Map<String, List<Entity>> languageToNounPhrases = new LinkedHashMap<String, List<Entity>>();
	public Map<String, Set<String>> goldenAnswers = new HashMap<String, Set<String>>();

	// TODO redo the goldAnswer thing. Introduce Answer object

	public Question() {
		goldenAnswers.put("en", new HashSet<String>());
		goldenAnswers.put("de", new HashSet<String>());
	}

	@Override
	public String toString() {

		String output = String.format("id: %s answerType: %s aggregation: %s onlydbo: %s\n", id, answerType, aggregation, onlydbo);
		for (Map.Entry<String, String> entry : languageToQuestion.entrySet()) {
			output += "\t" + entry.getKey() + "\tQuestion: " + entry.getValue() + "\n";
			output += "\t\tKeywords: " + StringUtils.join(languageToKeywords.get(entry.getKey()), ", ") + "\n";
			output += "\t\tGold-Entities: " + StringUtils.join(goldenAnswers, ", ") + "\n";
			if (languageToNamedEntites.containsKey(entry.getKey()))
				output += "\t\tEntities: " + StringUtils.join(languageToNamedEntites.get(entry.getKey()), ", ") + "\n";
			if (languageToNounPhrases.containsKey(entry.getKey()))
				output += "\t\tNouns: " + StringUtils.join(languageToNounPhrases.get(entry.getKey()), ", ") + "\n";
		}
		output += "SPARQL: " + sparqlQuery;

		return output;
	}
}
