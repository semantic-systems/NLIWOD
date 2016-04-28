package org.aksw.qa.systems;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class START extends ASystem {
	Logger log = LoggerFactory.getLogger(START.class);
	public String name(){return "start";};

	public HashSet<String> search(String question) {
		log.debug(this.toString() + ": " + question);
		try {
			HttpClient client = HttpClientBuilder.create().build();
			URI uri = new URIBuilder().setScheme("http").setHost("start.csail.mit.edu").setPath("/justanswer.php").setParameter("query", question).build();
			HttpGet httpget = new HttpGet(uri);
			HttpResponse response = client.execute(httpget);

			Document doc = Jsoup.parse(responseToString(response));
			System.out.println(doc.select("span[type=reply]").text());

			// TODO return senseful answer from start
			// return resultSet;
		} catch (ClientProtocolException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (URISyntaxException e) {
			log.error(e.getLocalizedMessage(), e);
		}
        return new HashSet<String>();
	}
}
