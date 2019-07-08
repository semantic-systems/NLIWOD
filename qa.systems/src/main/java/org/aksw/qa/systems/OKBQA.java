package org.aksw.qa.systems;

import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OKBQA extends Gen_HTTP_QA_Sys_JSON {
	
	private static final String CONTROLLER_URI = "http://ws.okbqa.org:7047/cm";
	private static final String TGM_URI = "http://ws.okbqa.org:1515/templategeneration/rocknrole";
	private static final String KB_URI1 = "http://kbox.kaist.ac.kr:5889/sparql";
	private static final String KB_URI2 = "http://en.dbpedia2014.kaist.ac.kr";
	private static final String QGM_URI = "http://ws.okbqa.org:38401/queries";
	private static final String AGM_URI = "http://ws.okbqa.org:7745/agm";
	private static final String DM_URI = "http://ws.okbqa.org:2357/agdistis/run";

	private JSONObject conf;
	
	public OKBQA() {
		super(CONTROLLER_URI, "okbqa");
		try {
			createJSONConf();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
			
	@Override
	public String createInputJSON(String question) {
		JSONObject json = new JSONObject();
		JSONObject input = new JSONObject();
		
		input.put("language", "en");
		input.put("string", question);
	
		json.put("input", input);
		json.put("conf", conf);
		json.put("timelimit", "10000");
		
		return json.toString();
	}
	
	@Override
	public void processResponse(String response, IQuestion question) {
		HashSet<String> resultSet = new HashSet<String>();
		if(response == null || response.length() == 0) return;
		JSONObject obj = new JSONObject(response);
		JSONArray results = obj.getJSONArray("result");
		//Iterate over answers and add them to the final answerSet
		for(int i=0; i<results.length(); i++){
			JSONObject result = results.getJSONObject(i);

			String answerString = result.getString("answer");
			resultSet.add(answerString);
			
		}
		
		//Get Query from log
		JSONArray logs = obj.getJSONArray("log");
		for(int i=0; i<logs.length(); i++){
			JSONObject log = logs.getJSONObject(i);
			if(log.getString("1. module").equals("QGM") && log.has("4. output")){
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
		question.setGoldenAnswers(resultSet);	
	}
	
	private void createJSONConf() throws JSONException{
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
		kbAddressURIs.put(0, KB_URI1);
		kbAddressURIs.put(1, KB_URI2);
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
		conf.put("sync", "on");
	}
}
