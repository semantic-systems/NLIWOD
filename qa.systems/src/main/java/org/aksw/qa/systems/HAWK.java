package org.aksw.qa.systems;

import java.net.URI;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.commons.codec.Charsets;
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

public class HAWK extends ASystem {
	Logger log = LoggerFactory.getLogger(HAWK.class);

	private Decoder based64Decoder = Base64.getDecoder();

	public String name() {
		return "hawk";
	};

	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		URIBuilder builder = new URIBuilder().setScheme("http")
				.setHost("139.18.2.164:8181").setPath("/search")
				.setParameter("q", questionString);
		if(this.setLangPar){
			builder = builder.setParameter("lang", language);
		}
		URI iduri = builder.build();
		HttpGet httpget = new HttpGet(iduri);
		HttpResponse idresponse = client.execute(httpget);
		
		//Test if error occured
		if(idresponse.getStatusLine().getStatusCode()>=400){
			throw new Exception("HAWK Server (ID) could not answer due to: "+idresponse.getStatusLine());
		}
		
		String id = responseparser.responseToString(idresponse);
		JSONParser parser = new JSONParser();

		URI quri = new URIBuilder().setScheme("http")
				.setHost("139.18.2.164:8181").setPath("/status")
				.setParameter("UUID", id.substring(1, id.length() - 2)).build();

		int j = 0;
		do {
			Thread.sleep(50);
			HttpGet questionpost = new HttpGet(quri);
			HttpResponse questionresponse = client.execute(questionpost);
			
			//Test if error occured
			if(questionresponse.getStatusLine().getStatusCode()>=400){
				throw new Exception("HAWK Server (Question) could not answer due to: "+questionresponse.getStatusLine());
			}
			
			JSONObject responsejson = (JSONObject) parser.parse(responseparser
					.responseToString(questionresponse));
			if (responsejson.containsKey("answer")) {
				JSONArray answerlist = (JSONArray) responsejson.get("answer");
				HashSet<String> result = new HashSet<String>();
				for (int i = 0; i < answerlist.size(); i++) {
					JSONObject answer = (JSONObject) answerlist.get(i);
					result.add(answer.get("URI").toString());
				}
				question.setGoldenAnswers(result);
				if (responsejson.containsKey("final_sparql_base64")) {
					String sparqlQuery = responsejson
							.get("final_sparql_base64").toString();
					sparqlQuery = new String(
							based64Decoder.decode(sparqlQuery), Charsets.UTF_8);
					question.setSparqlQuery(sparqlQuery);
				}
			}
			j = j + 1;
		} while (j < 500);
	}
}
