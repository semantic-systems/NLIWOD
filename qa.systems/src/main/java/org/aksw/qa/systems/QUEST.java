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

//not finished, webservice not working at the moment
public class QUEST extends Gen_HTTP_QA_Sys {
	
	private static final String URL = "https://quest-sys.mpi-inf.mpg.de/getanswer";
	
	//for more information check the information page on the website
	//training resource possible values: WQ (WebQuestions), F917 (Free917)
	private static final String ACTION = "Answer_button";
	//possible values: T (Typed), UN (Untyped)
	private static final String NUMBER_OF_GSTS = "50";
	//top-k queries possible values: 1, 3, 5
	private static final String ALIGN_THRES = "0.25";
	//Number of Decision Trees for LTR possible values: 40, 70, 90
	private static final String PRED_ALIGN_THRES = "0.75";
	//possible values: T (Typed), UN (Untyped)
	private static final String RANKING_OPTIONS = "1";
	//top-k queries possible values: 1, 3, 5
	private static final String DOC_OPTIONS = "10";
	//Number of Decision Trees for LTR possible values: 40, 70, 90
	private static final String TYPE_OPTIONS = "0";
	
	public QUEST() {
		super(URL, "quest", true, false);
		this.setQueryKey("question");
	}
	
	public QUEST(String url) {
		super(url, "quest", true, false);
		this.setQueryKey("question");
	}

	@Override
	public HttpResponse fetchPostResponse(String url, Map<String, String> paramMap) throws ClientProtocolException, IOException {
		paramMap.put("action", ACTION);
		paramMap.put("Number_of_GSTs", NUMBER_OF_GSTS);
		paramMap.put("Align_thres", ALIGN_THRES);
		paramMap.put("pred_Align_thres", PRED_ALIGN_THRES);
		paramMap.put("RankingOptions", RANKING_OPTIONS);
		paramMap.put("docOptions", DOC_OPTIONS);
		paramMap.put("TypeOptions", TYPE_OPTIONS);
		return super.fetchPostResponse(url, paramMap);	
	}
	
	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		HashSet<String> resultSet = new HashSet<String>();	
		Document doc = Jsoup.parse(responseparser.responseToString(response));
		System.out.println(doc.data());
//		Element container = doc.getElementById("result-container");
//		
//		if(container == null) return;
//		Elements results = container.select("a");		
//		for(Element result: results) {
//			//only returns wikipedia links
//			resultSet.add(result.attr("href"));
//		}
//		question.setGoldenAnswers(resultSet);		
	}
	
	public static void main(String[] args) throws Exception {
		ASystem a = new QUEST();
		System.out.println(a.search("Who played for FC Munich and was born in Karlsruhe?", "en"));
	}
}
