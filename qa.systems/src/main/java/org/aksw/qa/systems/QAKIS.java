package org.aksw.qa.systems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QAKIS extends ASystem {
	Logger log = LoggerFactory.getLogger(QAKIS.class);

	public String name() {
		return "qakis";
	};

	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);
		final HashSet<String> resultSet = new HashSet<String>();
		String url = "http://qakis.org/qakis/index.xhtml";

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost httppost = new HttpPost(url);
		HttpResponse ping = client.execute(httppost);
		//Test if error occured
		if(ping.getStatusLine().getStatusCode()>=400){
			throw new Exception("QAKIS Server could not answer due to: "+ping.getStatusLine());
		}
		
		Document vsdoc = Jsoup.parse(responseparser.responseToString(ping));
		Elements el = vsdoc.select("input");
		String viewstate = (el.get(el.size() - 1).attr("value"));

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("index_form", "index_form"));
		formparams.add(new BasicNameValuePair("index_form:question",
				questionString));
		formparams.add(new BasicNameValuePair("index_form:eps", ""));
		formparams.add(new BasicNameValuePair("index_form:submitQuestion", ""));
		formparams.add(new BasicNameValuePair("javax.faces.ViewState",
				viewstate));
		if(this.setLangPar){
			formparams.add(new BasicNameValuePair("index_form:language", language));
		}
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
				Consts.UTF_8);
		httppost.setEntity(entity);
		HttpResponse response = client.execute(httppost);

		Document doc = Jsoup.parse(responseparser.responseToString(response));
		Elements answer = doc.select("div.global-presentation-details>h3>a");
		NodeVisitor nv = new NodeVisitor() {
			public void tail(Node node, int depth) {
				if (depth == 0)
					resultSet.add(node.attr("href"));
			}

			public void head(Node arg0, int arg1) {
				// do nothing here
			}
		};
		answer.traverse(nv);
		question.setGoldenAnswers(resultSet);

		Elements codeElements = doc.select("div#sparqlQuery pre");
		if (codeElements.size() > 0) {
			Element sparqlElement = codeElements.get(0);
			Elements codeChildren = sparqlElement.children();
			for (Element c : codeChildren) {
				c.remove();
			}
			question.setSparqlQuery(sparqlElement.text());
		}
	}
}
