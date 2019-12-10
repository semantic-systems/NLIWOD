package org.aksw.qa.systems;

import java.io.IOException;
import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.util.ResponseToStringParser;
import org.apache.http.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class QANARY extends Gen_HTTP_QA_Sys {

	private static final String URL = "http://qanswer-core1.univ-st-etienne.fr/api/gerbil";
	
	//possible values: dbpedia, wikidata, dblp, freebase
	private static final String KB = "wikidata";
	
	public QANARY() {
		super(URL, "qanary", true, false);
		this.getParamMap().put("kb", KB);
	}
	
	public QANARY(String url, String kb) {
		super(url, "qanary", true, false);
		this.getParamMap().put("kb", kb);
	}
	
	
	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		HashSet<String> resultSet = new HashSet<String>();
		
		ResponseToStringParser responseparser = new ResponseToStringParser();
		JSONParser parser = new JSONParser();
		String responseString = responseparser.responseToString(response);
		JSONObject answerjson = null;
		
		try {
			answerjson = (JSONObject) parser.parse(responseString);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		JSONArray array = (JSONArray) answerjson.get("questions");
		JSONObject answer = (JSONObject) array.get(0);
		JSONObject questionObject = (JSONObject) answer.get("question");
		String answerString = (String) questionObject.get("answers");
		
		try {
			answerjson = (JSONObject) parser.parse(answerString);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		JSONArray vars = (JSONArray) ((JSONObject) answerjson.get("head")).get("vars");
		
		//no answers
		if(vars == null) return;
		
		for(int var = 0; var < vars.size(); var++) {
			JSONObject results  = (JSONObject) answerjson.get("results");
			JSONArray bindings = (JSONArray) results.get("bindings");
			for(int result = 0; result<bindings.size(); result++) {
				JSONObject r = (JSONObject) bindings.get(result);
				resultSet.add((String) ((JSONObject)(r.get(vars.get(var)))).get("value"));
			}
		}
		question.setGoldenAnswers(resultSet);
		
		JSONArray sparql = ((JSONArray) ((JSONObject) answer.get("question")).get("language"));	
		question.setSparqlQuery((String) ((JSONObject) sparql.get(0)).get("SPARQL"));
	}
}
