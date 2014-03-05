package org.aksw.hawk.nlp.spotter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class WikipediaMiner implements NERD_module {
	static Logger log = LoggerFactory.getLogger(WikipediaMiner.class);

	private String request = "http://wikipedia-miner.cms.waikato.ac.nz/services/wikify";
	private String repeatMode = "all";
	private String responseFormat = "json";

	public WikipediaMiner() {
	}

	private String doTASK(String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "source=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&responseFormat=" + responseFormat;
		urlParameters += "&repeatMode=" + repeatMode;

		URL url = new URL(request);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
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
		log.debug(sb.toString());
		return sb.toString();
	}

	@Override
	public Map<String, List<Entity>> getEntities(String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<String, List<Entity>>();
		try {
			String foxJSONOutput = doTASK(question);

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(foxJSONOutput);

			JSONArray resources = (JSONArray) jsonObject.get("detectedTopics");

			ArrayList<Entity> tmpList = new ArrayList<>();
			for (Object res : resources.toArray()) {
				JSONObject next = (JSONObject) res;
				Entity ent = new Entity();
				ent.uris.add(new ResourceImpl(((String) next.get("title"))));
				tmpList.add(ent);
			}
			tmp.put("en", tmpList);
			Pattern TAG_REGEX = Pattern.compile("\\[\\[(.+?)\\]\\]");
			String wikifiedText = (String) jsonObject.get("wikifiedDocument");
			Matcher matcher = TAG_REGEX.matcher(wikifiedText);

			while (matcher.find()) {
				String[] uriLabel = matcher.group(1).split("\\|");
				Collections.sort(tmpList);
				Collections.reverse(tmpList);
				for (Entity entity : tmpList) {
					String resource = entity.uris.get(0).getURI();
					if (uriLabel.length == 1) {
						  if (resource.toLowerCase().equals(uriLabel[0].toLowerCase())) {
							entity.label = uriLabel[0];
						}
					} else {
						if (resource.equals(uriLabel[0])) {
							entity.label = uriLabel[1];
						}
					}
				}
			}
			//  eliminate entities that still have no label, because they are the shorter ones of overlaps

			for (int i=tmpList.size()-1; i>=0; --i){
				if(tmpList.get(i).label.equals("")){
					tmpList.remove(i);
				}
			}
			
			String baseURI = "http://dbpedia.org/resource/";
			for (Entity entity : tmpList) {
				//hack to make underscores where spaces are
				Resource resource = entity.uris.get(0);
				entity.uris.add(new ResourceImpl(baseURI + resource.getURI().replace(" ", "_")));
				entity.uris.remove(0);
			}
			
			

		} catch (IOException | ParseException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return tmp;
	}

	public static void main(String args[]) {
		Question q = new Question();
		//q.languageToQuestion.put("en", "Which street basketball player was diagnosed with Sarcoidosis?");
		//q.languageToQuestion.put("en", "Which recipients of the Victoria Cross died in the Battle of Arnhe");
		 q.languageToQuestion.put("en", "Under which king did the British prime minister that signed the Munich agreement serve?");
//		 q.languageToQuestion.put("en", "Which anti-apartheid activist graduated from the University of South Africa?");
		NERD_module fox = new WikipediaMiner();
		q.languageToNamedEntites = fox.getEntities(q.languageToQuestion.get("en"));
		for (String key : q.languageToNamedEntites.keySet()) {
			System.out.println(key);
			for (Entity entity : q.languageToNamedEntites.get(key)) {
				System.out.println("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTypesAndCategories) {
					System.out.println("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					System.out.println("\t\turi: " + r);
				}
			}
		}
	}
}
