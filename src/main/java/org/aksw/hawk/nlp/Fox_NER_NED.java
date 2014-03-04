package org.aksw.hawk.nlp;

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

public class Fox_NER_NED implements NERD_module {
	static Logger log = LoggerFactory.getLogger(Fox_NER_NED.class);

	private String request = "http://139.18.2.164:4444/api";
	private String outputFormat = "N3";
	private String taskType = "NER";
	private String inputType = "text";

	public Fox_NER_NED(String outputFormat, String taskType, String nif, String inputType) {
		this.outputFormat = outputFormat;
		this.taskType = taskType;
		this.inputType = inputType;

	}

	public Fox_NER_NED() {
	}

	private String doTASK(String inputText) throws MalformedURLException, IOException, ProtocolException {

		String urlParameters = "type=" + inputType + "&";
		urlParameters += "task=" + taskType + "&";
		urlParameters += "output=" + outputFormat + "&";
		urlParameters += "input=" + URLEncoder.encode(inputText, "UTF-8");

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

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.aksw.hawk.nlp.NERD_module#getEntities(java.lang.String)
	 */
	@Override
	public Map<String, List<Entity>> getEntities(String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<String, List<Entity>>();
		try {
			String foxJSONOutput = doTASK(question);

			JSONParser parser = new JSONParser();
			JSONArray jsonArray = (JSONArray) parser.parse(foxJSONOutput);

			String output = URLDecoder.decode((String) ((JSONObject) jsonArray.get(0)).get("output"), "UTF-8");

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
						ent.uris.add(statement.getObject().asResource());
					} else if (predicateURI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
						ent.posTags.add(statement.getObject().asResource());
					}
				}
				tmpList.add(ent);
			}
			tmp.put("en", tmpList);

		} catch (IOException | ParseException e) {
			log.error("Could not call FOX for NER/NED", e);
		}
		return tmp;
	}

	public static void main(String args[]) {
		Question q = new Question();
		q.languageToQuestion.put("en", "Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		NERD_module fox = new Fox_NER_NED();
		q.languageToNamedEntites = fox.getEntities(q.languageToQuestion.get("en"));
		for (String key : q.languageToNamedEntites.keySet()) {
			System.out.println(key);
			for (Entity entity : q.languageToNamedEntites.get(key)) {
				System.out.println("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTags) {
					System.out.println("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					System.out.println("\t\turi: " + r);
				}
			}
		}
	}
}
