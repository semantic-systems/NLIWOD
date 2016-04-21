package org.aksw.mlqa.systems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

public class Asker {
	
	public Asker(){
		
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
	
    public HashSet<String> askQakis(String question) throws ClientProtocolException, IOException {
    	
    	HashSet<String> result = new HashSet<String>();
    	String url = "http://qakis.org/qakis/index.xhtml";
        
    	HttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        HttpResponse ping = client.execute(httppost);

        Document vsdoc = Jsoup.parse(responseToString(ping));
        Elements el = vsdoc.select("input");
        String viewstate = (el.get(el.size()-1).attr("value"));

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("index_form", "index_form"));
        formparams.add(new BasicNameValuePair("index_form:question", question));
        formparams.add(new BasicNameValuePair("index_form:eps", ""));
        formparams.add(new BasicNameValuePair("index_form:submitQuestion", ""));
        formparams.add(new BasicNameValuePair("javax.faces.ViewState", viewstate));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        httppost.setEntity(entity);
        HttpResponse response = client.execute(httppost);

        Document doc = Jsoup.parse(responseToString(response));
        Elements answer = doc.select("div.global-presentation-details>h3>a");
        NodeVisitor nv  = new NodeVisitor (){
        	@Override
            public void tail(Node node, int depth) {
        		if(depth == 0)
        			result.add(node.attr("href"));
               }
        	@Override
            public void head(Node node, int depth) {
           
            };
        };
           
        //String query = doc.select("#sparqlQuery").toString().split("<br>")[1].replaceAll("&lt;", "<").replaceAll("&gt;", ">");
        //System.out.println(query);
        answer.traverse(nv); 
        return result;
    }
    
    public HashSet<String> askSina(String question) throws ClientProtocolException, IOException, URISyntaxException, IllegalStateException, ParseException {

    	HashSet<String> result = new HashSet<String>();
        HttpClient client = new DefaultHttpClient();
        URI uri = new URIBuilder().setScheme("http").setHost("sina.aksw.org").setPath("/api/rest/search").setParameter("q", question).build();
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = client.execute(httpget);
        JSONParser parser = new JSONParser();
        JSONArray answerjson = (JSONArray) parser.parse(responseToString(response));
        for(int i =0; i < answerjson.size(); i++){
        	JSONObject answer = (JSONObject) answerjson.get(i);
        	result.add((String) answer.get("URI_PARAM"));
        }
        return result;
    }

    /*
     * Not sure how to return START Answers..
     */
    
	public  void askSTART(String question) throws URISyntaxException, ClientProtocolException, IOException{
		HttpClient client = new DefaultHttpClient();
        URI uri = new URIBuilder().setScheme("http").setHost("start.csail.mit.edu").setPath("/justanswer.php").setParameter("query", question).build();
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = client.execute(httpget);
        
        Document doc = Jsoup.parse(responseToString(response));
        System.out.println(doc.select("span[type=reply]").text());        
	}
	
	public HashSet<String> askYoda(String question) throws ClientProtocolException, IOException, ParseException, InterruptedException {
    	
		HashSet<String> result = new HashSet<String>();
		
    	String url = "http://qa.ailao.eu/q";
        HttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", question));

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
        			if(Float.parseFloat(confj) > 0.70)
        				result.add(textj);        
        			}
        	}
        else
        	EntityUtils.consume(questionresponse.getEntity());
        }while(finished.equals("false"));
        
        return result;
    }
	
	public HashSet<String> askHAWK(String question) throws ClientProtocolException, IOException, ParseException, InterruptedException, URISyntaxException {
	
		HashSet<String> result = new HashSet<String>(); 
				
		HttpClient client = new DefaultHttpClient();
        URI iduri = new URIBuilder().setScheme("http").setHost("139.18.2.164:8181").setPath("/search").setParameter("q", question).build();
        HttpGet httpget = new HttpGet(iduri);
        HttpResponse idresponse = client.execute(httpget);
        
        String id = responseToString(idresponse);
	    JSONParser parser = new JSONParser();
	    
	    
        URI quri = new URIBuilder().setScheme("http").setHost("139.18.2.164:8181").setPath("/status").setParameter("UUID", id.substring(1, id.length()-2)).build();
        
        Boolean foundAnswer = false;
	    int j = 0;
	    do {
	    Thread.sleep(50);
	    HttpGet questionpost = new HttpGet(quri);
	    HttpResponse questionresponse = client.execute(questionpost);
	    JSONObject responsejson = (JSONObject) parser.parse(responseToString(questionresponse));
	    foundAnswer = responsejson.containsKey("answer");	
	    if(!foundAnswer.booleanValue())	
	    	EntityUtils.consume(questionresponse.getEntity());
	    else
	    	{ 
	    	JSONArray answerlist = (JSONArray) responsejson.get("answer");
	    	for (int i = 0; i < answerlist.size(); i++){
	    		JSONObject answer = (JSONObject) answerlist.get(i);
	    		result.add(answer.get("URI").toString());
	    		}
	    	}
	    j = j+1;
	    }while(!foundAnswer.booleanValue()&&j<500);
	    return result;
	}

}
