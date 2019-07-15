package org.aksw.qa.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class ResponseToStringParser {
	
    public String responseToString(HttpResponse response) throws IllegalStateException, IOException {
        HttpEntity entity = null;
        BufferedReader br = null;
        try {
            entity = response.getEntity();
            br = new BufferedReader(new InputStreamReader(entity.getContent()));
            return br.lines().collect(Collectors.joining());
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
