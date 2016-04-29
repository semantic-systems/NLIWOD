package org.aksw.qa.systems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public abstract class ASystem {

    public IQuestion search(String question) {
        IQuestion iQuestion = new Question();
        Map<String, String> langToQuestion = new HashMap<String, String>();
        langToQuestion.put("en", question);
        iQuestion.setLanguageToQuestion(langToQuestion);
        search(iQuestion);
        return iQuestion;
    }

    public abstract void search(IQuestion question);

    public abstract String name();

    protected String responseToString(HttpResponse response) throws IllegalStateException, IOException {
        HttpEntity entity = null;
        BufferedReader br = null;
        try {
            entity = response.getEntity();
            br = new BufferedReader(new InputStreamReader(entity.getContent()));
            // TODO use java 8 file IO API
            StringBuffer htmlResponse = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                htmlResponse.append(line).append("\n");
            }
            return htmlResponse.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                }
            }
        }
    }

}
