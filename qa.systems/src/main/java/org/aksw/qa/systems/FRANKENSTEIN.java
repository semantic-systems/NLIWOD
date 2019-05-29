package org.aksw.qa.systems;

import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FRANKENSTEIN extends Gen_HTTP_QA_Sys {
	
	private static final String URL = "http://frankenstein.qanary-qa.com/query";
	
	public FRANKENSTEIN() {
		super(URL, "frankenstein", true, false);
	}
	
	public FRANKENSTEIN(String url) {
		super(url, "frankenstein", true, false);
	}
		
	@Override
	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		
		String responseString = execute(createInputJSON(questionString));
		JSONParser parser = new JSONParser();
		JSONObject answerjson = null;
		try {
			answerjson = (JSONObject) parser.parse(responseString);
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
	
	private String execute(String jsonInput) throws Exception{
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(URL);
		StringEntity entity = new StringEntity(jsonInput);
		httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
		httppost.setEntity(entity);
		
		HttpResponse response = client.execute(httppost);
		
		if(response.getStatusLine().getStatusCode()>=400){
			throw new Exception("FRANKENSTEIN Server could not answer due to: " + response.getStatusLine());
		}
		return responseparser.responseToString(response);
	}

	@SuppressWarnings("unchecked")
	private String createInputJSON(String question) throws JSONException{
		JSONObject json = new JSONObject();
		
		json.put("queryRequestString", question);
		JSONArray components = new JSONArray();
		json.put("components", components);
		json.put("requiresQueryBuilding", true);
		JSONArray tasks = new JSONArray();
		json.put("conf", tasks);

		return json.toString();
	}
}
