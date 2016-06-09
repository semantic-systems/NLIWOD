package org.aksw.hawk.spotter;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
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

import com.google.common.base.Joiner;

//FIXME deprecated until offset is set
@Deprecated 
public class Fox extends ASpotter {
	static Logger log = LoggerFactory.getLogger(Fox.class);

	private String requestURL = "http://139.18.2.164:4444/api";
	private String outputFormat = "N-Triples";
	private String taskType = "NER";
	private String inputType = "text";

	private String doTask(final String inputText) {

		String urlParameters = "type=" + inputType;
		urlParameters += "&task=" + taskType;
		urlParameters += "&output=" + outputFormat;
		try {
			urlParameters += "&input=" + URLEncoder.encode(inputText, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			log.debug("", e);
		}

		return requestPOST(urlParameters, requestURL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.hawk.nlp.NERD_module#getEntities(java.lang.String)
	 */
	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		HashMap<String, List<Entity>> mappedEntitysReturn = new HashMap<>();
		String foxJSONOutput = null;

		foxJSONOutput = doTask(question);
		if (!foxJSONOutput.equals("") && (!(foxJSONOutput == null))) {

			try {

				JSONParser parser = new JSONParser();
				JSONObject jsonArray = (JSONObject) parser.parse(foxJSONOutput);
				String output = URLDecoder.decode((String) jsonArray.get("output"), "UTF-8");

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
				mappedEntitysReturn.put("en", tmpList);

			} catch (IOException | ParseException e) {
				log.error("Could not parse Server rensponse", e);
			}

		}

		if (!mappedEntitysReturn.isEmpty()) {
			log.debug("\t" + Joiner.on("\n").join(mappedEntitysReturn.get("en")));
		}
		return mappedEntitysReturn;
	}

	// TODO CHristian: Transform to unit test
	public static void main(final String args[]) {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		ASpotter fox = new Fox();
		q.setLanguageToNamedEntites(fox.getEntities(q.getLanguageToQuestion().get("en")));
		for (String key : q.getLanguageToNamedEntites().keySet()) {
			System.out.println(key);
			for (Entity entity : q.getLanguageToNamedEntites().get(key)) {
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
