package org.aksw.qa.systems;

import java.net.URI;
import java.util.HashSet;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SINA extends ASystem {
    Logger log = LoggerFactory.getLogger(SINA.class);

    public String name() {
        return "sina";
    };

    public void search(IQuestion question) {
        String questionString;
        if (!question.getLanguageToQuestion().containsKey("en")) {
            return;
        }
        questionString = question.getLanguageToQuestion().get("en");
        log.debug(this.getClass().getSimpleName() + ": " + questionString);
        try {
            HashSet<String> resultSet = new HashSet<String>();

            HttpClient client = HttpClientBuilder.create().build();
            URI uri = new URIBuilder().setScheme("http").setHost("sina.aksw.org").setPath("/api/rest/search")
                    .setParameter("q", questionString).build();
            HttpGet httpget = new HttpGet(uri);
            HttpResponse response = client.execute(httpget);
            JSONParser parser = new JSONParser();
            JSONArray answerjson = (JSONArray) parser.parse(responseToString(response));
            for (int i = 0; i < answerjson.size(); i++) {
                JSONObject answer = (JSONObject) answerjson.get(i);
                resultSet.add((String) answer.get("URI_PARAM"));
            }
            question.setGoldenAnswers(resultSet);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
