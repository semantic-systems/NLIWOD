package org.aksw.hawk.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.util.JSONStatusBuilder;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//TODO delete everything from that class that already exists in the abstract class
public class HAWKQuestion extends Question implements IQuestion, Serializable {

	private static final long serialVersionUID = 1L;
	private int cardinality;
	private Boolean isClassifiedAsASKQuery; // what the program classifies
	private Boolean loadedAsASKQuery; // what we load from the data file
	private MutableTree tree;
	private Map<String, List<Entity>> languageToNamedEntites = new LinkedHashMap<String, List<Entity>>();
	private Map<String, List<Entity>> languageToNounPhrases = new LinkedHashMap<String, List<Entity>>();
	private Map<String, List<Entity>> goldEntites = new HashMap<String, List<Entity>>();

	// for proper handling in webservice
	// TODO remove this all after new webservice based on JSON RDF is
	// implemented
	private UUID UUID;
	private boolean finished;
	private List<Answer> finalAnswer;
	private JSONObject tree_full;
	private JSONObject tree_pruned;
	private JSONObject tree_final;
	private JSONArray pruning_messages = new JSONArray();

	public HAWKQuestion() {

		goldEntites.put("en", new ArrayList<Entity>());
		goldEntites.put("de", new ArrayList<Entity>());
	}

	// TODO remove this method and replace it with a senseful one, once the rest
	// interface is changed
	public String toString() {

		String output = String.format("id: %s answerType: %s aggregation: %s onlydbo: %s\n", getId(), getAnswerType(), getAggregation(), getOnlydbo());
		for (Map.Entry<String, String> entry : getLanguageToQuestion().entrySet()) {
			output += "\t" + entry.getKey() + "\tQuestion: " + entry.getValue() + "\n";
			output += "\t\tKeywords: " + StringUtils.join(getLanguageToKeywords().get(entry.getKey()), ", ") + "\n";
			output += "\t\tGold-Entities: " + StringUtils.join(goldEntites, ", ") + "\n";
			if (getLanguageToNamedEntites().containsKey(entry.getKey()))
				output += "\t\tEntities: " + StringUtils.join(getLanguageToNamedEntites().get(entry.getKey()), ", ") + "\n";
			if (getLanguageToNounPhrases().containsKey(entry.getKey()))
				output += "\t\tNouns: " + StringUtils.join(getLanguageToNounPhrases().get(entry.getKey()), ", ") + "\n";
		}
		output += "PseudoSPARQL: " + getPseudoSparqlQuery() + "\n";
		output += "SPARQL: " + getSparqlQuery() + "\n";
		output += "Answers: " + StringUtils.join(getGoldenAnswers(), ", ") + "\n";

		return output;
	}

	public String getJSONStatus() {
		JSONObject sb = JSONStatusBuilder.status(this);
		return sb.toJSONString();

	}

	public JSONObject getTree_full() {
		return tree_full;
	}

	public void setTree_full(JSONObject tree_full) {
		this.tree_full = tree_full;
	}

	public Boolean getIsClassifiedAsASKQuery() {
		return isClassifiedAsASKQuery;
	}

	public void setIsClassifiedAsASKQuery(Boolean isClassifiedAsASKQuery) {
		this.isClassifiedAsASKQuery = isClassifiedAsASKQuery;
	}

	public Boolean getLoadedAsASKQuery() {
		return loadedAsASKQuery;
	}

	public void setLoadedAsASKQuery(Boolean loadedAsASKQuery) {
		this.loadedAsASKQuery = loadedAsASKQuery;
	}

	public Map<String, List<Entity>> getLanguageToNamedEntites() {
		return languageToNamedEntites;
	}

	public void setLanguageToNamedEntites(Map<String, List<Entity>> languageToNamedEntites) {
		this.languageToNamedEntites = languageToNamedEntites;
	}

	public Map<String, List<Entity>> getLanguageToNounPhrases() {
		return languageToNounPhrases;
	}

	public void setLanguageToNounPhrases(Map<String, List<Entity>> languageToNounPhrases) {
		this.languageToNounPhrases = languageToNounPhrases;
	}

	public MutableTree getTree() {
		return tree;
	}

	public void setTree(MutableTree tree) {
		this.tree = tree;
	}

	public JSONObject getTree_final() {
		return tree_final;
	}

	public void setTree_final(JSONObject tree_final) {
		this.tree_final = tree_final;
	}

	public JSONObject getTree_pruned() {
		return tree_pruned;
	}

	public void setTree_pruned(JSONObject tree_pruned) {
		this.tree_pruned = tree_pruned;
	}

	public JSONArray getPruning_messages() {
		return pruning_messages;
	}

	public void setPruning_messages(JSONArray pruning_messages) {
		this.pruning_messages = pruning_messages;
	}

	public List<Answer> getFinalAnswer() {
		return finalAnswer;
	}

	public void setFinalAnswer(List<Answer> finalAnswer) {
		this.finalAnswer = finalAnswer;
	}

	public UUID getUUID() {
		return UUID;
	}

	public void setUUID(UUID uUID) {
		UUID = uUID;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	public boolean checkSuitabillity() {
		return (this.getAnswerType().matches("resource||boolean") & this.getOnlydbo() & !this.getAggregation());// ||
		                                                                                                        // this.getLoadedAsASKQuery();

	}

}
