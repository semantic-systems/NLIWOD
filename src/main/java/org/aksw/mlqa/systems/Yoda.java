package org.aksw.mlqa.systems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Yoda {
    private static String url = "http://qa.ailao.eu/q";


    public static void main(String[] args) throws ClientProtocolException, IOException, ParseException, InterruptedException {



        HttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", "Who wrote the Hitchikers Guide to the galaxy?"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
        httppost.setEntity(entity);
        HttpResponse response = client.execute(httppost);
        

        
        JSONParser parser = new JSONParser();
        JSONObject idjson = (JSONObject) parser.parse(responseToString(response));
        String id =  idjson.get("id").toString();

        HttpGet questionpost = new HttpGet(url + "/" + id);
        
        String finished;
        do {
        Thread.sleep(50);
        HttpResponse questionresponse = client.execute(questionpost);
        JSONObject responsejson = (JSONObject) parser.parse(responseToString(questionresponse));
        finished = responsejson.get("finished").toString();
        if(finished.equals("true"))	
        	{ 
        	JSONArray answer = (JSONArray) responsejson.get("answers");
        	for (int j=0; j < answer.size(); j++ ){
        			JSONObject answerj = (JSONObject) answer.get(j);
        			String textj = answerj.get("text").toString();
        			String confj = answerj.get("confidence").toString();
        			System.out.println(confj + " " + textj);
        			}
        	System.out.println(responsejson.get("answerSentence"));
        	
        	}
        else
        	EntityUtils.consume(questionresponse.getEntity());
        }while(finished.equals("false"));
    }

    
    
    private static String responseToString(HttpResponse response) throws IllegalStateException, IOException{
    	        BufferedReader br = new BufferedReader(new InputStreamReader(response
                .getEntity().getContent()));
        StringBuffer htmlResponse = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) != null) {
            htmlResponse.append(line).append("\n");
        }
        return htmlResponse.toString();
    }

}