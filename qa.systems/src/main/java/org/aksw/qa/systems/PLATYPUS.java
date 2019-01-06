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

public class PLATYPUS extends Gen_HTTP_QA_Sys{
	
	private static final String URL = "https://qa.askplatyp.us/v0/ask";
	
	public PLATYPUS() {
		super(URL, "platypus", false, false);
		this.setQuery_key("q");
	}
	
	public PLATYPUS(String url) {
		super(url, "platypus", false, false);
		this.setQuery_key("q");
	}
	
	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
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
		
		HashSet<String> resultSet = new HashSet<String>();
		//check if one or more results
		if(answerjson.get("member") instanceof JSONArray) {
			JSONArray answers = (JSONArray) answerjson.get("member");
			
			//no answers
			if(answers.size() == 0) return;		
			
			setQuery((JSONObject) answers.get(0), question);
			for(int i = 0; i<answers.size(); i++) {
				JSONObject answer = (JSONObject) answers.get(i);
				setResult((JSONObject) answer.get("result"), question, resultSet);
			}
		} else {			
			JSONObject answer = (JSONObject) answerjson.get("member"); 		
			setQuery(answer, question);
			setResult((JSONObject) answer.get("result"), question, resultSet);
		}
		question.setGoldenAnswers(resultSet);
	}
	
	private void setQuery(JSONObject answer, IQuestion question) {
		String query = (String) answer.get("platypus:sparql");
		if(query != null) {
			query = query.replaceAll("\t", "");
			query = query.replaceAll("\n", "");
			question.setSparqlQuery(query);
		}
	}
	
	private void setResult(JSONObject result, IQuestion question, HashSet<String> resultSet) {
		String id = (String) result.get("@id");
		if(id == null) {
			id = (String) result.get("name");
		}
		id = id.replaceAll("wd:", "http://www.wikidata.org/entity/");
		resultSet.add(id);
	}
}
