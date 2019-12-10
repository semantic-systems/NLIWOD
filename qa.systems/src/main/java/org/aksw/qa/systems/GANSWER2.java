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

public class GANSWER2 extends Gen_HTTP_QA_Sys {
	
	private static final String URL = "http://ganswer.gstore-pku.com//result2.jsp";
	
	public GANSWER2() {
		super(URL, "gAnswer2", false, false);
		this.setQueryKey("question");
	}
	
	public GANSWER2(String url) {
		super(url, "gAnswer2", false, false);
		this.setQueryKey("question");
	}

	@Override
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		HashSet<String> resultSet = new HashSet<String>();	
		Document doc = Jsoup.parse(responseparser.responseToString(response));
		
		Element container = doc.getElementById("myTabContent");
		Element table = container.select("table").get(0);
		if(table.getElementById("hit") == null) return;

		Elements rows = table.select("tr");
		for(Element row: rows) {
			Elements cols = row.select("td");
			for(Element col: cols){
				Element hit = col.getElementById("hit");
				Element url = col.getElementById("entity_name");
				Element value = col.getElementById("values");
				if(url != null && value != null) {
					resultSet.add(value.ownText());
				} else if(url != null) {
					resultSet.add(url.attr("href"));
				} else {
					resultSet.add(hit.ownText());
				}
			}
		}
		question.setGoldenAnswers(resultSet);	
	}
}