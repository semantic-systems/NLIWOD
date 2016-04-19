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

public class START {

	public static void main(String[] args) throws URISyntaxException, ClientProtocolException, IOException{
		String question = "What is the highest Mountain?";
		HttpClient client = new DefaultHttpClient();
        URI uri = new URIBuilder().setScheme("http").setHost("start.csail.mit.edu").setPath("/justanswer.php").setParameter("query", question).build();
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = client.execute(httpget);
        
        Document doc = Jsoup.parse(responseToString(response));
        System.out.println(doc.select("span[type=reply]").text());
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