package org.aksw.mlqa.systems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;


public class Sina {


    public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException {

        String question = "Who shot Franz Ferdinand?";

        HttpClient client = new DefaultHttpClient();
        URI uri = new URIBuilder().setScheme("http").setHost("sina.aksw.org").setPath("/search.xhtml").setParameter("q", question).setParameter("content", "sparql").build();
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = client.execute(httpget);
        Document doc = Jsoup.parse(responseToString(response));
        
        
        NodeVisitor nd  = new NodeVisitor (){

              @Override
              public void tail(Node node, int depth) {
                  if((node.nodeName().equals("a")) && (node.attr("id").contains("page")) ){
                      String fullstring = node.attr("href");
                      String sparql = fullstring.substring(75);
                      System.out.println(sparql);
                  }
                 }

              @Override
              public void head(Node node, int depth) {
              };


        };
        doc.traverse(nd);
        System.out.println(doc.toString());
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