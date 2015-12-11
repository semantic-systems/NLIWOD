package org.aksw.mlqa.analyzer.querytype;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fox extends ASpotter {
	static Logger log = LoggerFactory.getLogger(Fox.class);

	// private String requestURL = "http://139.18.2.164:4444/api";
	private String requestURL = "http://fox-demo.aksw.org/api";
	private String outputFormat = "N-Triples";
	private String taskType = "NER";
	private String inputType = "text";

	private String doTASK(String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "type=" + inputType;
		urlParameters += "&task=" + taskType;
		urlParameters += "&output=" + outputFormat;
		urlParameters += "&input=" + URLEncoder.encode(inputText, "UTF-8");

		return requestPOST(urlParameters, requestURL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.hawk.nlp.NERD_module#getEntities(java.lang.String)
	 */
	@Override
	public Map<String, List<Entity>> getEntities(String question) throws MalformedURLException, ProtocolException, IOException, ParseException {
		HashMap<String, List<Entity>> tmp = new HashMap<String, List<Entity>>();
		String foxJSONOutput = doTASK(question);
		log.debug(foxJSONOutput);
		JSONParser parser = new JSONParser();
		JSONObject jsonArray = (JSONObject) parser.parse(foxJSONOutput);
		String output = URLDecoder.decode((String) jsonArray.get("output"), "UTF-8");
		log.debug(output);
		String baseURI = "http://dbpedia.org";
		Model model = ModelFactory.createDefaultModel();
		RDFReader r = model.getReader("N3");
		r.read(model, new StringReader(output), baseURI);

		ResIterator iter = model.listSubjects();
		ArrayList<Entity> tmpList = new ArrayList<>();
		while (iter.hasNext()) {
			Resource next = iter.next();
			StmtIterator statementIter = next.listProperties();
			Entity ent = new Entity();
			while (statementIter.hasNext()) {
				Statement statement = statementIter.next();
				String predicateURI = statement.getPredicate().getURI();
				if (predicateURI.equals("http://www.w3.org/2000/10/annotation-ns#body")) {
					ent.label = statement.getObject().asLiteral().getString();
				} else if (predicateURI.equals("http://ns.aksw.org/scms/means")) {
					String uri = statement.getObject().asResource().getURI();
					String encode = uri.replaceAll(",", "%2C");
					ResourceImpl e = new ResourceImpl(encode);
					ent.uris.add(e);
				} else if (predicateURI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					ent.posTypesAndCategories.add(statement.getObject().asResource());
				}
			}
			tmpList.add(ent);
		}
		tmp.put("en", tmpList);

		return tmp;
	}

	// TODO transform to unit test
	public static void main(String args[]) throws MalformedURLException, ProtocolException, IOException, ParseException {
		ASpotter fox = new Fox();
		Map<String, List<Entity>> tmp = fox.getEntities("Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		for (String key : tmp.keySet()) {
			log.debug(key);
			for (Entity entity : tmp.get(key)) {
				log.debug("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTypesAndCategories) {
					log.debug("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					log.debug("\t\turi: " + r);
				}
			}
		}
		tmp = fox.getEntities("Where was President Obama born?");
		for (String key : tmp.keySet()) {
			log.debug(key);
			for (Entity entity : tmp.get(key)) {
				log.debug("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTypesAndCategories) {
					log.debug("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					log.debug("\t\turi: " + r);
				}
			}
		}

		tmp = fox.getEntities("Give me all taikonauts.");
		for (String key : tmp.keySet()) {
			log.debug(key);
			for (Entity entity : tmp.get(key)) {
				log.debug("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTypesAndCategories) {
					log.debug("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					log.debug("\t\turi: " + r);
				}
			}
		}

		log.debug("ERROR below");
		tmp = fox.getEntities("Give me all cosmonauts.");
		for (String key : tmp.keySet()) {
			log.debug(key);
			for (Entity entity : tmp.get(key)) {
				log.debug("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTypesAndCategories) {
					log.debug("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					log.debug("\t\turi: " + r);
				}
			}
		}
	}
}
