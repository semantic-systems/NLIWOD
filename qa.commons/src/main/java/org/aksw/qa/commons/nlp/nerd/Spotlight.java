package org.aksw.qa.commons.nlp.nerd;

import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// submodule needs to be independent of other submodules, should have caching
public class Spotlight {
	private static final Logger log = LoggerFactory.getLogger(Spotlight.class);

	private String requestURL = "https://api.dbpedia-spotlight.org/en/annotate";
	private String confidence = "0.65";
	private String support = "20";

	protected String requestPOST(final String input, final String requestURL) {

		String output = "";
		try {
			output = post(input, requestURL);
		} catch (IOException e) {
			log.debug("Could not call Spotlight for NER/NED", e);
		}

		return output;
	}

	private String post(final String urlParameters, final String requestURL) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(requestURL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
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

		return sb.toString();
	}

	@Override
	public String toString() {
		String[] name = getClass().getName().split("\\.");
		return name[name.length - 1].substring(0, 3);
	}

	private String doTASK(final String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&confidence=" + confidence;
		urlParameters += "&support=" + support;

		return requestPOST(urlParameters, requestURL);
	}

	public Map<String, List<Entity>> getEntities(final String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<>();
		try {
			String JSONOutput = doTASK(question);

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(JSONOutput);

			JSONArray resources = (JSONArray) jsonObject.get("Resources");
			if (resources != null) {
				ArrayList<Entity> tmpList = new ArrayList<>();
				for (Object res : resources.toArray()) {
					JSONObject next = (JSONObject) res;
					Entity ent = new Entity();
					ent.setOffset(Integer.valueOf((String) next.get("@offset")));
					ent.setLabel((String) next.get("@surfaceForm"));
					String uri = ((String) next.get("@URI")).replaceAll(",", "%2C");
					ent.getUris().add(new ResourceImpl(uri));
					for (String type : ((String) next.get("@types")).split(",")) {
						ent.getPosTypesAndCategories().add(new ResourceImpl(type));
					}
					tmpList.add(ent);
				}
				tmp.put("en", tmpList);
			}
		} catch (IOException | ParseException e) {
			log.error("Could not call Spotlight for NER/NED", e);
		}
		return tmp;
	}

	public String getConfidence() {
		return confidence;
	}

	public void setConfidence(final double i) {
		this.confidence = String.valueOf(i);
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(final String support) {
		this.support = support;
	}
}
