package org.aksw.qa.systems;

import java.util.HashSet;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OKBQA extends ASystem {
	Logger log = LoggerFactory.getLogger(OKBQA.class);
	
	private static final String CONTROLLER_URI = "http://ws.okbqa.org:7042/cm";
	private static final String TGM_URI = "http://ws.okbqa.org:1515/templategeneration/rocknrole";
	private static final String KB_URI2 = "http://dbpedia.org/sparql";
	private static final String QGM_URI = "http://ws.okbqa.org:38401/queries";
	private static final String AGM_URI = "http://ws.okbqa.org:7745/agm";
	private static final String DM_URI = "http://ws.okbqa.org:2357/agdistis/run";

	private JSONObject conf;
	
	public OKBQA(){
		createJSONConf();
	}
	
	private String execute(String jsonInput) throws Exception{
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(CONTROLLER_URI);
		StringEntity entity = new StringEntity(jsonInput);
		
		httppost.setEntity(entity);
		
		HttpResponse response = client.execute(httppost);
		
		if(response.getStatusLine().getStatusCode()>=400){
			throw new Exception("OKBQA Server could not answer due to: "+response.getStatusLine());
		}
		return responseparser.responseToString(response);
	}
	
	@Override
	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);
		
		
		Set<String> answerSet = new HashSet<String>();
		question.setGoldenAnswers(answerSet);
		
		//Execute TGM to AGM. 
		String responseString = execute(createInputJSON(questionString, language));
		JSONObject obj = new JSONObject(responseString);
		JSONArray results = obj.getJSONArray("result");
		//Iterate over answers and add them to the final answerSet
		for(int i=0; i<results.length(); i++){
			JSONObject result = results.getJSONObject(i);

			String answerString = result.getString("answer");
			answerSet.add(answerString);
			
		}
		
		//Get Query from log
		JSONArray logs = obj.getJSONArray("log");
		for(int i=0; i<logs.length(); i++){
			JSONObject log = logs.getJSONObject(i);
			if(log.getString("1. module").equals("QGM")){
				if(log.has("4. output")){
					try{
						JSONArray qout = log.getJSONArray("4. output");
						if(qout.length()>0&& qout.getJSONObject(0).has("query")){
							String queryString = qout.getJSONObject(0).getString("query");
							question.setSparqlQuery(queryString);
						}
					}catch(JSONException e){
						return;
					}
				}
			}
		}

		question.setGoldenAnswers(answerSet);
	}
	
	@Override
	public String name() {
		return "okbqa";
	}
	
	private String createInputJSON(String questionString, String language){
		JSONObject json = new JSONObject();
		JSONObject input = new JSONObject();
		
		input.put("language", language);
		input.put("string", questionString);
	
		json.put("input", input);
		json.put("conf", conf);
		json.put("timelimit", "10000");
		
		return json.toString();
	}
	
	private void createJSONConf(){
		conf = new JSONObject();
		JSONArray sequence = new JSONArray();
		sequence.put(0, "TGM");
		sequence.put(1, "DM");
		sequence.put(2, "QGM");
		sequence.put(3, "AGM");
		conf.put("sequence", sequence);
		
		JSONObject address = new JSONObject();
		JSONArray kbAddress = new JSONArray();
		JSONArray kbAddressURIs = new JSONArray();
		kbAddressURIs.put(0, KB_URI2);
		kbAddressURIs.put(1, "");
		kbAddress.put(kbAddressURIs);
		JSONArray tgmAddress = new JSONArray();
		tgmAddress.put(TGM_URI);
		JSONArray dmAddress = new JSONArray();
		dmAddress.put(DM_URI);
		JSONArray qgmAddress = new JSONArray();
		qgmAddress.put(QGM_URI);
		JSONArray agmAddress = new JSONArray();
		agmAddress.put(AGM_URI);
		
		address.put("KB", kbAddress);
		address.put("TGM", tgmAddress);
		address.put("DM", dmAddress);
		address.put("QGM", qgmAddress);
		address.put("AGM", agmAddress);
		conf.put("address", address);
	}


}
