package org.aksw.qa.annotation.spotter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spotlight extends ASpotter {
	private static Logger log = LoggerFactory.getLogger(Spotlight.class);

	private String requestURL = "http://model.dbpedia-spotlight.org/en/annotate";
	private String confidence = "0.5";
	private String support = "0";
	private String contentType = "application/x-www-form-urlencoded;charset=UTF-8";

	private String doTASK(final String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&confidence=" + confidence;
		urlParameters += "&support=" + support;

		return requestPOST(urlParameters, requestURL, contentType);
	}

	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<>();
		try {
			String foxJSONOutput = doTASK(question);

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(foxJSONOutput);

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
