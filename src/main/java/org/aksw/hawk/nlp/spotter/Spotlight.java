package org.aksw.hawk.nlp.spotter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class Spotlight implements NERD_module {
	static Logger log = LoggerFactory.getLogger(Spotlight.class);

	private String request = "http://spotlight.dbpedia.org/rest/annotate";
	private String confidence = "0.2";
	private String support = "20";

	public Spotlight() {
	}

	private String doTASK(String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&confidence=" + confidence;
		urlParameters += "&support=" + support;

		URL url = new URL(request);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.length()));

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();

		InputStream inputStream = connection.getInputStream();
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(in);

		StringBuilder sb = new StringBuilder();
		while (reader.ready()) {
			sb.append(reader.readLine());
		}

		wr.close();
		reader.close();
		connection.disconnect();
		System.out.println(sb.toString());
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.hawk.nlp.NERD_module#getEntities(java.lang.String)
	 */
	@Override
	public Map<String, List<Entity>> getEntities(String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<String, List<Entity>>();
		try {
			String foxJSONOutput = doTASK(question);

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(foxJSONOutput);

			JSONArray resources = (JSONArray) jsonObject.get("Resources");

			ArrayList<Entity> tmpList = new ArrayList<>();
			for (Object res : resources.toArray()) {
				JSONObject next = (JSONObject) res;
				Entity ent = new Entity();
				ent.label = (String) next.get("@surfaceForm");
				ent.uris.add(new ResourceImpl((String) next.get("@URI")));
				for (String type : ((String) next.get("@types")).split(",")) {
					ent.types.add(new ResourceImpl(type));
				}
				tmpList.add(ent);
			}
			tmp.put("en", tmpList);

		} catch (IOException | ParseException e) {
			log.error("Could not call Spotlight for NER/NED", e);
		}
		return tmp;
	}

	public static void main(String args[]) {
		Question q = new Question();
		q.languageToQuestion.put("en", "Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		NERD_module fox = new Spotlight();
		q.languageToNamedEntites = fox.getEntities(q.languageToQuestion.get("en"));
		for (String key : q.languageToNamedEntites.keySet()) {
			System.out.println(key);
			for (Entity entity : q.languageToNamedEntites.get(key)) {
				System.out.println("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.types) {
					System.out.println("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					System.out.println("\t\turi: " + r);
				}
			}
		}
	}
}
