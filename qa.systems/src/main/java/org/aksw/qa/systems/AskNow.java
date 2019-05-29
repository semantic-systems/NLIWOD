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

public class AskNow extends Gen_HTTP_QA_Sys {
	
	private static final String URL = "https://asknowdemo.sda.tech/_getJSON";
	
	public AskNow() {
		super(URL, "asknow", true, false);
		this.setQueryKey("question");
	}
	
	public AskNow(String url) {
		super(url, "asknow", true, false);
		this.setQueryKey("question");
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
		answerjson = (JSONObject) answerjson.get("fullDetail");
		
		//if no answer just return
		if(((JSONArray) answerjson.get("answers")).size() == 0) return;	
		JSONArray answers =  (JSONArray) ((JSONArray) answerjson.get("answers")).get(0);
		for(int i = 0; i< answers.size(); i++) {
			JSONObject answer = (JSONObject) answers.get(i);
			String key = (String) answer.keySet().toArray()[0];
			answer = (JSONObject) answer.get(key);
			resultSet.add((String) answer.get("value"));
		}
		question.setGoldenAnswers(resultSet);	
		if(!(answerjson.get("sparql") instanceof JSONObject)) return;
		
		JSONArray queries =  (JSONArray) ((JSONObject) answerjson.get("sparql")).get("queries");	
		String query = (String) queries.get(0);
		question.setSparqlQuery(query.trim());
	}
}
