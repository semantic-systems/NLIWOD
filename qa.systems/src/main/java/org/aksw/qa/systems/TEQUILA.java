package org.aksw.qa.systems;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


public class TEQUILA extends Gen_HTTP_QA_Sys {

	private static final String URL = "https://gate.d5.mpi-inf.mpg.de/tequila/getAnswers";
	
	//Remove Temporal Expressions possible values: Remove, NotRemove
	private static final String REMOVE_DATE_NODE = "Remove";
	//Add Candidates without Date possible values: Add, NotAdd
	private static final String ADD_NO_DATE_MODE = "NotAdd";
	//Temporal Relation possible values: Latest, NotLatest
	private static final String ONLY_LATEST_ONE = "NotLatest";
	//Top-k Candidate possible values: Rank1, Rank2, Rank3
	private static final String RANK_MODE = "Rank1";
	//Underlying KB-QA System possible values: AQQU, QUINT
	private static final String QA_MODE = "AQQU";
	
	public TEQUILA() {
		super(URL, "tequila", false, false);
		this.setQueryKey("question");
	}
	
	public TEQUILA(String url) {
		super(url, "tequila", false, false);
		this.setQueryKey("question");
	}
	
	@Override
	public HttpResponse fetchGetResponse(String url, Map<String, String> paramMap) throws URISyntaxException, ClientProtocolException, IOException {
		paramMap.put("removeDateMode", REMOVE_DATE_NODE);
		paramMap.put("addNoDateAnswersMode", ADD_NO_DATE_MODE);
		paramMap.put("onlyLatestOneMode", ONLY_LATEST_ONE);
		paramMap.put("rankMode", RANK_MODE);
		paramMap.put("qaMode", QA_MODE);
		return super.fetchGetResponse(url, paramMap);
	}
	
	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		HashSet<String> resultSet = new HashSet<String>();	
		JSONParser parser = new JSONParser();
		JSONObject answerjson = null;
		
		try {
			answerjson =  (JSONObject) parser.parse(responseparser.responseToString(response));		
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		JSONArray answerArray = (JSONArray) answerjson.get("tempo_answer");
		if(answerArray == null || answerArray.size() == 0) return;
		
		for(int i = 0; i<answerArray.size(); i++) {
			JSONObject answer = (JSONObject) answerArray.get(i);
			String ans = (String) answer.get("wiki");
			if(ans.length() == 0) return;
			resultSet.add(ans.substring(1,ans.length()-1));
		}
		question.setGoldenAnswers(resultSet);	
		question.setSparqlQuery((String) answerjson.get("subquestion_1_updated_sparql"));
	}
	
	public static void main(String[] args) throws Exception {
		ASystem a = new TEQUILA();
		System.out.println(a.search("x", "en"));
	}
}
