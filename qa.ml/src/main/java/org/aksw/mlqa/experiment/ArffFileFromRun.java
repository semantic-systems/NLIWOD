package org.aksw.mlqa.experiment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.systems.ASystem;
import org.aksw.qa.systems.HAWK;
import org.aksw.qa.systems.QAKIS;
import org.aksw.qa.systems.SINA;
import org.aksw.qa.systems.YODA;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class ArffFileFromRun {
	private static Logger log = LoggerFactory.getLogger(ArffFileFromRun.class);

	public static void main(String[] args) throws Exception {
		HAWK hawk = new HAWK();
		SINA sina = new SINA();
		QAKIS qakis = new QAKIS();
		YODA yoda = new YODA();
		
		/*
		 * For multilable classification:
		 */
		
		ArrayList<String> fvhawk = new ArrayList<String>();
		fvhawk.add("1");
		fvhawk.add("0");
		Attribute hawkatt = new Attribute("hawk", fvhawk);
		
		ArrayList<String> fvqakis = new ArrayList<String>();
		fvqakis.add("1");
		fvqakis.add("0");
		Attribute qakisatt = new Attribute("qakis", fvqakis);
		
		ArrayList<String> fvyoda = new ArrayList<String>();
		fvyoda.add("1");
		fvyoda.add("0");
		Attribute yodaatt = new Attribute("yoda", fvyoda);
		
		ArrayList<String> fvsina = new ArrayList<String>();
		fvsina.add("1");
		fvsina.add("0");
		Attribute sinaatt = new Attribute("sina", fvsina);
		

		/*
		 * 
		 */
		
		// 1. Learn on the training data for each system a classifier to find
		// out which system can answer which question

		// 1.1 load the questions and how good each system answers
		log.debug("Load the questions and how good each system answers");
		List<IQuestion> trainQuestions = LoaderController.load(Dataset.QALD6_Train_Multilingual);
		List<ASystem> systems = Lists.newArrayList(hawk, sina, qakis, yoda);
		JSONArray traindata = RunProducer.loadRunData(Dataset.QALD6_Train_Multilingual);
		
		// 1.2 calculate the features per question and system
		log.debug("Calculate the features per question and system");
		Analyzer analyzer = new Analyzer();
		ArrayList<Attribute> fvfinal = analyzer.fvWekaAttributes;
		
		fvfinal.add(0, hawkatt);
		fvfinal.add(0, yodaatt);
		fvfinal.add(0, sinaatt);
		fvfinal.add(0,qakisatt);
		
		
		Instances trainingSet = new Instances("training_classifier: -C 4" , fvfinal, trainQuestions.size());
		log.debug("Start collection of training data for each system");

	
		for (int i = 0; i < traindata.size(); i++) {
			JSONObject questiondata = (JSONObject) traindata.get(i);
			JSONObject allsystemsdata = (JSONObject) questiondata.get("answers");
			String question = (String) questiondata.get("question");
			Instance tmp = analyzer.analyze(question);

			tmp.setValue(hawkatt, 0);
			tmp.setValue(yodaatt, 0);
			tmp.setValue(sinaatt, 0);
			tmp.setValue(qakisatt, 0);

			for(ASystem system: systems){
				JSONObject systemdata = (JSONObject) allsystemsdata.get(system.name());
				if(new Double(systemdata.get("fmeasure").toString()) > 0)
					switch (system.name()){
					case "hawk": tmp.setValue(hawkatt, 1); 
					case "yoda": tmp.setValue(yodaatt, 1);
					case "sina": tmp.setValue(sinaatt, 1);
					case "qakis": tmp.setValue(qakisatt, 1);
					}
				}

			trainingSet.add(tmp);
			}
		log.debug(trainingSet.toString());

		try (FileWriter file = new FileWriter("./src/main/resources/Train.arff")) {
			file.write(trainingSet.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}				
		}
	}
