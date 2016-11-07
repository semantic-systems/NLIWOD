package org.aksw.qa.systems;

import java.net.URI;
import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SINA extends ASystem {
	Logger log = LoggerFactory.getLogger(SINA.class);

	public String name() {
		return "sina";
	};

	public void search(IQuestion question) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey("en")) {
			return;
		}
		questionString = question.getLanguageToQuestion().get("en");
		log.debug(this.getClass().getSimpleName() + ": " + questionString);
		HashSet<String> resultSet = new HashSet<String>();

		HttpClient client = HttpClientBuilder.create().build();
		URI uri = new URIBuilder().setScheme("http").setHost("sina.aksw.org")
				.setPath("/api/rest/search").setParameter("q", questionString)
				.build();
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = client.execute(httpget);
		//Test if error occured
		if(response.getStatusLine().getStatusCode()>=400){
			throw new Exception("SINA Server could not answer due to: "+response.getStatusLine());
		}
		
		JSONParser parser = new JSONParser();
		String responseString = responseparser.responseToString(response);
		JSONArray answerjson = (JSONArray) parser.parse(responseString);
		for (int i = 0; i < answerjson.size(); i++) {
			JSONObject answer = (JSONObject) answerjson.get(i);
			resultSet.add((String) answer.get("URI_PARAM"));
		}
		question.setGoldenAnswers(resultSet);

		uri = new URIBuilder().setScheme("http").setHost("sina.aksw.org")
				.setPath("/api/rest/search").setParameter("q", questionString)
				.setParameter("content", "sparql").build();
		httpget = new HttpGet(uri);
		response = client.execute(httpget);
		responseString = responseparser.responseToString(response);
		answerjson = (JSONArray) parser.parse(responseString);
		if (answerjson.size() > 0) {
			JSONObject sparqlQuery = (JSONObject) answerjson.get(0);
			question.setSparqlQuery((String) sparqlQuery.get("SPARQL_PARAM"));
		}
	}
}
