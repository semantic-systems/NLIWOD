package org.aksw.qa.systems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YODA extends ASystem {
	private Logger log = LoggerFactory.getLogger(YODA.class);

	/**
	 * Time to wait between calls.
	 */
	private static final long YODAY_WAIT_TIME = 1000;

	/**
	 * timestamp of the last time YODA has been called.
	 */
	private long lastCall = 0;

	private String url = "http://yodaqa.felk.cvut.cz:4567/q";

	@Override
	public String name() {
		return "yoda";
	};

	@Override
	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);

		long timeToWait = (lastCall + YODAY_WAIT_TIME)
				- System.currentTimeMillis();
		if (timeToWait > 0) {
			Thread.sleep(timeToWait);
		}
		lastCall = System.currentTimeMillis();
		HashSet<String> result = new HashSet<String>();



		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("text", questionString));
		if(this.setLangPar){
			params.add(new BasicNameValuePair("lang", language));
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,
				Consts.UTF_8);
		httppost.setEntity(entity);
		HttpResponse response = client.execute(httppost);
		// Test if error occured
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new Exception("YODA Server could not answer due to: "
					+ response.getStatusLine());
		}

		JSONParser parser = new JSONParser();
		JSONObject idjson = (JSONObject) parser.parse(responseparser
				.responseToString(response));
		String id = idjson.get("id").toString();

		HttpGet questionpost = new HttpGet(url + "/" + id);

		String finished;
		do {
			Thread.sleep(50);
			HttpResponse questionresponse = client.execute(questionpost);
			JSONObject responsejson = (JSONObject) parser.parse(responseparser
					.responseToString(questionresponse));
			finished = responsejson.get("finished").toString();
			if ("true".equals(finished)) {
				JSONArray answer = (JSONArray) responsejson.get("answers");
				for (int j = 0; j < answer.size(); j++) {
					JSONObject answerj = (JSONObject) answer.get(j);
					String textj = answerj.get("text").toString();
					String confj = answerj.get("confidence").toString();
					if (Float.parseFloat(confj) > 0.70)
						result.add(textj);
				}
			} else
				EntityUtils.consume(questionresponse.getEntity());
		} while ("false".equals(finished));
		question.setGoldenAnswers(result);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
