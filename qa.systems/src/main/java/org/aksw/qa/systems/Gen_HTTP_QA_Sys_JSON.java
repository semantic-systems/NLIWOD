package org.aksw.qa.systems;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Gen_HTTP_QA_Sys_JSON extends ASystem {
	private Logger log = LoggerFactory.getLogger(Gen_HTTP_QA_Sys_JSON.class);
	
	// String constants
	private String url;
	private String name;

	public Gen_HTTP_QA_Sys_JSON(String url, String name) {
		super();
		this.url = url;
		this.name = name;
	}
	
	public String fetchPostResponse(String url, String json) throws Exception {
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(url);
		StringEntity entity = new StringEntity(json);
		httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
		httppost.setEntity(entity);
		
		HttpResponse response = client.execute(httppost);
		
		if(response.getStatusLine().getStatusCode()>=400){
			throw new Exception(this.name + " Server could not answer due to: " + response.getStatusLine());
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
		
		String responseString = fetchPostResponse(this.url, createInputJSON(questionString));
			
		processResponse(responseString, question);
	}

	public abstract String createInputJSON(String question);
	
	public abstract void processResponse(String response, IQuestion question);

	@Override
	public String name() {
		return name;
	}
}
