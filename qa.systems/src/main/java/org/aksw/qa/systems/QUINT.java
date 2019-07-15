package org.aksw.qa.systems;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;


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
	}
	
	public QUINT(String url) {
		super(url, "quint", true, false);
	}

	@Override
	public HttpResponse fetchPostResponse(String url, Map<String, String> paramMap) throws ClientProtocolException, IOException {
		paramMap.put("dataResource", DATA_RESOURCE);
		paramMap.put("quintVariant", QUINT_VARIANT);
		paramMap.put("queriesNumber", QUERIES_NUMBER);
		paramMap.put("numberDecisionTrees", NUMBER_DECISION_TREES);
		return super.fetchPostResponse(url, paramMap);	
	}
}
