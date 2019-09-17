package org.aksw.qa.systems;

import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SorokinQA extends Gen_HTTP_QA_Sys_JSON {

	private static final String URL_UG = "http://semanticparsing.ukp.informatik.tu-darmstadt.de:5000/question-answering/ungroundedgraph/";
	private static final String URL_GG = "http://semanticparsing.ukp.informatik.tu-darmstadt.de:5000/question-answering/groundedgraphs/";
	private static final String URL_EG = "http://semanticparsing.ukp.informatik.tu-darmstadt.de:5000/question-answering/evaluategraphs/";
	
	public SorokinQA() {
		super(URL_UG, "sorokinqa");
	}
	
	/**
	 * Overriding original search method to implement SorokinQA's three step requests for QA
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		
		String responseString = fetchPostResponse(URL_UG, createInputJSON(questionString));
		
		JSONParser parser = new JSONParser();
		JSONObject answerjson = null;
		answerjson = (JSONObject) parser.parse(responseString);
		JSONArray entities = (JSONArray) answerjson.get("entities");
		
		// next step only takes the first linking from the first step for each entity 
		if(entities.size() > 0) {
			for(int i=0; i<entities.size(); i++) {
				JSONObject element = (JSONObject) entities.get(i);
				JSONArray linkings = (JSONArray) element.get("linkings");
				JSONArray first = (JSONArray) linkings.get(0);
				linkings.clear();
				linkings.add(first);
			}
		}	
		responseString = fetchPostResponse(URL_GG, answerjson.toString());

		answerjson = (JSONObject) parser.parse(responseString);

		JSONArray graphs = (JSONArray) answerjson.get("graphs");
		JSONArray inputEvaluate = new JSONArray();
		//next step only takes the first element from this array
		if(graphs.size() > 0) {
			JSONArray first = (JSONArray) graphs.get(0);
			inputEvaluate.add(first);
		}
		responseString = fetchPostResponse(URL_EG, inputEvaluate.toString());
		
		processResponse(responseString, question);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String createInputJSON(String question) {
		JSONObject json = new JSONObject();	
		json.put("question", question);
		json.put("simplified_npparser", 1);
		return json.toString();
	}

	@Override
	public void processResponse(String response, IQuestion question) {
		JSONParser parser = new JSONParser();
		JSONArray answerjson = null;
		try {
			answerjson =  (JSONArray) parser.parse(response);		
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}

		HashSet<String> resultSet = new HashSet<String>();
		answerjson = (JSONArray) answerjson.get(0);
		if(answerjson.size() == 0) {
			System.out.println("eee");
			return;
		}
		for(int i = 0; i<answerjson.size(); i++) {
			resultSet.add("https://www.wikidata.org/wiki/" + (String) answerjson.get(i));
		}
		//only returns wikidata ids, nothing else
		question.setGoldenAnswers(resultSet);		
	}
}
