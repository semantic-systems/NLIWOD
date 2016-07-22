package org.aksw.mlqa.experiment;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.systems.ASystem;
import org.aksw.qa.systems.HAWK;
import org.aksw.qa.systems.QAKIS;
import org.aksw.qa.systems.SINA;
import org.aksw.qa.systems.YODA;
import org.aksw.qa.util.ResponseToStringParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunProducer {
	static Logger log = LoggerFactory.getLogger(RunProducer.class);
	public static void main(String[] args) throws Exception {
		getAnswers(Dataset.QALD6_Train_Multilingual);
	}
	
	/*
	 * Reads Questions and produces JSONArray with aggregated answers
	 * Entries of the JSONArray will look like : 
	 * 
		{	"question": "In which country does the Ganges start?",
			"answertype": "resource",
			"answers": {
				"qakis": {
					"foundAnswers": ["http:\/\/dbpedia.org\/resource\/India"],
					"fmeasure": 1.0
				},
				"yoda": {
					"foundAnswers": ["http:\/\/dbpedia.org\/resource\/India", "http:\/\/dbpedia.org\/resource\/Bangladesh"],
					"fmeasure": 0.6666667
				},
				"sina": {
					"foundAnswers": [],
					"fmeasure": 0.0
				},
				"hawk": {
					"foundAnswers": [],
					"fmeasure": 0.0
				}
			},
			"id": 10,
			"goldanswers": ["http:\/\/dbpedia.org\/resource\/India"]
		}
	 */

	@SuppressWarnings("unchecked")
	public static void getAnswers(Dataset dataset){
		JSONArray rundata = new JSONArray();
		HAWK hawk = new HAWK();
		SINA sina = new SINA();
		QAKIS qakis = new QAKIS();
		YODA yoda = new YODA();
		List<ASystem> systems = Arrays.asList(hawk, sina, qakis, yoda);
		List<IQuestion> questions = LoaderController.load(dataset);
		
		for(IQuestion question: questions){			
			JSONObject questiondata = new JSONObject();
			JSONObject answers = new JSONObject();
			questiondata.put("id", question.getId());
			questiondata.put("question", question.getLanguageToQuestion().get("en"));
			questiondata.put("answertype", question.getAnswerType());
			Set<String> goldAnswers = question.getGoldenAnswers();
			//we have to use Lists to get a valid json array
			questiondata.put("goldanswers", goldAnswers.stream().collect(Collectors.toList()));
			for(ASystem system: systems){
				Set<String> foundAnswers = system.search(question.getLanguageToQuestion().get("en")).getGoldenAnswers();
				JSONObject s = new JSONObject();
				//yodas answers have to be converted to resources if the answertype is "resource"
				if(system.name().equals("yoda") && question.getAnswerType().equals("resource"))
					foundAnswers = (HashSet<String>) foundAnswers.stream().map(x -> stringToResource(x)).collect(Collectors.toSet());
				//we have to use Lists to get a valid json array
				s.put("foundAnswers", foundAnswers.stream().collect(Collectors.toList()));
				s.put("fmeasure", fmeasure(foundAnswers, goldAnswers));
				answers.put(system.name(), s);
				}
			questiondata.put("answers", answers);
			rundata.add(questiondata);
			log.debug("Just wrote: " + questiondata.toJSONString());		
		}
		
		try (FileWriter file = new FileWriter("./src/main/resources/" + dataset.name() + "_Answers.txt")) {
			file.write(rundata.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * loads previously written JSONArray
	 */
	public static JSONArray loadRunData(Dataset dataset){
		String loadeddata;
		try {			
			Path datapath = Paths.get("./src/main/resources/" + dataset.name() + "_Answers.txt");
			loadeddata = Files.lines(datapath).collect(Collectors.joining());
			JSONParser parser = new JSONParser();
			JSONArray arr = (JSONArray) parser.parse(loadeddata);
			return arr;
		} catch (IOException | ParseException  e) {
			e.printStackTrace();
			log.debug("loading failed.");
			return new JSONArray();
		}

	}
	/*
	 * a ~ retrieved answers b ~ gold answers
	 */
	
	public static float fmeasure(Set<String> foundAnswers, Set<String> goldAnswers){
		float precision;
		float recall;
		float fmeasure;
		HashSet<String> intersection = new HashSet<String>(foundAnswers);
		intersection.retainAll(goldAnswers);
		if(foundAnswers.size()>0)
			precision = intersection.size() / (float) foundAnswers.size();
		else
			precision = 0;
		recall = intersection.size() / (float) goldAnswers.size();
		if(recall > 0 && precision >0)
			fmeasure = 2*(precision*recall) / (float) (precision + recall);
		else
			fmeasure = 0;
		return fmeasure;
	}
	/*
	 * turns yodas answers into dbpedia resources : 
	 */
	
	public static String stringToResource(String answer){
		try {
			ResponseToStringParser parser = new ResponseToStringParser();
			HttpClient client = HttpClientBuilder.create().build();
			URI uri = new URIBuilder().setScheme("http").setHost("dbpedia.org").setPath("/sparql")
					.setParameter("default-graph-uri", "http://dbpedia.org")
					.setParameter("query", "SELECT ?uri WHERE {?uri rdfs:label \"" + answer + "\"@en.}")
					.setParameter("format", "text/html")
					.setParameter("CXML_redir_for_subjs", "121")
					.setParameter("CSML_redir_for_hrefs", "")
					.setParameter("timeout", "30000")
					.setParameter("debug", "on")
					.build();
			HttpGet httpget = new HttpGet(uri);
			HttpResponse response = client.execute(httpget);
			Document doc;
			doc = Jsoup.parse(parser.responseToString(response));
		return doc.select("a").attr("href");
		} catch (IllegalStateException | IOException | URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
		
		
	}
}
