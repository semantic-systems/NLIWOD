package org.aksw.qa.annotation.spotter;

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

import com.google.common.base.Joiner;

public class Fox extends ASpotter {
	private static Logger log = LoggerFactory.getLogger(Fox.class);

	private String requestURL = "http://fox.cs.uni-paderborn.de:4444/fox";
	private String outputFormat = "RDF/JSON";
	private String taskType = "ner";
	private String inputType = "text";
	private String contentType = "application/json; charset=utf-8";
	
	@SuppressWarnings("unchecked")
	private String doTask(final String inputText) {
		JSONObject urlParameters = new JSONObject();

		urlParameters.put("type", inputType);
		urlParameters.put("task", taskType);
		urlParameters.put("lang", "en");

		urlParameters.put("output", outputFormat);
		urlParameters.put("input", inputText);
		return requestPOST(urlParameters.toString(), requestURL, contentType);	
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.aksw.hawk.nlp.NERD_module#getEntities(java.lang.String)
	 */
	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		HashMap<String, List<Entity>> mappedEntitiesReturn = new HashMap<>();
		String foxJSONOutput = null;

		foxJSONOutput = doTask(question);
		if (!foxJSONOutput.equals("") && (!(foxJSONOutput == null))) {
			try {
				JSONParser parser = new JSONParser();
				JSONObject jsonArray = (JSONObject) parser.parse(foxJSONOutput);
				
				ArrayList<Entity> tmpList = new ArrayList<>();
				for(Object key: jsonArray.keySet()) {
					JSONObject json = (JSONObject) jsonArray.get(key);
					if(json.keySet().contains("http://www.w3.org/2005/11/its/rdf#taIdentRef")) {
						Entity ent = new Entity();
						
						JSONArray uriArray = (JSONArray) json.get("http://www.w3.org/2005/11/its/rdf#taIdentRef");
						String uri = (String) ((JSONObject) uriArray.get(0)).get("value");
						String encode = uri.replaceAll(",", "%2C");
						ResourceImpl e = new ResourceImpl(encode);
						ent.getUris().add(e);

						JSONArray typeArray = (JSONArray) json.get("http://www.w3.org/2005/11/its/rdf#taClassRef");
						for(int i = 0; i<typeArray.size(); i++) {
							JSONObject types = (JSONObject) typeArray.get(i);
							encode = ((String) types.get("value")).replaceAll(",", "%2C");
							e = new ResourceImpl(encode);
							ent.getPosTypesAndCategories().add(e);
						}
											
						JSONArray labelArray = (JSONArray) json.get("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf");
						String label = (String) ((JSONObject) labelArray.get(0)).get("value");
						ent.setLabel(label);
						
						JSONArray offsetArray = (JSONArray) json.get("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex");
						String offset = (String) ((JSONObject) offsetArray.get(0)).get("value");
						ent.setOffset(Integer.parseInt(offset));
							
						tmpList.add(ent);
					}
				}
				mappedEntitiesReturn.put("en", tmpList);
			} catch (ParseException e) {
				log.error("Could not parse Server rensponse", e);
			}
		}

		if (!mappedEntitiesReturn.isEmpty()) {
			log.debug("\t" + Joiner.on("\n").join(mappedEntitiesReturn.get("en")));
		}
		return mappedEntitiesReturn;
	}
}
