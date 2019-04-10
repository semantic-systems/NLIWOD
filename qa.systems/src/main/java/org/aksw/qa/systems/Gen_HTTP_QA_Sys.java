package org.aksw.qa.systems;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.aksw.qa.commons.load.json.QaldJson;
import org.aksw.qa.commons.load.json.QaldQuery;
import org.aksw.qa.commons.load.json.QaldQuestionEntry;
import org.apache.commons.io.IOUtils;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Gen_HTTP_QA_Sys extends ASystem {
	private Logger log = LoggerFactory.getLogger(Gen_HTTP_QA_Sys.class);
	// String constants
	private String query_key = "query";
	private String lang_key = "lang";

	private String url;
	private Boolean isPostReq;
	private Boolean isEQALD;
	private Map<String, String> paramMap;
	private String name;

	public Gen_HTTP_QA_Sys(String url, String name, Boolean isPostReq, Boolean isEQALD) {
		super();
		this.paramMap = new HashMap<>();
		this.name = name;
		this.url = url;
		this.isPostReq = isPostReq;
		this.isEQALD = isEQALD;
	}
	
	public HttpResponse fetchPostResponse(String url, Map<String, String> paramMap) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> params = new ArrayList<>();
		for (String key : paramMap.keySet()) {
			params.add(new BasicNameValuePair(key, paramMap.get(key)));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		httppost.setEntity(entity);
		response = client.execute(httppost);
		return response;
	}

	public HttpResponse fetchGetResponse(String url, Map<String, String> paramMap) throws URISyntaxException, ClientProtocolException, IOException {
		HttpResponse response = null;
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

		URIBuilder builder = new URIBuilder();
		for (String key : paramMap.keySet()) {
			builder.setParameter(key, paramMap.get(key));
		}
		URI uri = new URI(url + builder.toString());
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
		HttpResponse response = isPostReq ? fetchPostResponse(this.url, this.paramMap)
				: fetchGetResponse(this.url, this.paramMap);

		// Test if error occured
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new Exception(this.name+" Server could not answer due to: " + response.getStatusLine());
		}
		//Checking if expected format is EQALD or QALD
		if(this.isEQALD)
			processEQALDResponse(response, question);
		else
			processQALDResp(response, question);		
	}

	/**
	 * Method to process a QALD Based http response and set the details in
	 * 'IQuestion' Formatting based on QALD as mentioned :
	 * https://github.com/dice-group/gerbil/wiki/Question-Answering
	 * 
	 * @param response
	 *            - response to be processed
	 * @param question
	 *            - IQuestion instance to be set
	 * @throws IOException
	 * @throws ParseException
	 * @throws IllegalStateException
	 */
	@Deprecated
	public void processQALDResponse(HttpResponse response, IQuestion question)
			throws IllegalStateException, ParseException, IOException {
		JSONParser parser = new JSONParser();

		JSONObject responsejson = (JSONObject) parser.parse(responseparser.responseToString(response));
		JSONArray questionsArr = (JSONArray) responsejson.get("questions");
		if (questionsArr != null && questionsArr.size() > 0) {
			JSONObject questionsObj = (JSONObject) questionsArr.get(0);
			if (questionsObj != null && questionsObj.containsKey("query")) {
				JSONObject queryJson = (JSONObject) questionsObj.get("query");
				String sparqlQuery = queryJson.get("sparql").toString();
				sparqlQuery = new String(sparqlQuery);
				question.setSparqlQuery(sparqlQuery);
			}
			if (questionsObj.containsKey("answers")) {
				JSONArray answerlist = (JSONArray) questionsObj.get("answers");
				JSONObject answersJson = (JSONObject) answerlist.get(0);

				HashSet<String> vars = new HashSet<String>();
				JSONObject headJson = (JSONObject) answersJson.get("head");
				if (headJson != null && headJson.containsKey("vars")) {
					JSONArray varsArr = (JSONArray) headJson.get("vars");
					for (int i = 0; i < varsArr.size(); i++) {
						vars.add(varsArr.get(i).toString());
					}

					JSONObject resultsJson = (JSONObject) answersJson.get("results");
					JSONArray bindingsArr = (JSONArray) resultsJson.get("bindings");
					HashSet<String> result = new HashSet<String>();
					for (int i = 0; i < bindingsArr.size(); i++) {
						JSONObject answer = (JSONObject) bindingsArr.get(i);
						for (String varLabel : vars) {
							JSONObject uriJson = (JSONObject) answer.get(varLabel);
							if (uriJson.containsKey("value"))
								result.add(uriJson.get("value").toString());
						}
					}
					question.setGoldenAnswers(result);
				}
			}
		}

	}
	/**
	 * Method to process a QALD Based http response and set the details in
	 * 'IQuestion'.
	 * 
	 * @param response
	 *            - response to be processed
	 * @param question
	 *            - IQuestion instance to be set
	 * @throws IOException
	 * @throws ParseException
	 * @throws IllegalStateException
	 */
	public void processQALDResp(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		QaldJson qaldJson = (QaldJson) ExtendedQALDJSONLoader.readJson(response.getEntity().getContent(),QaldJson.class);
		//Fetch all answers
		for (QaldQuestionEntry it : qaldJson.getQuestions()) {
			QaldQuery qry = it.getQuery();
			if (qry != null) {
				question.setSparqlQuery(qry.getSparql());
				question.setPseudoSparqlQuery(qry.getPseudo());
			}
			if (it.getAnswers() != null && it.getAnswers().size() > 0) {
				EJAnswers answers = it.getAnswers().get(0);
				if (answers == null) return;
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
	}

	/**
	 * Method to process a EQALD Based http response and set the details in
	 * 'IQuestion'
	 * 
	 * @param response
	 * @param question
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	public void processEQALDResponse(HttpResponse response, IQuestion question) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		//System.out.println(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
		ExtendedJson json = (ExtendedJson) ExtendedQALDJSONLoader.readJson(response.getEntity().getContent(),ExtendedJson.class);
		for (EJQuestionEntry it : json.getQuestions()) {
			EJQuestion q = it.getQuestion();
			for (EJLanguage lang : q.getLanguage()) {
				question.setSparqlQuery(lang.getSparql());
				question.setPseudoSparqlQuery(lang.getPseudo());
			}
			EJAnswers answers = q.getAnswers();
			if (answers == null) return;
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

	public String getQueryKey() {
		return query_key;
	}

	public void setQueryKey(String query_key) {
		this.query_key = query_key;
	}

	public String getLangKey() {
		return lang_key;
	}

	public void setLangKey(String lang_key) {
		this.lang_key = lang_key;
	}

	public Boolean getIsEQALD() {
		return isEQALD;
	}

	public void setIsEQALD(Boolean isEQALD) {
		this.isEQALD = isEQALD;
	}

}
