package org.aksw.qa.systems;

import java.net.URI;
import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.util.ResponseToStringParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskNow extends ASystem {
	private Logger log = LoggerFactory.getLogger(AskNow.class);
	
	@Override
	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);
		HashSet<String> resultSet = new HashSet<String>();

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		URIBuilder builder = new URIBuilder().setScheme("https").setHost("asknowdemo.sda.tech")
				.setPath("/list").setParameter("question", questionString);
		
		URI uri = builder.build();
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = client.execute(httpget);	

		//Test if error occured
		if(response.getStatusLine().getStatusCode()>=400){
			throw new Exception("AskNow Server could not answer due to: " + response.getStatusLine());
		}
			
		builder = new URIBuilder().setScheme("https").setHost("asknowdemo.sda.tech")
				.setPath("/_getJSON");
		uri = builder.build();
		httpget = new HttpGet(uri);
		httpget.addHeader("Referer", "https://asknowdemo.sda.tech/list?question=" + questionString + "?");
		response = client.execute(httpget);
		
		if(response.getStatusLine().getStatusCode()>=400){
			throw new Exception("AskNow Server could not answer due to: "+ response.getStatusLine());
		}
		
		ResponseToStringParser responseparser = new ResponseToStringParser();
		JSONParser parser = new JSONParser();
		String responseString = responseparser.responseToString(response);
		JSONObject answerjson =  (JSONObject) parser.parse(responseString);
		answerjson = (JSONObject) answerjson.get("fullDetail");
		
		//if no answer just return
		if(((JSONArray) answerjson.get("answers")).size() == 0) {
			return;
		}
		
		JSONArray answers =  (JSONArray) ((JSONArray) answerjson.get("answers")).get(0);
		
		for(int i = 0; i< answers.size(); i++) {
			JSONObject answer = (JSONObject) answers.get(i);
			String key = (String) answer.keySet().toArray()[0];
			answer = (JSONObject) answer.get(key);
			resultSet.add((String) answer.get("value"));
		}
		question.setGoldenAnswers(resultSet);
		
		JSONArray queries =  (JSONArray) ((JSONObject) answerjson.get("sparql")).get("queries");	
		String query = (String) queries.get(0);
		question.setSparqlQuery(query.trim());
	}

	@Override
	public String name() {
		return "asknow";
	}
}
