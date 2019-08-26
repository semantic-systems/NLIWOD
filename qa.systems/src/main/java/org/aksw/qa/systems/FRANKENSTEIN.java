package org.aksw.qa.systems;

import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FRANKENSTEIN extends Gen_HTTP_QA_Sys_JSON {
	
	private static final String URL = "http://frankenstein.qanary-qa.com/query";
	
	public FRANKENSTEIN() {
		super(URL, "frankenstein");
	}
	
	public FRANKENSTEIN(String url) {
		super(url, "frankenstein");
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public String createInputJSON(String question) {
		JSONObject json = new JSONObject();
		
		json.put("queryRequestString", question);
		JSONArray components = new JSONArray();
		json.put("components", components);
		json.put("requiresQueryBuilding", true);
		JSONArray tasks = new JSONArray();
		json.put("conf", tasks);

		return json.toString();
	}

	@Override
	public void processResponse(String response, IQuestion question) {
		JSONParser parser = new JSONParser();
		JSONObject answerjson = null;

		try {
			answerjson = (JSONObject) parser.parse(response);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		HashSet<String> resultSet = new HashSet<String>();	
		JSONArray responseStrings = (JSONArray) answerjson.get("queryResponseStrings");
		
		//check if answer array is empty
		if(responseStrings == null || responseStrings.size() < 3) return;
		
		for(int i = 2; i<responseStrings.size(); i++) {
			String answer = (String) responseStrings.get(i);
			//0 at third index means no answer
			if(i == 2 && "0".equals(answer)) return;
			resultSet.add(answer);
		}
		question.setGoldenAnswers(resultSet);		
	}
}
