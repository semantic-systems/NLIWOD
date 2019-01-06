package org.aksw.qa.systems;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;

public class QASystem extends Gen_HTTP_QA_Sys {
	
	private static final String URL = "http://qald-beta.cs.upb.de:80/gerbil";
			
	public QASystem() {
		super(URL, "qasystem", true, false);
	}
	
	public QASystem(String url) {
		super(url, "qasystem", true, false);
	}
	
	@Override
	public HttpResponse fetchPostResponse(String url, Map<String, String> paramMap) throws ClientProtocolException, IOException {
		URIBuilder builder = new URIBuilder();
		for (String key : paramMap.keySet()) {
			builder.setParameter(key, paramMap.get(key));
		}
		return super.fetchPostResponse(url + builder.toString(), paramMap);	
	}
}
