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

import meka.classifiers.multilabel.BCC;
import meka.classifiers.multilabel.BPNN;
import meka.classifiers.multilabel.BRq;
import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.CDN;
import meka.classifiers.multilabel.CDT;
import meka.classifiers.multilabel.CT;
import meka.classifiers.multilabel.DBPNN;
import meka.classifiers.multilabel.FW;
import meka.classifiers.multilabel.HASEL;
import meka.classifiers.multilabel.LC;
import meka.classifiers.multilabel.MCC;
import meka.classifiers.multilabel.MULAN;
import meka.classifiers.multilabel.MajorityLabelset;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multilabel.PCC;
import meka.classifiers.multilabel.PMCC;
import meka.classifiers.multilabel.PS;
import meka.classifiers.multilabel.PSt;
import meka.classifiers.multilabel.RAkEL;
import meka.classifiers.multilabel.RAkELd;
import meka.classifiers.multilabel.RT;
import weka.core.Debug.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class SystemTestonCV {
	static Logger log = LoggerFactory.getLogger(CDTClassifierMultilable.class);
	
	
	public static void main(String[] args) throws Exception {		

		Path datapath= Paths.get("./src/main/resources/Qald6Logs.arff");
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(6);
		
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


		int seed = 133;
		int folds = 10;
		
		Random rand = new Random(seed);
		Instances randData = new Instances(data);
		randData.randomize(rand);
		
		for(String system: systems){
			double cv_ave_f = 0;
			for(int n=0; n < folds; n++){
			    Instances train = randData.trainCV(folds,  n);
			    Instances test = randData.testCV(folds,  n);
				double cv_f = 0;
	
				for(int j = 0; j < test.size(); j++){
					Instance ins = test.get(j);
					int k = 0; 
					for(int l=0; l < data.size(); l++){
						Instance tmp = data.get(l);
						if(tmp.toString().equals(ins.toString())){
							k = l;
						}
					}
					
			//compare trained system with best possible system
						
					double p = Float.parseFloat(loadSystemP(system).get(k));				
					double r = Float.parseFloat(loadSystemR(system).get(k));
					if(!(p==0&&r==0)){
						cv_f += (2*p*r/(p+r))/test.size();
					}
				}
				cv_ave_f += cv_f/folds;
			}
			System.out.println(system + ": " + cv_ave_f);
		}
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
}