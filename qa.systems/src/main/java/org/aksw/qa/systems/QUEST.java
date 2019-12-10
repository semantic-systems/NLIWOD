package org.aksw.qa.systems;

import java.io.IOException;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class QUEST extends Gen_HTTP_QA_Sys {
	
	private static final String URL = "https://quest-sys.mpi-inf.mpg.de/getanswer";
	
	private static final String ACTION = "Answer_button";
	//possible values: 10-100
	private static final String NUMBER_OF_GSTS = "50";
	//Entity alignment possible values: 0.2-0.3
	private static final String ALIGN_THRES = "0.25";
	//Predicate Alignment possible values: 0.7-0.8
	private static final String PRED_ALIGN_THRES = "0.75";
	//Ranking Options possible values: 1-5
	private static final String RANKING_OPTIONS = "1";
	//Number of Documents possible values: 3, 5, 10
	private static final String DOC_OPTIONS = "10";
	//Type filtering possible values: 1 (yes), 0 (no)
	private static final String TYPE_OPTIONS = "0";
	
	public QUEST() {
		super(URL, "quest", true, false);
		this.setQueryKey("question");
		initParamMap();
	}
	
	public QUEST(String url) {
		super(url, "quest", true, false);
		this.setQueryKey("question");
		initParamMap();
	}

	private void initParamMap() {
		this.getParamMap().put("action", ACTION);
		this.getParamMap().put("Number_of_GSTs", NUMBER_OF_GSTS);
		this.getParamMap().put("Align_thres", ALIGN_THRES);
		this.getParamMap().put("pred_Align_thres", PRED_ALIGN_THRES);
		this.getParamMap().put("RankingOptions", RANKING_OPTIONS);
		this.getParamMap().put("docOptions", DOC_OPTIONS);
		this.getParamMap().put("TypeOptions", TYPE_OPTIONS);
	}
	
	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		HashSet<String> resultSet = new HashSet<String>();	
		Document doc = Jsoup.parse(responseparser.responseToString(response));
		//TODO: Try to get a better answer, returns different links to news sites, wikipedia etc.
		Element container = doc.getElementById("show_results");
		Elements refs = container.select("a");
		for(Element ref: refs) {
			String uri = ref.attr("href");
			if(uri.contains("en.wikipedia.org")) {
				resultSet.add(uri);
				break;
			}
		}
		question.setGoldenAnswers(resultSet);		
	}
}
