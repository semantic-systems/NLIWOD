package org.aksw.hawk.spotter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spotlight extends ASpotter {
	static Logger log = LoggerFactory.getLogger(Spotlight.class);

	private String requestURL = "http://spotlight.sztaki.hu:2222/rest/annotate";
	private String confidence = "0.65";
	private String support = "20";

	public Spotlight() {
	}

	private String doTASK(final String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&confidence=" + confidence;
		urlParameters += "&support=" + support;

		return requestPOST(urlParameters, requestURL);
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
					// FIXME implement offset also for other spotters, write a
					// test that each spotter returns an offset
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

	// TODO Christian: Unit Test
	public static void main(final String args[]) {
		HAWKQuestion q = new HAWKQuestion();
		// q.getLanguageToQuestion().put("en",
		// "Which buildings in art deco style did Shreve, Lamb and Harmon
		// design?");
		// q.getLanguageToQuestion().put("en",
		// "Which anti-apartheid activist was born in Mvezo?");
		q.getLanguageToQuestion().put("en", " Who was vice president under the president who approved the use of atomic weapons against Japan during World War II?");
		ASpotter spotter = new Spotlight();

		for (double i = 0; i <= 1.0; i += 0.05) {
			((Spotlight) spotter).setConfidence(i);
			System.out.println("Confidence: " + ((Spotlight) spotter).getConfidence());
			q.setLanguageToNamedEntites(spotter.getEntities(q.getLanguageToQuestion().get("en")));
			for (String key : q.getLanguageToNamedEntites().keySet()) {
				System.out.println(key);
				for (Entity entity : q.getLanguageToNamedEntites().get(key)) {
					System.out.println("\t" + entity.getLabel() + " ->" + entity.getType());
					for (Resource r : entity.getPosTypesAndCategories()) {
						System.out.println("\t\tpos: " + r);
					}
					for (Resource r : entity.getUris()) {
						System.out.println("\t\turi: " + r);
					}
				}
			}
		}
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
