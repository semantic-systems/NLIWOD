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

public class HAWKQuestion extends Question implements IQuestion, Serializable {

	private static final long serialVersionUID = 1L;
	private int cardinality;
	private Boolean isClassifiedAsASKQuery; // what the program classifies
	private Boolean loadedAsASKQuery; // what we load from the data file
	private MutableTree tree;
	private Map<String, List<Entity>> languageToNamedEntites = new LinkedHashMap<>();
	private Map<String, List<Entity>> languageToNounPhrases = new LinkedHashMap<>();
	private Map<String, List<Entity>> goldEntites = new HashMap<>();
	private String transformedQuestion = new String(); // for POS-tagged and annotated sentence in HybridPSG

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

	public HAWKQuestion(final String englishQuestion) {
		this();
		this.getLanguageToQuestion().put("en", englishQuestion);
	}

	// TODO remove this method and replace it with a senseful one, once the rest
	// interface is changed
	@Override
	public String toString() {

		String output = String.format("id: %s answerType: %s aggregation: %s onlydbo: %s\n", getId(), getAnswerType(), getAggregation(), getOnlydbo());
		for (Map.Entry<String, String> entry : getLanguageToQuestion().entrySet()) {
			output += "\t" + entry.getKey() + "\tQuestion: " + entry.getValue() + "\n";
			output += "\t\tKeywords: " + StringUtils.join(getLanguageToKeywords().get(entry.getKey()), ", ") + "\n";
			output += "\t\tGold-Entities: " + StringUtils.join(goldEntites, ", ") + "\n";
			if (getLanguageToNamedEntites().containsKey(entry.getKey())) {
				output += "\t\tEntities: " + StringUtils.join(getLanguageToNamedEntites().get(entry.getKey()), ", ") + "\n";
			}
			if (getLanguageToNounPhrases().containsKey(entry.getKey())) {
				output += "\t\tNouns: " + StringUtils.join(getLanguageToNounPhrases().get(entry.getKey()), ", ") + "\n";
			}
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

	public void setTree_full(final JSONObject tree_full) {
		this.tree_full = tree_full;
	}

	public Boolean getIsClassifiedAsASKQuery() {
		return isClassifiedAsASKQuery;
	}

	public void setIsClassifiedAsASKQuery(final Boolean isClassifiedAsASKQuery) {
		this.isClassifiedAsASKQuery = isClassifiedAsASKQuery;
	}

	public Boolean getLoadedAsASKQuery() {
		return loadedAsASKQuery;
	}

	public void setLoadedAsASKQuery(final Boolean loadedAsASKQuery) {
		this.loadedAsASKQuery = loadedAsASKQuery;
	}

	public Map<String, List<Entity>> getLanguageToNamedEntites() {
		return languageToNamedEntites;
	}

	public void setLanguageToNamedEntites(final Map<String, List<Entity>> languageToNamedEntites) {
		this.languageToNamedEntites = languageToNamedEntites;
	}

	public Map<String, List<Entity>> getLanguageToNounPhrases() {
		return languageToNounPhrases;
	}

	public void setLanguageToNounPhrases(final Map<String, List<Entity>> languageToNounPhrases) {
		this.languageToNounPhrases = languageToNounPhrases;
	}

	public MutableTree getTree() {
		return tree;
	}

	public void setTree(final MutableTree tree) {
		this.tree = tree;
	}

	public JSONObject getTree_final() {
		return tree_final;
	}

	public void setTree_final(final JSONObject tree_final) {
		this.tree_final = tree_final;
	}

	public JSONObject getTree_pruned() {
		return tree_pruned;
	}

	public void setTree_pruned(final JSONObject tree_pruned) {
		this.tree_pruned = tree_pruned;
	}

	public JSONArray getPruning_messages() {
		return pruning_messages;
	}

	public void setPruning_messages(final JSONArray pruning_messages) {
		this.pruning_messages = pruning_messages;
	}

	public List<Answer> getFinalAnswer() {
		return finalAnswer;
	}

	public void setFinalAnswer(final List<Answer> finalAnswer) {
		this.finalAnswer = finalAnswer;
	}

	public UUID getUUID() {
		return UUID;
	}

	public void setUUID(final UUID uUID) {
		UUID = uUID;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(final int cardinality) {
		this.cardinality = cardinality;
	}
	
	public void setTransformedQuestion(final String transformedQuestion) {
		this.transformedQuestion = transformedQuestion;
	}
	
	public String getTransformedQuestion() {
		return transformedQuestion; 
	}

	public boolean checkSuitabillity() {
		return (this.getAnswerType().matches("resource||boolean||uri") & this.getOnlydbo() & !this.getAggregation());// ||
		                                                                                                        // this.getLoadedAsASKQuery();

	}

}
