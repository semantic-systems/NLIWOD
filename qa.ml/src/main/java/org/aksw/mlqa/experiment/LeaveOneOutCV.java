package org.aksw.mlqa.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

import meka.classifiers.multilabel.RT;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class LeaveOneOutCV {
	private static Logger log = LoggerFactory.getLogger(CDTClassifierMultilable.class);
	
	
	public static void main(String[] args) throws Exception {		
		/*
		 * For multilable classification:
		 */
		//load the data
		Path datapath= Paths.get("./src/main/resources/Qald6Logs.arff");
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		/*
		 * Test the trained system
		 */
		
		JSONObject qald6test = loadTestQuestions();
		JSONArray questions = (JSONArray) qald6test.get("questions");
		ArrayList<String> testQuestions = Lists.newArrayList();
		for(int i = 0; i < questions.size(); i++){
			JSONObject questionData = (JSONObject) questions.get(i);
			JSONArray questionStrings = (JSONArray) questionData.get("question");
			JSONObject questionEnglish = (JSONObject) questionStrings.get(0);
			testQuestions.add((String) questionEnglish.get("string"));
		}

		Instances data = arff.getData();
		data.setClassIndex(6);

		double cv_ave = 0;
		ArrayList<String> systems = Lists.newArrayList("KWGAnswer", "NbFramework", "PersianQA", "SemGraphQA", "UIQA_withoutManualEntries", "UTQA_English" );
		for(int i = 0; i < 100; i++){
			Instance testquestion = data.get(i);
			data.remove(i);
			RT classifier = new RT();
			classifier.buildClassifier(data);
			double[] confidences = classifier.distributionForInstance(testquestion);

			int argmax = -1;
			double max = -1;
			for(int j = 0; j < 6; j++){
				if(confidences[j]>max){
					max = confidences[j];
					argmax = j;
				}
			}
			String sys2ask = systems.get(systems.size() - argmax -1);
			float p = Float.parseFloat(loadSystemP(sys2ask).get(i));				
			float r = Float.parseFloat(loadSystemR(sys2ask).get(i));
			double f = 0;
			if(p>0&&r>0){f = 2*p*r/(p + r);}
			cv_ave += f;
			data.add(i, testquestion);
		}
		System.out.println(cv_ave/100);
	}
	
	public static ArrayList<String> loadSystemP(String system){

		Path datapath = Paths.get("./src/main/resources/QALD6MultilingualLogs/multilingual_" + system + ".html");
		ArrayList<String> result = Lists.newArrayList();

		try{
			String loadedData = Files.lines(datapath).collect(Collectors.joining()); 
			Document doc = Jsoup.parse(loadedData);
			Element table = doc.select("table").get(5);
			Elements tableRows = table.select("tr");
			for(Element row: tableRows){
				Elements tableEntry = row.select("td");
				result.add(tableEntry.get(2).ownText());
			}
			result.remove(0); //remove the head of the table
			return result;
		}catch(IOException e){
			e.printStackTrace();
			log.debug("loading failed.");
			return result;
		}
	}
	public static ArrayList<String> loadSystemR(String system){
		Path datapath = Paths.get("./src/main/resources/QALD6MultilingualLogs/multilingual_" + system + ".html");
		ArrayList<String> result = Lists.newArrayList();

		try{
			String loadedData = Files.lines(datapath).collect(Collectors.joining()); 
			Document doc = Jsoup.parse(loadedData);
			Element table = doc.select("table").get(5);
			Elements tableRows = table.select("tr");
			for(Element row: tableRows){
				Elements tableEntry = row.select("td");
				result.add(tableEntry.get(1).ownText());
			}
			result.remove(0); //remove the head of the table
			return result;
		}catch(IOException e){
			e.printStackTrace();
			log.debug("loading failed.");
			return result;
		}
	}
	
	public static JSONObject loadTestQuestions(){
		String loadeddata;
		try {			
			Path datapath = Paths.get("./src/main/resources/qald-6-test-multilingual.json");
			loadeddata = Files.lines(datapath).collect(Collectors.joining());
			JSONParser parser = new JSONParser();
			JSONObject arr = (JSONObject) parser.parse(loadeddata);
			return arr;
		} catch (IOException | ParseException  e) {
			e.printStackTrace();
			log.debug("loading failed.");
			return new JSONObject();
		}
	}
	
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	    	sets.add(new HashSet<T>());
	    	return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	    	Set<T> newSet = new HashSet<T>();
	    	newSet.add(head);
	    	newSet.addAll(set);
	    	sets.add(newSet);
	    	sets.add(set);
	    }		
	    return sets;
	}


}
