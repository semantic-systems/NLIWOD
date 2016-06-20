package org.aksw.mlqa.experiment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
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

import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class SimpleClassification {
	static Logger log = LoggerFactory.getLogger(SimpleClassification.class);

	public static void main(String[] args) throws Exception {
		HAWK hawk = new HAWK();
		SINA sina = new SINA();
		QAKIS qakis = new QAKIS();
		YODA yoda = new YODA();
		
		// 1. Learn on the training data for each system a classifier to find
		// out which system can answer which question

		// 1.1 load the questions and how good each system answers
		log.debug("Load the questions and how good each system answers");
		List<IQuestion> trainQuestions = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);
		List<ASystem> systems = Lists.newArrayList(hawk, sina, qakis, yoda);
		JSONArray traindata = RunProducer.loadRunData(Dataset.QALD6_Train_Multilingual);
		
		// 1.2 calculate the features per question and system
		log.debug("Calculate the features per question and system");
		Analyzer analyzer = new Analyzer();
		// Create an empty training set per system
		Map<ASystem, Instances> instancesPerSystem = new HashMap<ASystem, Instances>();
		for (ASystem system: systems) {
			Instances isTrainingSet = new Instances("training_" + system.name(), analyzer.fvWekaAttributes, trainQuestions.size());
			// set class attribute
			isTrainingSet.setClass(analyzer.getClassAttribute());
			instancesPerSystem.put(system, isTrainingSet);
		}

		log.debug("Start collection of training data for each system");

		for (ASystem system: systems) {
			Instances instances = instancesPerSystem.get(system);
			// TODO fix this, it calculates each feature per question
			// $runs.size() times
			for (int i = 0; i < traindata.size(); i++) {
				JSONObject questiondata = (JSONObject) traindata.get(i);
				JSONObject allsystemsdata = (JSONObject) questiondata.get("answers");
				JSONObject systemdata = (JSONObject) allsystemsdata.get(system.name());
				String question = (String) questiondata.get("question");
				
				// calculate features
				Instance tmp = analyzer.analyze(question);

				// add to instances of the particular system
				tmp.setValue((Attribute) analyzer.getClassAttribute(), new Double(systemdata.get("fmeasure").toString()));
				instances.add(tmp);		
				}
			log.info(instances.toSummaryString());
			try (FileWriter file = new FileWriter("./src/main/resources/" + system.name() +  ".arff")) {
				file.write(instances.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}	

			instancesPerSystem.put(system, instances);
			
		}
		// TODO since many systems submit only a subset of questions, take all
		// submitted logs together and optimize best training set for each
		// system by remembering the best f-measure for each question. Hope is,
		// we get less sparse instances

		// 2.3 use machine learning to train it
		log.debug("Start machine learning");
		// iterate the classifiers here
		Map<ASystem, Classifier> classifierPerSystem = new HashMap<ASystem, Classifier>();
		for (ASystem system: systems) {
			// TODO add more classifiers (see
			// https://github.com/AKSW/fox/blob/master/src%2Fmain%2Fjava%2Forg%2Faksw%2Ffox%2Fnerlearner%2FFoxClassifierFactory.java)
			// Create a naïve bayes classifier

			Classifier cModel = (Classifier) new MultilayerPerceptron();
			((MultilayerPerceptron) cModel).setNominalToBinaryFilter(true);
			cModel.buildClassifier(instancesPerSystem.get(system));

			//TODO  // Test the model
//			 Evaluation eTest = new Evaluation(isTrainingSet);
//			 eTest.evaluateModel(cModel, isTestingSet);
//
//			The evaluation module can output a bunch of statistics:
//			 // Print the result à la Weka explorer:
//			 String strSummary = eTest.toSummaryString();
//			 System.out.println(strSummary);
//			 
//			 // Get the confusion matrix
//			 double[][] cmMatrix = eTest.confusionMatrix();
//			 
			classifierPerSystem.put(system, cModel);
		}
		
		log.info("Machine learning done... commence to testing!");
		// 3. Use the classifier model to decide which system should answer the
		// current question and measure the performance
		List<IQuestion> testQuestions = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);

		for (IQuestion q : testQuestions) {
			// calculate features
			Instance tmpInstance = analyzer.analyze(q.getLanguageToQuestion().get("en"));
			System.out.println(tmpInstance.toString());
			// decide which system to use
			String result = q.getLanguageToQuestion().get("en") + "\t";
			for (ASystem key : classifierPerSystem.keySet()) {
				tmpInstance.setDataset(instancesPerSystem.get(key));
				double projectedfmeasure = classifierPerSystem.get(key).classifyInstance(tmpInstance);
				result += key.name() + ":  " + projectedfmeasure + "\n";
			}
			log.info(result);

			// TODO calculate f-measure
		}
	}
}
