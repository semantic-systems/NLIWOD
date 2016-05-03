package org.aksw.qa.systems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
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
    Logger log = LoggerFactory.getLogger(YODA.class);

    /**
     * Time to wait between calls.
     */
    private static final long YODAY_WAIT_TIME = 1000;

    /**
     * timestamp of the last time YODA has been called.
     */
    private long lastCall = 0;

    public String name() {
        return "yoda";
    };

    public void search(IQuestion question) {
        String questionString;
        if (!question.getLanguageToQuestion().containsKey("en")) {
            return;
        }
        questionString = question.getLanguageToQuestion().get("en");
        log.debug(this.getClass().getSimpleName() + ": " + questionString);
        try {
            long timeToWait = (lastCall + YODAY_WAIT_TIME) - System.currentTimeMillis();
            if (timeToWait > 0) {
                Thread.sleep(timeToWait);
            }
            lastCall = System.currentTimeMillis();
            HashSet<String> result = new HashSet<String>();

            String url = "http://qa.ailao.eu/q";

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("text", questionString));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
            httppost.setEntity(entity);
            HttpResponse response = client.execute(httppost);

            JSONParser parser = new JSONParser();
            JSONObject idjson = (JSONObject) parser.parse(responseparser.responseToString(response));
            String id = idjson.get("id").toString();

            HttpGet questionpost = new HttpGet(url + "/" + id);

            String finished;
            do {
                Thread.sleep(50);
                HttpResponse questionresponse = client.execute(questionpost);
                JSONObject responsejson = (JSONObject) parser.parse(responseparser.responseToString(questionresponse));
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
            question.setGoldenAnswers(result);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
