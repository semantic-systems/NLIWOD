package org.aksw.qa.systems;

import java.io.IOException;
import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.util.ResponseToStringParser;
import org.apache.http.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SorokinQA extends Gen_HTTP_QA_Sys{

	private static final String URL = "http://semanticparsing.ukp.informatik.tu-darmstadt.de:5000/question-answering/answerforqald/";
	
	public SorokinQA() {
		super(URL, "sorokinqa", true, false);
		this.setQueryKey("question");
	}
	
	public SorokinQA(String url) {
		super(url, "sorokinqa", true, false);
		this.setQueryKey("question");
	}
	
	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		ResponseToStringParser responseparser = new ResponseToStringParser();
		JSONParser parser = new JSONParser();
		String responseString = responseparser.responseToString(response);
		JSONArray answerjson = null;
		try {
			answerjson =  (JSONArray) parser.parse(responseString);		
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}

		HashSet<String> resultSet = new HashSet<String>();
		for(int i = 0; i<answerjson.size(); i++) {
			resultSet.add("https://www.wikidata.org/wiki/" + (String) answerjson.get(i));
		}
		//only returns wikidata ids, nothing else
		question.setGoldenAnswers(resultSet);
	}
}
