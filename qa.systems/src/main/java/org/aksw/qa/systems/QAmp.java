package org.aksw.qa.systems;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QAmp extends Gen_HTTP_QA_Sys {

    private static final String URL = "https://kbqa-api.ai.wu.ac.at/ask";

    public QAmp() {
        super(URL, "qamp", false, false);
        this.setQueryKey("question");
    }

    public QAmp(String url) {
        super(url, "qamp", false, false);
        this.setQueryKey("question");
    }

    @Override
    public void processQALDResp(HttpResponse response, IQuestion question)
            throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
        JSONParser parser = new JSONParser();
        JSONObject answerjson = null;
        String res = responseparser.responseToString(response);
        try {
            answerjson = (JSONObject) parser.parse(res);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        HashSet<String> resultSet = new HashSet<String>();
        if(answerjson.get("answers") instanceof JSONArray) {
            JSONArray answers = (JSONArray) answerjson.get("answers");
            for (int i = 0; i < answers.size(); i++) {
                JSONObject answer = (JSONObject) answers.get(i);
                Iterator<?> iter = answer.keySet().iterator();
                while(iter.hasNext()) {
                    resultSet.add((String) iter.next());
                }
            }
        } else {
            Boolean answer = (Boolean) answerjson.get("answers");
            resultSet.add(String.valueOf(answer));
        }
        question.setGoldenAnswers(resultSet);	
    }
}