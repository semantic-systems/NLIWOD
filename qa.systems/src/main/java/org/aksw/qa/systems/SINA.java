package org.aksw.qa.systems;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SINA extends ASystem {
	Logger log = LoggerFactory.getLogger(SINA.class);
	public String name(){return "sina";};

	public HashSet<String> search(String question) {
		log.debug(this.toString() + ": " + question);
		try {
			HashSet<String> resultSet = new HashSet<String>();

			HttpClient client = HttpClientBuilder.create().build();
			URI uri = new URIBuilder().setScheme("http").setHost("sina.aksw.org").setPath("/api/rest/search").setParameter("q", question).build();
			HttpGet httpget = new HttpGet(uri);
			HttpResponse response = client.execute(httpget);
			JSONParser parser = new JSONParser();
			JSONArray answerjson = (JSONArray) parser.parse(responseToString(response));
			for (int i = 0; i < answerjson.size(); i++) {
				JSONObject answer = (JSONObject) answerjson.get(i);
				resultSet.add((String) answer.get("URI_PARAM"));
			}
			return resultSet;
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (URISyntaxException e) {
			log.error(e.getLocalizedMessage(), e);
        } catch (IllegalStateException e) {
			log.error(e.getLocalizedMessage(), e);
        } catch (ParseException e) {
			log.error(e.getLocalizedMessage(), e);
        }
        return new HashSet<String>();
	}
}
