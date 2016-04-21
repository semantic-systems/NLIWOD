package org.aksw.qa.systems;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class HAWK extends ASystem {
	Logger log = LoggerFactory.getLogger(HAWK.class);

	public HashSet<String> search(String question) {
		log.debug(this.toString() + ": " + question);
		try {

			HashSet<String> result = new HashSet<String>();

			HttpClient client = HttpClientBuilder.create().build();
			URI iduri = new URIBuilder().setScheme("http").setHost("139.18.2.164:8181").setPath("/search").setParameter("q", question).build();
			HttpGet httpget = new HttpGet(iduri);
			HttpResponse idresponse = client.execute(httpget);

			String id = responseToString(idresponse);
			JSONParser parser = new JSONParser();

			URI quri = new URIBuilder().setScheme("http").setHost("139.18.2.164:8181").setPath("/status").setParameter("UUID", id.substring(1, id.length() - 2)).build();

			Boolean foundAnswer = false;
			int j = 0;
			do {
				Thread.sleep(50);
				HttpGet questionpost = new HttpGet(quri);
				HttpResponse questionresponse = client.execute(questionpost);
				JSONObject responsejson = (JSONObject) parser.parse(responseToString(questionresponse));
				foundAnswer = responsejson.containsKey("answer");
				if (!foundAnswer.booleanValue())
					EntityUtils.consume(questionresponse.getEntity());
				else {
					JSONArray answerlist = (JSONArray) responsejson.get("answer");
					for (int i = 0; i < answerlist.size(); i++) {
						JSONObject answer = (JSONObject) answerlist.get(i);
						result.add(answer.get("URI").toString());
					}
				}
				j = j + 1;
			} while (!foundAnswer.booleanValue() && j < 500);
			return result;
		} catch (ClientProtocolException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (URISyntaxException e) {
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
