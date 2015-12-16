package org.aksw.autosparql.commons.qald;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.autosparql.commons.qald.uri.GoldEntity;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.util.JSONStatusBuilder;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//TODO move to qa-commons
public class Question implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9130793012431486456L;

	public Integer id;
	public int cardinality;
	public Boolean onlydbo;
	public Boolean outOfScope;
	public Boolean aggregation;
	public Boolean hybrid;
	public String answerType;
	public String sparqlQuery;
	public String pseudoSparqlQuery;
	public MutableTree tree;
	public Map<String, String> languageToQuestion = new LinkedHashMap<String, String>();
	public Map<String, List<String>> languageToKeywords = new LinkedHashMap<String, List<String>>();
	public Map<String, List<Entity>> languageToNamedEntites = new LinkedHashMap<String, List<Entity>>();
	public Map<String, List<Entity>> languageToNounPhrases = new LinkedHashMap<String, List<Entity>>();
	public Map<String, List<GoldEntity>> goldEntites = new HashMap<String, List<GoldEntity>>();
	public Map<String, Set<String>> goldenAnswers = new HashMap<String, Set<String>>();

	//for proper handling in webservice
	public UUID UUID;
	public boolean finished;
	public List<Answer> finalAnswer;
	public JSONObject tree_full;
	public JSONObject tree_pruned;
	public JSONObject tree_final;
	public JSONArray pruning_messages=new JSONArray();



	public Question() {

		goldEntites.put("en", new ArrayList<GoldEntity>());
		goldEntites.put("de", new ArrayList<GoldEntity>());
	}

	@Override
	public String toString() {

		String output = String.format("id: %s answerType: %s aggregation: %s onlydbo: %s\n", id, answerType, aggregation, onlydbo);
		for (Map.Entry<String, String> entry : languageToQuestion.entrySet()) {
			output += "\t" + entry.getKey() + "\tQuestion: " + entry.getValue() + "\n";
			output += "\t\tKeywords: " + StringUtils.join(languageToKeywords.get(entry.getKey()), ", ") + "\n";
			output += "\t\tGold-Entities: " + StringUtils.join(goldEntites, ", ") + "\n";
			if (languageToNamedEntites.containsKey(entry.getKey()))
				output += "\t\tEntities: " + StringUtils.join(languageToNamedEntites.get(entry.getKey()), ", ") + "\n";
			if (languageToNounPhrases.containsKey(entry.getKey()))
				output += "\t\tNouns: " + StringUtils.join(languageToNounPhrases.get(entry.getKey()), ", ") + "\n";
		}
		output += "SPARQL: " + pseudoSparqlQuery;
		output += "\tAnswers: " + StringUtils.join(goldenAnswers, ", ") + "\n";

		return output;
	}

	public String getJSONStatus() {
		JSONObject sb = JSONStatusBuilder.status(this);
		return sb.toJSONString();

	}
}
