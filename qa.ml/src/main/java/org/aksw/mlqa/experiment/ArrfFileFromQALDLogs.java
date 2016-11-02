package org.aksw.mlqa.experiment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.qa.commons.load.Dataset;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class ArrfFileFromQALDLogs {
	static Logger log = LoggerFactory.getLogger(ArrfFileFromQALDLogs.class);

	/*
	 * Class to parse Results from QALD6 LogFiles 
	 */
	
	public static void main(String[] args) {
		ArrayList<String> systems = Lists.newArrayList("KWGAnswer", "NbFramework", "PersianQA", "SemGraphQA", "UIQA_withoutManualEntries", "UTQA_English" );
		Analyzer analyzer = new Analyzer();
		ArrayList<Attribute> fvfinal = analyzer.fvWekaAttributes;
		// create the attributes
		ArrayList<String> test = Lists.newArrayList(); 
		for(String system: systems){
			test.add(system);
			fvfinal.add(0, new Attribute(system, Lists.newArrayList("0","1")));
		}
		System.out.println(test);
		// load trainQuestions
		JSONObject qald6test = Utils.loadTestQuestions();
		JSONArray questions = (JSONArray) qald6test.get("questions");
		ArrayList<String> testQuestions = Lists.newArrayList();
		for(int i = 0; i < questions.size(); i++){
			JSONObject questionData = (JSONObject) questions.get(i);
			JSONArray questionStrings = (JSONArray) questionData.get("question");
			JSONObject questionEnglish = (JSONObject) questionStrings.get(0);
			testQuestions.add((String) questionEnglish.get("string"));
		}
		// create Instances
		Instances trainingSet = new Instances("training_classifier: -C " + systems.size() , fvfinal, testQuestions.size());
		log.debug("Start collection of training data for each system");
		for(int i = 0; i < testQuestions.size(); i++){
			Instance tmp = analyzer.analyze(testQuestions.get(i));
			for(int j = 0; j < systems.size(); j++){
				//if fmeasure > 0, target will be 1, else 0
				if(new Double(Utils.loadSystemData(systems.get(systems.size() -1  - j)).get(i)) > 0){
					tmp.setValue(j, 1);
				} else {
					tmp.setValue(j, 0);
				}
			}
			trainingSet.add(tmp);
		}
		log.debug(trainingSet.toString());
		
		try (FileWriter file = new FileWriter("./src/main/resources/Qald6Logs.arff")) {
			file.write(trainingSet.toString());
		} catch (IOException e) {
			log.debug("writing failed.");
			e.printStackTrace();
		}				
	};
}
