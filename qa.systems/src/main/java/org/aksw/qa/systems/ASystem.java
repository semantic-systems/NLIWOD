package org.aksw.qa.systems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public abstract class ASystem {

    public abstract HashSet<String> search(String question);

    public String responseToString(HttpResponse response) throws IllegalStateException, IOException {
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
