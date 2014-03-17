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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

public class TagMe implements NERD_module {
	static Logger log = LoggerFactory.getLogger(TagMe.class);

	private String request = "http://tagme.di.unipi.it/tag";
	private String key = "";
	private String lang = "en";
	private String include_all_spots = "true";
	private String include_categories = "true";

	public TagMe() {

		try {
			Properties prop = new Properties();
			InputStream input=getClass().getClassLoader().getResourceAsStream("hawk.properties");
			prop.load(input);
			this.key = prop.getProperty("tagmekey");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String doTASK(String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&key=" + key;
		urlParameters += "&lang=" + lang;
		urlParameters += "&include_all_spots=" + include_all_spots;
		urlParameters += "&include_categories=" + include_categories;
		log.debug(urlParameters);
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
			// String foxJSONOutput =
			// "{\"timestamp\":\"2014-03-05T14:48:47\",\"time\":1,\"api\":\"tag\",\"annotations\":[{\"id\":537481,\"title\":\"Building code\",\"dbpedia_categories\":[\"Architecture\",\"Building engineering\",\"Construction law\",\"Real estate\",\"Safety codes\",\"Legal codes\"],\"start\":6,\"rho\":\"0.21168\",\"end\":15,\"spot\":\"buildings\"},{\"id\":1881,\"title\":\"Art Deco\",\"dbpedia_categories\":[\"Art Deco\",\"Art Deco architecture\",\"20th-century architectural styles\",\"Decorative arts\",\"Modern art\",\"Art movements\",\"Moderne architecture\"],\"start\":19,\"rho\":\"0.61866\",\"end\":27,\"spot\":\"art deco\"},{\"id\":8560,\"title\":\"Design\",\"dbpedia_categories\":[\"Design\",\"Architectural design\",\"Arts\"],\"start\":28,\"rho\":\"0.22216\",\"end\":33,\"spot\":\"style\"},{\"id\":3563046,\"title\":\"Shreve, Lamb and Harmon\",\"dbpedia_categories\":[\"Architecture firms based in New York\",\"Architecture firms of the United States\"],\"start\":38,\"rho\":\"0.62794\",\"end\":61,\"spot\":\"Shreve, Lamb and Harmon\"},{\"id\":8560,\"title\":\"Design\",\"dbpedia_categories\":[\"Design\",\"Architectural design\",\"Arts\"],\"start\":62,\"rho\":\"0.22595\",\"end\":68,\"spot\":\"design\"}],\"lang\":\"en\"}\r\n";
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(foxJSONOutput);

			JSONArray resources = (JSONArray) jsonObject.get("annotations");

			ArrayList<Entity> tmpList = new ArrayList<>();
			for (Object res : resources.toArray()) {
				JSONObject next = (JSONObject) res;
				Entity ent = new Entity();
				ent.label = (String) next.get("spot");
				ent.uris.add(new ResourceImpl(((String) next.get("title"))));
				JSONArray types = (JSONArray) next.get("dbpedia_categories");
				for (Object type : types) {
					ent.posTypesAndCategories.add(new ResourceImpl((String) type));
				}
				tmpList.add(ent);
			}
			String baseURI = "http://dbpedia.org/resource/";
			for (Entity entity : tmpList) {
				// hack to make underscores where spaces are
				Resource resource = entity.uris.get(0);
				entity.uris.add(new ResourceImpl(baseURI + resource.getURI().replace(" ", "_")));
				entity.uris.remove(0);
			}

			tmp.put("en", tmpList);

		} catch (ParseException | IOException e) {
			log.error("Could not call TagMe for NER/NED", e);
		}
		return tmp;
	}

	public static void main(String args[]) {
		Question q = new Question();
		// q.languageToQuestion.put("en", "Merkel met Obama?");
		q.languageToQuestion.put("en", "Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		NERD_module fox = new TagMe();
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
