package org.aksw.qa.systems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QANARY extends ASystem {

	Logger log = LoggerFactory.getLogger(QANARY.class);

	private String qanaryUrl = "https://wdaqua-qanary.univ-st-etienne.fr/gerbil-execute/wdaqua-core1,%20QueryExecuter/";

	public QANARY() {
		super();
	}
	
	public QANARY(String url) {
		super();
		this.qanaryUrl=url;
	}
	
	@Override
	public void search(final IQuestion question, final String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(qanaryUrl);

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("query", questionString));
		if (this.setLangPar) {
			params.add(new BasicNameValuePair("lang", language));
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		httppost.setEntity(entity);

		HttpResponse response = client.execute(httppost);

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
		return "qanary";
	}

	public String getQanaryUrl() {
		return qanaryUrl;
	}

	public void setQanaryUrl(final String qanaryUrl) {
		this.qanaryUrl = qanaryUrl;
	}

}
