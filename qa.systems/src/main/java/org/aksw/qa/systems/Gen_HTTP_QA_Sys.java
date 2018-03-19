package org.aksw.qa.systems;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.json.EJAnswers;
import org.aksw.qa.commons.load.json.EJBinding;
import org.aksw.qa.commons.load.json.EJLanguage;
import org.aksw.qa.commons.load.json.EJQuestion;
import org.aksw.qa.commons.load.json.EJQuestionEntry;
import org.aksw.qa.commons.load.json.ExtendedJson;
import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gen_HTTP_QA_Sys extends ASystem{
	Logger log = LoggerFactory.getLogger(Gen_HTTP_QA_Sys.class);
	//String constants
	private String query_key = "query";
	private String lang_key = "lang";
	
	private String url;
	private Boolean isPostReq;
	private Map<String,String> paramMap;
	private String name;
	public Gen_HTTP_QA_Sys( String url, String name, Boolean isPostReq) {
		super();
		this.paramMap = new HashMap<>();
		this.name = name;
		this.url = url;
		this.isPostReq = isPostReq;
	}
	
	public Gen_HTTP_QA_Sys( String url, String name, Boolean isPostReq, Map<String, String> paramMap) {
		super();
		if(paramMap == null) {
			paramMap = new HashMap<>();
		}
		this.name = name;
		this.url = url;
		this.isPostReq = isPostReq;
		this.paramMap = paramMap;
	}
	
	private HttpResponse fetchPostResponse() throws ClientProtocolException, IOException {
		HttpResponse response = null;
		
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(this.url);

		List<NameValuePair> params = new ArrayList<>();
		
		for(String key: paramMap.keySet()) {
			params.add(new BasicNameValuePair(key, paramMap.get(key)));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		httppost.setEntity(entity);

		response = client.execute(httppost);
		return response;
	}
	
	private HttpResponse fetchGetResponse() throws URISyntaxException, ClientProtocolException, IOException {
		HttpResponse response = null;
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

		URIBuilder builder = new URIBuilder().setCustomQuery(this.url);
		for(String key: paramMap.keySet()) {
			builder.setParameter(key, paramMap.get(key));
		}
		
		URI uri = builder.build();
		HttpGet httpget = new HttpGet(uri);
		response = client.execute(httpget);
		return response;
	}

	@Override
	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);
		this.paramMap.put(query_key, questionString);
		if (this.setLangPar) {
			this.paramMap.put(lang_key, language);
		}
		HttpResponse response = isPostReq?fetchPostResponse():fetchGetResponse();

		//Test if error occured
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new Exception("QANARY Server could not answer due to: " + response.getStatusLine());
		}

		ExtendedJson json = (ExtendedJson) ExtendedQALDJSONLoader.readJson(response.getEntity().getContent(), ExtendedJson.class);

		for (EJQuestionEntry it : json.getQuestions()) {
			EJQuestion q = it.getQuestion();
			for (EJLanguage lang : q.getLanguage()) {
				question.setSparqlQuery(lang.getSparql());
				question.setPseudoSparqlQuery(lang.getPseudo());
			}
			EJAnswers answers = q.getAnswers();

			if (answers == null) {
				return;
			}
			if (answers.getBoolean() != null) {
				question.getGoldenAnswers().add(answers.getBoolean().toString());
			}
			if (answers.getResults() != null) {
				Vector<HashMap<String, EJBinding>> answerVector = answers.getResults().getBindings();
				for (HashMap<String, EJBinding> answerMap : answerVector) {
					for (EJBinding bind : answerMap.values()) {
						question.getGoldenAnswers().add(bind.getValue());
					}
				}
			}

		}
		
	}

	@Override
	public String name() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getIsPostReq() {
		return isPostReq;
	}

	public void setIsPostReq(Boolean isPostReq) {
		this.isPostReq = isPostReq;
	}

	public Map<String, String> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, String> paramMap) {
		this.paramMap = paramMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuery_key() {
		return query_key;
	}

	public void setQuery_key(String query_key) {
		this.query_key = query_key;
	}

	public String getLang_key() {
		return lang_key;
	}

	public void setLang_key(String lang_key) {
		this.lang_key = lang_key;
	}
	
}
