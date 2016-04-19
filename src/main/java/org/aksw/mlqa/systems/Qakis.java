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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;


public class Qakis {
    private static String url = "http://qakis.org/qakis/index.xhtml";


    public static void main(String[] args) throws ClientProtocolException, IOException {



        HttpClient client = new DefaultHttpClient();


        HttpPost httppost = new HttpPost(url);
        HttpResponse ping = client.execute(httppost);

        Document vsdoc = Jsoup.parse(responseToString(ping));
        Elements el = vsdoc.select("input");
        String viewstate = (el.get(el.size()-1).attr("value"));

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("index_form", "index_form"));
        formparams.add(new BasicNameValuePair("index_form:question", "Who are the Astronauts of NASA?"));
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
            		System.out.println(node.attr("href"));
               }

            @Override
            public void head(Node node, int depth) {
            };
        };
           
      String query = doc.select("#sparqlQuery").toString().split("<br>")[1].replaceAll("&lt;", "<").replaceAll("&gt;", ">");
      System.out.println(query);
      answer.traverse(nv);  
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
