package org.aksw.qa.systems;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


// not complete yet, since the website is not working correctly at the moment
public class QUINT extends Gen_HTTP_QA_Sys {

	private static final String URL = "https://gate.d5.mpi-inf.mpg.de/quint/quint";
	
	//for more information check the information page on the website
	//training resource possible values: WQ (WebQuestions), F917 (Free917)
	private static final String DATA_RESOURCE = "WQ";
	//possible values: T (Typed), UN (Untyped)
	private static final String QUINT_VARIANT = "T";
	//top-k queries possible values: 1, 3, 5
	private static final String QUERIES_NUMBER = "5";
	//Number of Decision Trees for LTR possible values: 40, 70, 90
	private static final String NUMBER_DECISION_TREES = "40";
	
	public QUINT() {
		super(URL, "quint", true, false);
		this.setQueryKey("question");
	}
	
	public QUINT(String url) {
		super(url, "quint", true, false);
		this.setQueryKey("question");
	}

	@Override
	public HttpResponse fetchPostResponse(String url, Map<String, String> paramMap) throws ClientProtocolException, IOException {
		paramMap.put("dataResource", DATA_RESOURCE);
		paramMap.put("quintVariant", QUINT_VARIANT);
		paramMap.put("queriesNumber", QUERIES_NUMBER);
		paramMap.put("numberDecisionTrees", NUMBER_DECISION_TREES);
		return super.fetchPostResponse(url, paramMap);	
	}
	
	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		HashSet<String> resultSet = new HashSet<String>();	
		Document doc = Jsoup.parse(responseparser.responseToString(response));
		Element container = doc.getElementById("result-container");
		
		if(container == null) return;
		Elements results = container.select("a");		
		for(Element result: results) {
			//only returns wikipedia links
			resultSet.add(result.attr("href"));
		}
		question.setGoldenAnswers(resultSet);		
	}

	public static void main(String[] args) throws Exception {
		ASystem a = new SorokinQA();
		System.out.println(a.search("How many children did Benjamin Franklin have?", "en"));
	}
}
