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

public class CDTClassifierMultilable {
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
		
		float cv_ave_p = 0;
		float cv_ave_r = 0;
		float cv_ave_f = 0;
		float cv_ave_best_p = 0;
		float cv_ave_best_r = 0;
		float cv_ave_best_f = 0;

		for(int n=0; n < folds; n++){
		    Instances train = randData.trainCV(folds,  n);
		    Instances test = randData.testCV(folds,  n);
			RT Classifier = new RT();
			Classifier.buildClassifier(train);
			
			/*
			 * Test the trained system
			 */
				
			float ave_p = 0;
			float ave_r = 0;
			Double ave_bestp = 0.0;
			Double ave_bestr = 0.0;
	
			for(int j = 0; j < test.size(); j++){
				Instance ins = test.get(j);
				int k = 0; 
				for(int l=0; l < data.size(); l++){
					Instance tmp = data.get(l);
					if(tmp.toString().equals(ins.toString())){
						k = l;
					}
				}
				
						
				double[] confidences = Classifier.distributionForInstance(ins);
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
				ave_p += Float.parseFloat(loadSystemP(sys2ask).get(k));				
				ave_r += Float.parseFloat(loadSystemR(sys2ask).get(k));
				double bestp = 0;
				double bestr = 0;
				for(String system:systems){
					if(Double.parseDouble(loadSystemP(system).get(k)) > bestp){bestp = Double.parseDouble(loadSystemP(system).get(k));}; 
					if(Double.parseDouble(loadSystemR(system).get(k)) > bestr){bestr = Double.parseDouble(loadSystemR(system).get(k));}; 
					}
				ave_bestp += bestp;
				ave_bestr += bestr;
				}
			
			double p = ave_p/test.size();
			double r = ave_r/test.size();
			//System.out.println("macro P : " + p);
			//System.out.println("macro R : " + r);
			double fmeasure = 2*p*r/(p + r);
			System.out.println("macro F : " + fmeasure);
			
			cv_ave_p += p/folds;
			cv_ave_r += r/folds;
			cv_ave_f += fmeasure/folds;
			
			/*
			 * calculate best possible fmeasure
			 */
			double bestp = ave_bestp/test.size();
			double bestr = ave_bestr/test.size();
			//System.out.println("best possible macro P : " + bestp);
			//System.out.println("best possible macro R : " + bestr);
			double bestfmeasure = 2*bestp*bestr/(bestp + bestr);
			System.out.println("best possible macro F : " + bestfmeasure);
			
			cv_ave_best_p += bestp/folds;
			cv_ave_best_r += bestr/folds;
			cv_ave_best_f += bestfmeasure/folds;

		}
		System.out.println(cv_ave_p);
		System.out.println(cv_ave_r);
		System.out.println(cv_ave_f);
		System.out.println('\n');
		System.out.println(cv_ave_best_p);
		System.out.println(cv_ave_best_r);
		System.out.println(cv_ave_best_f);
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