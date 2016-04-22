package org.aksw.mlqa.systems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YODA extends ASystem {
	Logger log = LoggerFactory.getLogger(YODA.class);
	public String name() { return "yoda";};
	public HashSet<String> search(String question) {
		log.debug(this.toString() + ": " + question);
		try {

			HashSet<String> result = new HashSet<String>();

			String url = "http://qa.ailao.eu/q";

			HttpClient client = HttpClientBuilder.create().build();
			HttpPost httppost = new HttpPost(url);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("text", question));

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
			httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);

			JSONParser parser = new JSONParser();
			JSONObject idjson = (JSONObject) parser.parse(responseToString(response));
			String id = idjson.get("id").toString();

			HttpGet questionpost = new HttpGet(url + "/" + id);

			String finished;
			do {
				Thread.sleep(50);
				HttpResponse questionresponse = client.execute(questionpost);
				JSONObject responsejson = (JSONObject) parser.parse(responseToString(questionresponse));
				finished = responsejson.get("finished").toString();
				if (finished.equals("true")) {
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
			} while (finished.equals("false"));

			return result;
		} catch (ClientProtocolException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (IllegalStateException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (ParseException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (InterruptedException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return Sets.newHashSet();
	}
}