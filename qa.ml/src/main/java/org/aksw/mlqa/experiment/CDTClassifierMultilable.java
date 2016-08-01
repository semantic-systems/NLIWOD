package org.aksw.mlqa.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

import meka.classifiers.multilabel.PSt;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class CDTClassifierMultilable {
	static Logger log = LoggerFactory.getLogger(CDTClassifierMultilable.class);
	
	
	public static void main(String[] args) throws Exception {		
		/*
		 * For multilable classification:
		 */
		
		//The classifier
		PSt PSt_Classifier = new PSt();
		//load the data
		Path datapath= Paths.get("./src/main/resources/Qald6Logs.arff");
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(6);
		PSt_Classifier.buildClassifier(data);
		
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

		
		ArrayList<String> systems = Lists.newArrayList("KWGAnswer", "NbFramework", "PersianQA", "SemGraphQA", "UIQA_withoutManualEntries", "UTQA_English" );
		float ave_p = 0;
		float ave_r = 0;
		Double ave_bestp = 0.0;
		Double ave_bestr = 0.0;

		for(int j = 0; j < data.size(); j++){
			Instance ins = data.get(j);
			double[] confidences = PSt_Classifier.distributionForInstance(ins);
			int argmax = -1;
			double max = -1;
				for(int i = 0; i < 6; i++){
					if(confidences[i]>max){
						max = confidences[i];
						argmax = i;
					}
				}
				//compare trained system with best possible system
				
				String sys2ask = systems.get(systems.size() - argmax -1);
				ave_p += Float.parseFloat(loadSystemP(sys2ask).get(j));				
				ave_r += Float.parseFloat(loadSystemR(sys2ask).get(j));
				
				double bestp = 0;
				double bestr = 0;
				String bestSystemp = "";
				String bestSystemr = ""; 
				for(String system:systems){
					if(Double.parseDouble(loadSystemP(system).get(j)) > bestp){bestSystemp = system; bestp = Double.parseDouble(loadSystemP(system).get(j));}; 
					if(Double.parseDouble(loadSystemR(system).get(j)) > bestr){bestSystemr = system; bestr = Double.parseDouble(loadSystemR(system).get(j));}; 
					}
				ave_bestp += bestp;
				ave_bestr += bestr;
				System.out.println(testQuestions.get(j));
				System.out.println(j + "... asked " + sys2ask + " with p " + loadSystemP(sys2ask).get(j) + "... best possible p: " + bestp + " was achieved by " + bestSystemp);
				System.out.println(j + "... asked " + sys2ask + " with r " + loadSystemR(sys2ask).get(j) + "... best possible r: " + bestr + " was achieved by " + bestSystemr);

				}
		
		double p = ave_p/data.size();
		double r = ave_r/data.size();
		System.out.println("macro P : " + p);
		System.out.println("macro R : " + r);
		double fmeasure = 2*p*r/(p + r);
		System.out.println("macro F : " + fmeasure);
	
		/*
		 * calculate best possible fmeasure
		 */

		double bestp = ave_bestp/data.size();
		double bestr = ave_bestr/data.size();
		System.out.println("best possible macro P : " + bestp);
		System.out.println("best possible macro R : " + bestr);
		double bestfmeasure = 2*bestp*bestr/(bestp + bestr);
		System.out.println("best possible macro F : " + bestfmeasure);
	
		Set<Integer> test = new HashSet<Integer>(Arrays.asList(7,8,9,10,11,12,13,14,15,16,17,18,19));
		System.out.println(powerSet(test).size());
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